package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import com.adit.sirs.BuildConfig
import com.adit.sirs.domain.model.Attachment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `CloudinaryUploadDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class CloudinaryUploadDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // FLOW: Mengirim file valid ke Cloudinary dan mengembalikan metadata attachment untuk laporan.
    suspend fun uploadFile(
        contentResolver: ContentResolver,
        fileUri: Uri,
        mimeType: String,
        fileName: String
    ): Attachment = withContext(Dispatchers.IO) {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        val folder = BuildConfig.CLOUDINARY_UPLOAD_FOLDER

        val resourceType = if (mimeType == "application/pdf") "raw" else "image"
        val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"

        val inputStream = contentResolver.openInputStream(fileUri)
            ?: throw IOException("Cannot open file")

        val fileBytes = inputStream.use { it.readBytes() }
        val mediaType = mimeType.toMediaType()

        // LANGKAH 1: Minta "Signature" ke Cloudflare Worker
        val userToken = auth.currentUser?.getIdToken(false)?.await()?.token
            ?: throw IOException("User token not available")

        // Memastikan URL berakhiran /generate-signature
        val baseUrl = BuildConfig.CLOUDINARY_DELETE_ENDPOINT.trimEnd('/')
        val signatureUrl = if (baseUrl.endsWith("/destroy")) {
            baseUrl.replace("/destroy", "/generate-signature")
        } else {
            "$baseUrl/generate-signature"
        }

        val sigRequest = Request.Builder()
            .url(signatureUrl)
            .get()
            .addHeader("Authorization", "Bearer $userToken")
            .build()

        val sigResponse = client.newCall(sigRequest).execute()
        if (!sigResponse.isSuccessful) {
            throw IOException("Gagal mendapatkan otorisasi upload: ${sigResponse.body?.string()}")
        }

        val sigJson = JSONObject(sigResponse.body!!.string())
        val signature = sigJson.getString("signature")
        val timestamp = sigJson.getString("timestamp")
        val apiKey = sigJson.getString("api_key")
        val folderConfig = sigJson.getString("folder")

        // LANGKAH 2: Upload ke Cloudinary memakai Signature (BUKAN Unsigned Preset lagi)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, fileBytes.toRequestBody(mediaType))
            .addFormDataPart("folder", folderConfig)
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("timestamp", timestamp)
            .addFormDataPart("signature", signature)
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("Upload failed: $errorBody")
        }

        val responseBody = response.body?.string()
            ?: throw IOException("Empty response from Cloudinary")

        val json = JSONObject(responseBody)

        Attachment(
            originalName = fileName,
            publicId = json.optString("public_id", ""),
            secureUrl = json.optString("secure_url", ""),
            resourceType = json.optString("resource_type", resourceType),
            format = json.optString("format", ""),
            mimeType = mimeType,
            bytes = json.optLong("bytes", 0),
            uploadedAt = Timestamp.now()
        )
    }

    // FLOW: Menghapus file secara permanen dari Cloudinary dengan memanggil backend Serverless yang memvalidasi otorisasi Token Firebase JWT pengguna.
    suspend fun deleteFile(publicId: String, resourceType: String) = withContext(Dispatchers.IO) {
        if (publicId.isBlank()) return@withContext

        val endpoint = BuildConfig.CLOUDINARY_DELETE_ENDPOINT
        if (endpoint.isBlank()) {
            throw IOException("Konfigurasi hapus file Cloudinary belum tersedia. Set CLOUDINARY_DELETE_ENDPOINT ke endpoint backend.")
        }

        val user = auth.currentUser ?: throw IOException("User tidak terautentikasi")
        val idToken = try {
            user.getIdToken(false).await().token
        } catch (e: Exception) {
            throw IOException("Gagal mendapatkan Firebase ID Token: ${e.message}")
        }
        if (idToken.isNullOrBlank()) throw IOException("Token autentikasi tidak valid")

        val payload = JSONObject()
            .put("publicId", publicId)
            .put("resourceType", resourceType.ifBlank { "image" })
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $idToken")
            .post(payload)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("Gagal menghapus file Cloudinary: $errorBody")
        }
    }
}
