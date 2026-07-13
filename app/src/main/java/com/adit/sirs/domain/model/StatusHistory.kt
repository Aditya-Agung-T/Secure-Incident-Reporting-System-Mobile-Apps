package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp

// FLOW: `StatusHistory` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class StatusHistory(
    val id: String = "",
    val fromStatus: String? = null,
    val toStatus: String = "",
    val note: String? = null,
    val changedBy: String = "",
    val changedByName: String = "",
    val createdAt: Timestamp? = null
)
