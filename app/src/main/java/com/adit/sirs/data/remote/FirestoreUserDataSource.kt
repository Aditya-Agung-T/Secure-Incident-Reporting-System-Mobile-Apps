package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.constants.AppConstants
import com.adit.sirs.data.model.UserDto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `FirestoreUserDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection get() = firestore.collection(AppConstants.COLLECTION_USERS)

    // FLOW: Fungsi `createUserProfile` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun createUserProfile(uid: String, name: String, email: String) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to "user",
            "isActive" to true,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        usersCollection.document(uid).set(userData).await()
    }

    // FLOW: Fungsi `getUserProfile` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun getUserProfile(uid: String): UserDto? {
        val doc = usersCollection.document(uid).get().await()
        if (doc.exists()) {
            val dto = doc.toObject(UserDto::class.java)
            return dto?.copy(uid = doc.id)
        }
        return null
    }

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    suspend fun updateLastLogin(uid: String) {
        usersCollection.document(uid).update(
            mapOf(
                "lastLoginAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    // FLOW: Fungsi `updateProfile` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun updateProfile(uid: String, name: String) {
        usersCollection.document(uid).update(
            mapOf(
                "name" to name,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }
}
