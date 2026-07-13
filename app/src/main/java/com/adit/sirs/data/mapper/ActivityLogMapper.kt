package com.adit.sirs.data.mapper

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.model.ActivityLogDto
import com.adit.sirs.domain.model.ActivityLog

// FLOW: `ActivityLogMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object ActivityLogMapper {
    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDomain(dto: ActivityLogDto): ActivityLog {
        return ActivityLog(
            id = dto.id,
            actorId = dto.actorId,
            actorName = dto.actorName,
            actorRole = dto.actorRole,
            action = dto.action,
            entityType = dto.entityType,
            entityId = dto.entityId,
            description = dto.description,
            context = dto.context,
            createdAt = dto.createdAt
        )
    }
}
