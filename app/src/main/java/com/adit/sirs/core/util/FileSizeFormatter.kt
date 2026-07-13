package com.adit.sirs.core.util

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


// FLOW: `FileSizeFormatter` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object FileSizeFormatter {
    // FLOW: Fungsi `format` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun format(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }
}
