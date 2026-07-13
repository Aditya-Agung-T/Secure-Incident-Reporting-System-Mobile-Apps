package com.adit.sirs.domain.repository

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import com.adit.sirs.core.common.Result
import com.adit.sirs.domain.model.Attachment
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.model.StatusHistory
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    // FLOW: Query laporan berdasarkan userId supaya user hanya melihat data miliknya.
    fun observeUserReports(userId: String, status: String? = null): Flow<List<IncidentReport>>
    // FLOW: Mendengarkan satu dokumen laporan agar detail berubah realtime.
    fun observeReport(reportId: String): Flow<IncidentReport?>
    // FLOW: Mendengarkan subcollection riwayat status agar perubahan admin terlihat di detail laporan.
    fun observeStatusHistories(reportId: String): Flow<List<StatusHistory>>
    // FLOW: Menambahkan reportCode/status awal lalu menyimpan laporan baru ke Firestore.
    suspend fun createReport(report: Map<String, Any?>): Result<String>
    // FLOW: Menyimpan perubahan laporan yang diedit pengguna selama masih memenuhi aturan akses.
    suspend fun updateReport(reportId: String, data: Map<String, Any?>): Result<Unit>
    // FLOW: Menghapus laporan beserta attachment terkait, lalu mencatat aktivitas jika dipanggil admin/user.
    suspend fun deleteReport(reportId: String): Result<Unit>
    // FLOW: Mengirim file valid ke Cloudinary dan mengembalikan metadata attachment untuk laporan.
    suspend fun uploadAttachment(
        contentResolver: ContentResolver,
        uri: Uri,
        mimeType: String,
        fileName: String
    ): Result<Attachment>
    // FLOW: Fungsi `getReport` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan kontrak operasi data agar ViewModel tidak bergantung langsung ke implementasi.
    suspend fun getReport(reportId: String): Result<IncidentReport?>
}
