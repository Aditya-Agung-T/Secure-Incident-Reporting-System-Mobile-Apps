package com.adit.sirs.core.common

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


sealed class Result<out T> {
    // FLOW: `Success<T>` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class Success<T>(val data: T) : Result<T>()
    // FLOW: `Error` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
