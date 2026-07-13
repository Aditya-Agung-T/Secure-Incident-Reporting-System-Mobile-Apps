package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp

// FLOW: `Category` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class Category(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val mitigationTips: List<String> = emptyList(),
    val recommendedEvidence: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null
)
