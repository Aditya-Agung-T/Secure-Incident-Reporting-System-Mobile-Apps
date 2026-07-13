package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp

// FLOW: `IncidentReport` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class IncidentReport(
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
    val severity: Severity = Severity.LOW,
    val status: IncidentStatus = IncidentStatus.PENDING,
    val adminResponse: String? = null,
    val handledBy: String? = null,
    val handledByName: String? = null,
    val handledAt: Timestamp? = null,
    val attachment: Attachment? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val isPending: Boolean get() = status == IncidentStatus.PENDING
    val hasAttachment: Boolean get() = attachment != null
}
