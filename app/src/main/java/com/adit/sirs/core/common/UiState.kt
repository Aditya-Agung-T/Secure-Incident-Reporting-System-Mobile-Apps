package com.adit.sirs.core.common

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    // FLOW: `Success<T>` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class Success<T>(val data: T) : UiState<T>()
    // FLOW: `Error` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class Error(val message: String) : UiState<Nothing>()
}
