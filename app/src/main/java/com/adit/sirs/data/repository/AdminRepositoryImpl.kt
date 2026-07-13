package com.adit.sirs.data.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.common.Result
import com.adit.sirs.core.error.ErrorMapper
import com.adit.sirs.data.mapper.ActivityLogMapper
import com.adit.sirs.data.mapper.ReportMapper
import com.adit.sirs.data.remote.FirestoreActivityLogDataSource
import com.adit.sirs.data.remote.FirestoreReportDataSource
import com.adit.sirs.domain.model.ActivityLog
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `AdminRepositoryImpl` berisi implementasi operasi domain dengan memanggil data source yang sesuai.
class AdminRepositoryImpl @Inject constructor(
    private val reportDataSource: FirestoreReportDataSource,
    private val activityLogDataSource: FirestoreActivityLogDataSource
) : AdminRepository {

    // FLOW: Query seluruh laporan untuk kebutuhan admin.
    override fun observeAllReports(status: String?): Flow<List<IncidentReport>> {
        return reportDataSource.observeAllReports(status).map { dtos ->
            dtos.map { ReportMapper.toDomain(it) }
        }
    }

    // FLOW: Admin mengubah status/severity/respon laporan lalu sistem membuat histori dan log aktivitas.
    override suspend fun updateReportStatus(
        reportId: String,
        newStatus: String,
        newSeverity: String,
        adminResponse: String?,
        adminUid: String,
        adminName: String,
        oldStatus: String,
        oldSeverity: String
    ): Result<Unit> {
        return try {
            reportDataSource.updateReportStatus(
                reportId = reportId,
                newStatus = newStatus,
                newSeverity = newSeverity,
                adminResponse = adminResponse,
                adminUid = adminUid,
                adminName = adminName,
                oldStatus = oldStatus,
                oldSeverity = oldSeverity
            )

            if (newStatus != oldStatus) {
                try {
                    activityLogDataSource.createLog(
                        actorId = adminUid,
                        actorName = adminName,
                        actorRole = "administrator",
                        action = "report.status_updated",
                        entityType = "incidentReport",
                        entityId = reportId,
                        description = "Status updated from $oldStatus to $newStatus"
                    )
                } catch (_: Exception) { }
            }

            if (newSeverity != oldSeverity) {
                try {
                    activityLogDataSource.createLog(
                        actorId = adminUid,
                        actorName = adminName,
                        actorRole = "administrator",
                        action = "report.severity_updated",
                        entityType = "incidentReport",
                        entityId = reportId,
                        description = "Severity updated from $oldSeverity to $newSeverity"
                    )
                } catch (_: Exception) { }
            }

            // Log response_updated if status/severity also changed (already handled in ViewModel for metadata-same case)
            if ((newStatus != oldStatus || newSeverity != oldSeverity) && adminResponse != null) {
                try {
                    activityLogDataSource.createLog(
                        actorId = adminUid,
                        actorName = adminName,
                        actorRole = "administrator",
                        action = "report.response_updated",
                        entityType = "incidentReport",
                        entityId = reportId,
                        description = "Admin response added during report update"
                    )
                } catch (_: Exception) { }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Fungsi `observeActivityLogs` menjalankan langkah khusus pada file ini dan menjaga alur menjembatani domain layer dengan data source Firebase atau Cloudinary.
    override fun observeActivityLogs(): Flow<List<ActivityLog>> {
        return activityLogDataSource.observeActivityLogs().map { dtos ->
            dtos.map { ActivityLogMapper.toDomain(it) }
        }
    }

    // FLOW: Mencatat atau membaca audit trail aktivitas penting aplikasi.
    override suspend fun logActivity(
        actorId: String?,
        actorName: String?,
        actorRole: String?,
        action: String,
        entityType: String?,
        entityId: String?,
        description: String,
        context: Map<String, Any>?
    ): Result<Unit> {
        return try {
            activityLogDataSource.createLog(actorId, actorName, actorRole, action, entityType, entityId, description, context)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }
}
