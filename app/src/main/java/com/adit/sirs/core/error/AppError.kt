package com.adit.sirs.core.error

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


sealed class AppError : Exception() {
    // FLOW: `AuthError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class AuthError(override val message: String) : AppError()
    // FLOW: `NetworkError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class NetworkError(override val message: String) : AppError()
    // FLOW: `FirestoreError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class FirestoreError(override val message: String) : AppError()
    // FLOW: `ValidationError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class ValidationError(override val message: String) : AppError()
    // FLOW: `UploadError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class UploadError(override val message: String) : AppError()
    // FLOW: `PermissionError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class PermissionError(override val message: String) : AppError()
    // FLOW: `UnknownError` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class UnknownError(override val message: String = "An unexpected error occurred") : AppError()
}
