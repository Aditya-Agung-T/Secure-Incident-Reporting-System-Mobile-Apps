package com.adit.sirs.domain.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.common.Result
import com.adit.sirs.domain.model.ActivityLog
import com.adit.sirs.domain.model.IncidentReport
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // FLOW: Query seluruh laporan untuk kebutuhan admin.
    fun observeAllReports(status: String? = null): Flow<List<IncidentReport>>
    // FLOW: Admin mengubah status/severity/respon laporan lalu sistem membuat histori dan log aktivitas.
    suspend fun updateReportStatus(
        reportId: String,
        newStatus: String,
        newSeverity: String,
        adminResponse: String?,
        adminUid: String,
        adminName: String,
        oldStatus: String,
        oldSeverity: String
    ): Result<Unit>
    // FLOW: Fungsi `observeActivityLogs` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan kontrak operasi data agar ViewModel tidak bergantung langsung ke implementasi.
    fun observeActivityLogs(): Flow<List<ActivityLog>>
    // FLOW: Mencatat atau membaca audit trail aktivitas penting aplikasi.
    suspend fun logActivity(
        actorId: String?,
        actorName: String?,
        actorRole: String?,
        action: String,
        entityType: String?,
        entityId: String?,
        description: String,
        context: Map<String, Any>? = null
    ): Result<Unit>
}
