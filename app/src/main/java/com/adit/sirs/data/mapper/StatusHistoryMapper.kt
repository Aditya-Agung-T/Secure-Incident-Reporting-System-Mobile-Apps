package com.adit.sirs.data.mapper

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.model.StatusHistoryDto
import com.adit.sirs.domain.model.StatusHistory

// FLOW: `StatusHistoryMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object StatusHistoryMapper {
    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDomain(dto: StatusHistoryDto): StatusHistory {
        return StatusHistory(
            id = dto.id,
            fromStatus = dto.fromStatus,
            toStatus = dto.toStatus,
            note = dto.note,
            changedBy = dto.changedBy,
            changedByName = dto.changedByName,
            createdAt = dto.createdAt
        )
    }
}
