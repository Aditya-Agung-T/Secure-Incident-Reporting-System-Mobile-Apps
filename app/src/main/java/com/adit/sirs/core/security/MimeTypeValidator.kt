package com.adit.sirs.core.security

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.constants.AppConstants

// FLOW: `MimeTypeValidator` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object MimeTypeValidator {
    // FLOW: Fungsi `isAllowed` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    fun isAllowed(mimeType: String?): Boolean {
        return mimeType != null && mimeType in AppConstants.ALLOWED_MIME_TYPES
    }

    // FLOW: Fungsi `getExtensionFromMime` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    fun getExtensionFromMime(mimeType: String): String? {
        return when (mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "application/pdf" -> "pdf"
            else -> null
        }
    }
}
