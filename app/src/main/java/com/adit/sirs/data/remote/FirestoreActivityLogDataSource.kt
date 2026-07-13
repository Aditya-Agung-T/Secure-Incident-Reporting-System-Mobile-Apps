package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.constants.AppConstants
import com.adit.sirs.data.model.ActivityLogDto
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
// FLOW: `FirestoreActivityLogDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FirestoreActivityLogDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val logsCollection get() = firestore.collection(AppConstants.COLLECTION_ACTIVITY_LOGS)

    // FLOW: Mencatat atau membaca audit trail aktivitas penting aplikasi.
    suspend fun createLog(
        actorId: String?,
        actorName: String?,
        actorRole: String?,
        action: String,
        entityType: String?,
        entityId: String?,
        description: String,
        context: Map<String, Any>? = null
    ) {
        val data = mapOf(
            "actorId" to actorId,
            "actorName" to actorName,
            "actorRole" to actorRole,
            "action" to action,
            "entityType" to entityType,
            "entityId" to entityId,
            "description" to description,
            "context" to context,
            "createdAt" to Timestamp.now()
        )
        logsCollection.document().set(data).await()
    }

    // FLOW: Fungsi `observeActivityLogs` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    fun observeActivityLogs(): Flow<List<ActivityLogDto>> = callbackFlow {
        val listener = logsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(AppConstants.ACTIVITY_LOG_PAGE_SIZE.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull {
                    it.toObject(ActivityLogDto::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(logs)
            }

        awaitClose { listener.remove() }
    }
}
