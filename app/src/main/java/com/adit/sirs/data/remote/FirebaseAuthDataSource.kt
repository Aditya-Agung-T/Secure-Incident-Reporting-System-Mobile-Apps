package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `FirebaseAuthDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    // FLOW: `val currentUser: FirebaseUser? get()` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser: FirebaseUser? get() = auth.currentUser
    // FLOW: `val currentUid: String? get()` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUid: String? get() = auth.currentUser?.uid

    // FLOW: Fungsi `signIn` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Login failed")
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Registration failed")
    }

    // FLOW: Fungsi `signOut` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    fun signOut() {
        auth.signOut()
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // FLOW: Fungsi `isLoggedIn` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    fun isLoggedIn(): Boolean = auth.currentUser != null
}
