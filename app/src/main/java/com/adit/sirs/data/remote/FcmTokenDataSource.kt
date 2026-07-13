package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.os.Build
import com.adit.sirs.core.constants.AppConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `FcmTokenDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FcmTokenDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    suspend fun registerToken(uid: String, role: String) {
        val token = messaging.token.await()
        val data = mapOf(
            "uid" to uid,
            "token" to token,
            "platform" to "android",
            "deviceName" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "role" to role,
            "isActive" to true,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        firestore.collection(AppConstants.COLLECTION_DEVICE_TOKENS)
            .document(token)
            .set(data)
            .await()
    }

    // FLOW: Fungsi `removeToken` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun removeToken() {
        try {
            val token = messaging.token.await()
            firestore.collection(AppConstants.COLLECTION_DEVICE_TOKENS)
                .document(token)
                .delete()
                .await()
        } catch (_: Exception) {
            // Ignore token removal errors during logout
        }
    }
}
