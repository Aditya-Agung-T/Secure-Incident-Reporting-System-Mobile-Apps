package com.adit.sirs.domain.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.domain.model.User
import com.adit.sirs.core.common.Result

interface AuthRepository {
    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    suspend fun login(email: String, password: String): Result<User>
    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    suspend fun register(name: String, email: String, password: String): Result<User>
    // FLOW: Menghapus sesi/token lokal lalu mengembalikan aplikasi ke halaman login.
    suspend fun logout(): Result<Unit>
    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    suspend fun sendPasswordReset(email: String): Result<Unit>
    // FLOW: Fungsi `getCurrentUser` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan kontrak operasi data agar ViewModel tidak bergantung langsung ke implementasi.
    suspend fun getCurrentUser(): Result<User?>
    // FLOW: Fungsi `isLoggedIn` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan kontrak operasi data agar ViewModel tidak bergantung langsung ke implementasi.
    fun isLoggedIn(): Boolean
}
