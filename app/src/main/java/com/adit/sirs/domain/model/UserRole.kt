package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


// FLOW: `UserRole` membatasi nilai pilihan agar status/role/severity konsisten di seluruh aplikasi.
enum class UserRole(val value: String) {
    USER("user"),
    ADMINISTRATOR("administrator");

    companion object {
        // FLOW: Fungsi `fromString` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan model bisnis utama yang dipakai lintas layer.
        fun fromString(value: String): UserRole {
            return entries.firstOrNull { it.value == value } ?: USER
        }
    }
}
