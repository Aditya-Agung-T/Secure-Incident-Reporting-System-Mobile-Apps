package com.adit.sirs.data.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

// FLOW: `ActivityLogDto` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class ActivityLogDto(
    val id: String = "",
    val actorId: String? = null,
    val actorName: String? = null,
    val actorRole: String? = null,
    val action: String = "",
    val entityType: String? = null,
    val entityId: String? = null,
    val description: String = "",
    val context: Map<String, Any>? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
