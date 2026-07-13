package com.adit.sirs.data.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.error.ErrorMapper
import com.adit.sirs.data.mapper.ReportMapper
import com.adit.sirs.data.mapper.StatusHistoryMapper
import com.adit.sirs.data.remote.CloudinaryUploadDataSource
import com.adit.sirs.data.remote.FirestoreReportDataSource
import com.adit.sirs.domain.model.Attachment
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.model.StatusHistory
import com.adit.sirs.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `ReportRepositoryImpl` berisi implementasi operasi domain dengan memanggil data source yang sesuai.
class ReportRepositoryImpl @Inject constructor(
    private val reportDataSource: FirestoreReportDataSource,
    private val cloudinaryDataSource: CloudinaryUploadDataSource
) : ReportRepository {

    // FLOW: Query laporan berdasarkan userId supaya user hanya melihat data miliknya.
    override fun observeUserReports(userId: String, status: String?): Flow<List<IncidentReport>> {
        return reportDataSource.observeUserReports(userId, status).map { dtos ->
            dtos.map { ReportMapper.toDomain(it) }
        }
    }

    // FLOW: Mendengarkan satu dokumen laporan agar detail berubah realtime.
    override fun observeReport(reportId: String): Flow<IncidentReport?> {
        return reportDataSource.observeReport(reportId).map { dto ->
            dto?.let { ReportMapper.toDomain(it) }
        }
    }

    // FLOW: Mendengarkan subcollection riwayat status agar perubahan admin terlihat di detail laporan.
    override fun observeStatusHistories(reportId: String): Flow<List<StatusHistory>> {
        return reportDataSource.observeStatusHistories(reportId).map { dtos ->
            dtos.map { StatusHistoryMapper.toDomain(it) }
        }
    }

    // FLOW: Menambahkan reportCode/status awal lalu menyimpan laporan baru ke Firestore.
    override suspend fun createReport(report: Map<String, Any?>): Result<String> {
        return try {
            val reportCode = reportDataSource.generateReportCode()
            val data = report.toMutableMap()
            data["reportCode"] = reportCode
            data["status"] = "pending"
            val id = reportDataSource.createReport(data)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Menyimpan perubahan laporan yang diedit pengguna selama masih memenuhi aturan akses.
    override suspend fun updateReport(reportId: String, data: Map<String, Any?>): Result<Unit> {
        return try {
            reportDataSource.updateReport(reportId, data)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Menghapus laporan beserta attachment terkait, lalu mencatat aktivitas jika dipanggil admin/user.
    override suspend fun deleteReport(reportId: String): Result<Unit> {
        return try {
            val report = reportDataSource.getReport(reportId)
            val attachment = report?.attachment?.let { ReportMapper.toDomain(report).attachment }

            if (attachment != null && attachment.publicId.isNotBlank()) {
                cloudinaryDataSource.deleteFile(
                    publicId = attachment.publicId,
                    resourceType = attachment.resourceType
                )
            }

            reportDataSource.deleteReport(reportId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Mengirim file valid ke Cloudinary dan mengembalikan metadata attachment untuk laporan.
    override suspend fun uploadAttachment(
        contentResolver: ContentResolver,
        uri: Uri,
        mimeType: String,
        fileName: String
    ): Result<Attachment> {
        return try {
            val attachment = cloudinaryDataSource.uploadFile(contentResolver, uri, mimeType, fileName)
            Result.Success(attachment)
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }

    // FLOW: Fungsi `getReport` menjalankan langkah khusus pada file ini dan menjaga alur menjembatani domain layer dengan data source Firebase atau Cloudinary.
    override suspend fun getReport(reportId: String): Result<IncidentReport?> {
        return try {
            val dto = reportDataSource.getReport(reportId)
            Result.Success(dto?.let { ReportMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(e, ErrorMapper.toUserMessage(e))
        }
    }
}
