package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp

// FLOW: `Attachment` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class Attachment(
    val originalName: String = "",
    val publicId: String = "",
    val secureUrl: String = "",
    val resourceType: String = "",
    val format: String = "",
    val mimeType: String = "",
    val bytes: Long = 0,
    val uploadedAt: Timestamp? = null
)
