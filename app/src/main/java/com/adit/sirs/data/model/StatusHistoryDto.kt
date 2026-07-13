package com.adit.sirs.data.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

// FLOW: `StatusHistoryDto` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class StatusHistoryDto(
    val id: String = "",
    val fromStatus: String? = null,
    val toStatus: String = "",
    val note: String? = null,
    val changedBy: String = "",
    val changedByName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
