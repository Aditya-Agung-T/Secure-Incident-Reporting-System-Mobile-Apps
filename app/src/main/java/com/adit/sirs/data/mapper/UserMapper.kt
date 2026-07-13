package com.adit.sirs.data.mapper

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.model.UserDto
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.model.UserRole

// FLOW: `UserMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object UserMapper {
    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDomain(dto: UserDto): User {
        return User(
            uid = dto.uid,
            name = dto.name,
            email = dto.email,
            role = UserRole.fromString(dto.role),
            isActive = dto.isActive,
            photoUrl = dto.photoUrl,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            lastLoginAt = dto.lastLoginAt
        )
    }

    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDto(user: User): UserDto {
        return UserDto(
            uid = user.uid,
            name = user.name,
            email = user.email,
            role = user.role.value,
            isActive = user.isActive,
            photoUrl = user.photoUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastLoginAt = user.lastLoginAt
        )
    }
}
