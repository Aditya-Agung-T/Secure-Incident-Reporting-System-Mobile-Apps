package com.adit.sirs.data.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

// FLOW: `IncidentReportDto` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class IncidentReportDto(
    val id: String = "",
    val reportCode: String = "",
    val slaDeadlineAt: Timestamp? = null,
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val incidentDate: Timestamp? = null,
    val severity: String = "low",
    val status: String = "pending",
    val adminResponse: String? = null,
    val handledBy: String? = null,
    val handledByName: String? = null,
    val handledAt: Timestamp? = null,
    val attachment: Map<String, Any>? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
