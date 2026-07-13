package com.adit.sirs.data.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// FLOW: `UserDto` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val photoUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null
)
