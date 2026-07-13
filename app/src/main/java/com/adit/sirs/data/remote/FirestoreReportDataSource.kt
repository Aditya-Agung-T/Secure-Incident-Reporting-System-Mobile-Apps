package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.constants.AppConstants
import com.adit.sirs.data.model.IncidentReportDto
import com.adit.sirs.data.model.StatusHistoryDto
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `FirestoreReportDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FirestoreReportDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val reportsCollection get() = firestore.collection(AppConstants.COLLECTION_REPORTS)

    // FLOW: Membuat kode laporan unik agar laporan mudah dilacak oleh user dan admin.
    suspend fun generateReportCode(): String {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val snapshot = reportsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        val lastNumber = if (snapshot.isEmpty) {
            0
        } else {
            val lastCode = snapshot.documents[0].getString("reportCode") ?: ""
            val parts = lastCode.split("-")
            if (parts.size == 3) parts[2].toIntOrNull() ?: 0 else 0
        }

        return "INC-$year-%05d".format(lastNumber + 1)
    }

    // FLOW: Menambahkan reportCode/status awal lalu menyimpan laporan baru ke Firestore.
    suspend fun createReport(data: Map<String, Any?>): String {
        val docRef = reportsCollection.document()
        val reportData = data.toMutableMap()
        reportData["createdAt"] = Timestamp.now()
        reportData["updatedAt"] = Timestamp.now()
        docRef.set(reportData).await()
        return docRef.id
    }

    // FLOW: Menyimpan perubahan laporan yang diedit pengguna selama masih memenuhi aturan akses.
    suspend fun updateReport(reportId: String, data: Map<String, Any?>) {
        val updateData = data.toMutableMap()
        updateData["updatedAt"] = Timestamp.now()
        reportsCollection.document(reportId).update(updateData).await()
    }

    // FLOW: Menghapus laporan beserta attachment terkait, lalu mencatat aktivitas jika dipanggil admin/user.
    suspend fun deleteReport(reportId: String) {
        val reportRef = reportsCollection.document(reportId)
        val histories = reportRef
            .collection(AppConstants.SUBCOLLECTION_STATUS_HISTORIES)
            .get()
            .await()

        firestore.batch().apply {
            histories.documents.forEach { delete(it.reference) }
            delete(reportRef)
        }.commit().await()
    }

    // FLOW: Query laporan berdasarkan userId supaya user hanya melihat data miliknya.
    fun observeUserReports(userId: String, status: String? = null): Flow<List<IncidentReportDto>> = callbackFlow {
        var query: Query = reportsCollection
            .whereEqualTo("userId", userId)

        if (status != null) {
            query = query.whereEqualTo("status", status)
        }

        // Limit the results
        query = query.limit(AppConstants.REPORT_PAGE_SIZE.toLong())

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.mapNotNull {
                it.toObject(IncidentReportDto::class.java)?.copy(id = it.id)
            } ?: emptyList()
            
            // Client-side sorting to bypass Firebase Composite Index FAILED_PRECONDITION
            val sortedReports = reports.sortedByDescending { it.createdAt }
            trySend(sortedReports)
        }

        awaitClose { listener.remove() }
    }

    // FLOW: Query seluruh laporan untuk kebutuhan admin.
    fun observeAllReports(status: String? = null): Flow<List<IncidentReportDto>> = callbackFlow {
        var query: Query = reportsCollection

        if (status != null) {
            query = query.whereEqualTo("status", status)
        }

        // Limit the results
        query = query.limit(AppConstants.REPORT_PAGE_SIZE.toLong())

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.mapNotNull {
                it.toObject(IncidentReportDto::class.java)?.copy(id = it.id)
            } ?: emptyList()
            
            // Client-side sorting to bypass Firebase Composite Index FAILED_PRECONDITION
            val sortedReports = reports.sortedByDescending { it.createdAt }
            trySend(sortedReports)
        }

        awaitClose { listener.remove() }
    }

    // FLOW: Mendengarkan satu dokumen laporan agar detail berubah realtime.
    fun observeReport(reportId: String): Flow<IncidentReportDto?> = callbackFlow {
        val listener = reportsCollection.document(reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val report = snapshot?.toObject(IncidentReportDto::class.java)?.copy(id = snapshot.id)
                trySend(report)
            }

        awaitClose { listener.remove() }
    }

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
    ) {
        val batch = firestore.batch()

        val reportRef = reportsCollection.document(reportId)
        val now = Timestamp.now()
        val reportUpdates = mutableMapOf<String, Any?>(
            "status" to newStatus,
            "severity" to newSeverity,
            "updatedAt" to now
        )
        if (adminResponse != null) {
            reportUpdates["adminResponse"] = adminResponse
        }
        if (oldStatus == "pending") {
            reportUpdates["handledBy"] = adminUid
            reportUpdates["handledByName"] = adminName
            reportUpdates["handledAt"] = now
        }
        batch.update(reportRef, reportUpdates)

        val historyRef = reportRef.collection(AppConstants.SUBCOLLECTION_STATUS_HISTORIES).document()
        batch.set(historyRef, mapOf(
            "fromStatus" to oldStatus,
            "toStatus" to newStatus,
            "fromSeverity" to oldSeverity,
            "toSeverity" to newSeverity,
            "note" to adminResponse,
            "changedBy" to adminUid,
            "changedByName" to adminName,
            "createdAt" to now
        ))

        batch.commit().await()
    }

    // FLOW: Mendengarkan subcollection riwayat status agar perubahan admin terlihat di detail laporan.
    fun observeStatusHistories(reportId: String): Flow<List<StatusHistoryDto>> = callbackFlow {
        val listener = reportsCollection.document(reportId)
            .collection(AppConstants.SUBCOLLECTION_STATUS_HISTORIES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val histories = snapshot?.documents?.mapNotNull {
                    it.toObject(StatusHistoryDto::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(histories)
            }

        awaitClose { listener.remove() }
    }

    // FLOW: Fungsi `getReport` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun getReport(reportId: String): IncidentReportDto? {
        val doc = reportsCollection.document(reportId).get().await()
        return if (doc.exists()) doc.toObject(IncidentReportDto::class.java)?.copy(id = doc.id) else null
    }
}
