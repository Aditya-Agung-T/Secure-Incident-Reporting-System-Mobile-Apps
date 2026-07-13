package com.adit.sirs.core.error

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

// FLOW: `ErrorMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object ErrorMapper {
    // FLOW: Fungsi `mapException` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun mapException(e: Throwable): AppError {
        return when (e) {
            is AppError -> e
            is FirebaseAuthException -> mapAuthError(e)
            is FirebaseFirestoreException -> mapFirestoreError(e)
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException -> AppError.NetworkError("Jaringan tidak tersedia. Periksa koneksi internet Anda.")
            else -> AppError.UnknownError(e.message ?: "Terjadi kesalahan tidak terduga")
        }
    }

    // FLOW: Fungsi `mapAuthError` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    private fun mapAuthError(e: FirebaseAuthException): AppError.AuthError {
        val message = when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Alamat email tidak valid."
            "ERROR_WRONG_PASSWORD" -> "Password salah."
            "ERROR_USER_NOT_FOUND" -> "Akun dengan email ini tidak ditemukan."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar."
            "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah."
            "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan."
            "ERROR_INVALID_CREDENTIAL" -> "Email atau password tidak valid."
            else -> e.message ?: "Terjadi kesalahan autentikasi."
        }
        return AppError.AuthError(message)
    }

    // FLOW: Fungsi `mapFirestoreError` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    private fun mapFirestoreError(e: FirebaseFirestoreException): AppError {
        return when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> AppError.PermissionError("Anda tidak memiliki izin untuk melakukan aksi ini.")
            FirebaseFirestoreException.Code.NOT_FOUND -> AppError.FirestoreError("Data tidak ditemukan.")
            FirebaseFirestoreException.Code.UNAVAILABLE -> AppError.NetworkError("Layanan tidak tersedia. Coba lagi nanti.")
            else -> AppError.FirestoreError(e.message ?: "Terjadi kesalahan database.")
        }
    }

    // FLOW: Fungsi `toUserMessage` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun toUserMessage(e: Throwable): String {
        return when (val error = mapException(e)) {
            is AppError.AuthError -> error.message
            is AppError.NetworkError -> error.message
            is AppError.FirestoreError -> error.message
            is AppError.ValidationError -> error.message
            is AppError.UploadError -> error.message
            is AppError.PermissionError -> error.message
            is AppError.UnknownError -> error.message
        }
    }
}
