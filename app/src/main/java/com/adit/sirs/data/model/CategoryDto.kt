package com.adit.sirs.data.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// FLOW: `CategoryDto` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val mitigationTips: List<String> = emptyList(),
    val recommendedEvidence: List<String> = emptyList(),
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null
)
