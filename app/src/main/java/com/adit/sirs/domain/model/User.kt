package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp

// FLOW: `User` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val photoUrl: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastLoginAt: Timestamp? = null
) {
    val isAdmin: Boolean get() = role == UserRole.ADMINISTRATOR
}
