package com.adit.sirs.core.security

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.adit.sirs.core.constants.AppConstants
import java.io.IOException
import java.util.Locale

// FLOW: `FileValidationResult` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class FileValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0,
    val fileName: String? = null
)

// FLOW: [FileValidation] merupakan objek keamanan (*security helper*) yang bertanggung jawab memvalidasi keaslian file.
// Tidak hanya mengecek ekstensi, tapi juga mencocokkan *Magic Bytes* / File Signature asli untuk mencegah injeksi malware.
object FileValidation {

    private const val MAX_SIGNATURE_BYTES = 8

    // FLOW: Memeriksa MIME, ukuran, ekstensi, dan magic bytes agar file upload benar-benar aman.
    fun validateFile(
        contentResolver: ContentResolver,
        uri: Uri
    ): FileValidationResult {
        val mimeType = contentResolver.getType(uri)?.lowercase(Locale.ROOT)
        if (!MimeTypeValidator.isAllowed(mimeType)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Tipe file tidak valid. Hanya JPG, PNG, dan PDF yang diperbolehkan."
            )
        }

        val metadata = readFileMetadata(contentResolver, uri)
        val fileSize = metadata.fileSize
        val fileName = metadata.fileName

        if (fileSize <= 0) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ukuran file tidak dapat dibaca."
            )
        }

        if (fileSize > AppConstants.MAX_ATTACHMENT_SIZE_BYTES) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "File terlalu besar. Ukuran maksimal 2 MB."
            )
        }

        val extension = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }

        if (extension != null && extension !in AppConstants.ALLOWED_EXTENSIONS) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ekstensi file tidak valid. Hanya JPG, PNG, dan PDF yang diperbolehkan."
            )
        }

        val expectedExtension = MimeTypeValidator.getExtensionFromMime(mimeType!!)
        if (extension != null && expectedExtension != null && !isCompatibleExtension(expectedExtension, extension)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ekstensi file tidak sesuai dengan tipe file."
            )
        }

        val signature = readSignature(contentResolver, uri)
            ?: return FileValidationResult(
                isValid = false,
                errorMessage = "File tidak dapat dibaca untuk validasi keamanan."
            )

        val detectedMimeType = detectMimeTypeFromMagicBytes(signature)
            ?: return FileValidationResult(
                isValid = false,
                errorMessage = "Signature file tidak valid. Hanya file JPG, PNG, dan PDF asli yang diperbolehkan."
            )

        if (!isCompatibleMimeType(mimeType, detectedMimeType)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Isi file tidak sesuai dengan tipe file yang dipilih."
            )
        }

        return FileValidationResult(
            isValid = true,
            mimeType = detectedMimeType,
            fileSize = fileSize,
            fileName = fileName
        )
    }

    // FLOW: Membaca metadata nama dan ukuran file dari Android ContentResolver tanpa meload seluruh isi file ke memori.
    private fun readFileMetadata(contentResolver: ContentResolver, uri: Uri): FileMetadata {
        var fileSize = 0L
        var fileName: String? = null

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) fileSize = cursor.getLong(sizeIndex)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) fileName = cursor.getString(nameIndex)
            }
        }

        if (fileSize <= 0) {
            fileSize = contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize.takeIf { it > 0 }
            } ?: 0L
        }

        return FileMetadata(fileSize = fileSize, fileName = fileName)
    }

    // FLOW: Fungsi `readSignature` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun readSignature(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(MAX_SIGNATURE_BYTES)
                val read = inputStream.read(buffer)
                if (read <= 0) null else buffer.copyOf(read)
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    // FLOW: Fungsi `detectMimeTypeFromMagicBytes` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun detectMimeTypeFromMagicBytes(bytes: ByteArray): String? {
        return when {
            bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte() -> "image/jpeg"

            bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte() &&
                bytes[4] == 0x0D.toByte() &&
                bytes[5] == 0x0A.toByte() &&
                bytes[6] == 0x1A.toByte() &&
                bytes[7] == 0x0A.toByte() -> "image/png"

            bytes.size >= 5 &&
                bytes[0] == 0x25.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x44.toByte() &&
                bytes[3] == 0x46.toByte() &&
                bytes[4] == 0x2D.toByte() -> "application/pdf"

            else -> null
        }
    }

    // FLOW: Fungsi `isCompatibleMimeType` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun isCompatibleMimeType(reportedMimeType: String, detectedMimeType: String): Boolean {
        return reportedMimeType == detectedMimeType ||
            (reportedMimeType == "image/jpg" && detectedMimeType == "image/jpeg")
    }

    // FLOW: Fungsi `isCompatibleExtension` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun isCompatibleExtension(expectedExtension: String, actualExtension: String): Boolean {
        return expectedExtension == actualExtension ||
            (expectedExtension == "jpg" && actualExtension == "jpeg")
    }

    private data class FileMetadata(
        val fileSize: Long,
        val fileName: String?
    )
}
