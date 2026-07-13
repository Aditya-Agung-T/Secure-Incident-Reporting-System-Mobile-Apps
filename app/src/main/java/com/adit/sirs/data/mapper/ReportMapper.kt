package com.adit.sirs.data.mapper

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.model.IncidentReportDto
import com.adit.sirs.domain.model.Attachment
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.domain.model.Severity
import com.google.firebase.Timestamp

// FLOW: `ReportMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object ReportMapper {
    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDomain(dto: IncidentReportDto): IncidentReport {
        return IncidentReport(
            id = dto.id,
            reportCode = dto.reportCode,
            slaDeadlineAt = dto.slaDeadlineAt,
            userId = dto.userId,
            userName = dto.userName,
            userEmail = dto.userEmail,
            categoryId = dto.categoryId,
            categoryName = dto.categoryName,
            title = dto.title,
            description = dto.description,
            location = dto.location,
            incidentDate = dto.incidentDate,
            severity = Severity.fromString(dto.severity),
            status = IncidentStatus.fromString(dto.status),
            adminResponse = dto.adminResponse,
            handledBy = dto.handledBy,
            handledByName = dto.handledByName,
            handledAt = dto.handledAt,
            attachment = dto.attachment?.let { mapAttachment(it) },
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    // FLOW: Fungsi `mapAttachment` menjalankan langkah khusus pada file ini dan menjaga alur mengubah DTO dari database menjadi model domain yang dipakai aplikasi.
    private fun mapAttachment(map: Map<String, Any>): Attachment {
        return Attachment(
            originalName = map["originalName"] as? String ?: "",
            publicId = map["publicId"] as? String ?: "",
            secureUrl = map["secureUrl"] as? String ?: "",
            resourceType = map["resourceType"] as? String ?: "",
            format = map["format"] as? String ?: "",
            mimeType = map["mimeType"] as? String ?: "",
            bytes = (map["bytes"] as? Number)?.toLong() ?: 0,
            uploadedAt = map["uploadedAt"] as? Timestamp
        )
    }

    // FLOW: Fungsi `attachmentToMap` menjalankan langkah khusus pada file ini dan menjaga alur mengubah DTO dari database menjadi model domain yang dipakai aplikasi.
    fun attachmentToMap(attachment: Attachment): Map<String, Any?> {
        return mapOf(
            "originalName" to attachment.originalName,
            "publicId" to attachment.publicId,
            "secureUrl" to attachment.secureUrl,
            "resourceType" to attachment.resourceType,
            "format" to attachment.format,
            "mimeType" to attachment.mimeType,
            "bytes" to attachment.bytes,
            "uploadedAt" to attachment.uploadedAt
        )
    }
}
