package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.core.constants.AppConstants
import com.adit.sirs.data.model.CategoryDto
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
// FLOW: `FirestoreCategoryDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class FirestoreCategoryDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val categoriesCollection get() = firestore.collection(AppConstants.COLLECTION_CATEGORIES)

    // FLOW: Fungsi `seedDefaultCategoriesIfEmpty` menjalankan langkah khusus pada file ini dan menjaga alur berkomunikasi langsung dengan layanan eksternal seperti Firebase, Firestore, FCM, atau Cloudinary.
    suspend fun seedDefaultCategoriesIfEmpty() {
        val snapshot = categoriesCollection.limit(1).get().await()
        if (!snapshot.isEmpty) return

        val categories = listOf(
            "Data Breach" to "data-breach",
            "Malware/Virus" to "malware-virus",
            "Phishing" to "phishing",
            "Unauthorized Access" to "unauthorized-access",
            "Network Intrusion" to "network-intrusion",
            "Physical Security" to "physical-security",
            "Social Engineering" to "social-engineering",
            "Denial of Service" to "denial-of-service",
            "Other" to "other"
        )
        val defaultMitigationTips = mapOf(
            "data-breach" to listOf("Identifikasi data yang berpotensi bocor.", "Ganti kredensial akun terkait.", "Batasi akses akun yang terdampak."),
            "malware-virus" to listOf("Putuskan koneksi jaringan perangkat.", "Jangan membuka file mencurigakan.", "Jalankan pemindaian antivirus."),
            "phishing" to listOf("Jangan klik tautan mencurigakan.", "Jangan masukkan password pada halaman tidak resmi.", "Simpan screenshot email atau pesan sebagai bukti."),
            "unauthorized-access" to listOf("Ganti password akun terkait.", "Periksa aktivitas login terakhir.", "Laporkan perangkat atau lokasi login mencurigakan."),
            "network-intrusion" to listOf("Catat waktu dan jaringan yang terdampak.", "Putuskan akses perangkat mencurigakan bila memungkinkan.", "Laporkan alamat IP atau log jaringan yang relevan."),
            "physical-security" to listOf("Amankan lokasi kejadian.", "Catat orang atau perangkat yang terlibat.", "Simpan foto atau bukti pendukung."),
            "social-engineering" to listOf("Jangan membagikan OTP atau kredensial.", "Catat nomor/email pelaku.", "Laporkan kronologi percakapan."),
            "denial-of-service" to listOf("Catat waktu layanan mulai terganggu.", "Simpan screenshot error atau log akses.", "Laporkan layanan yang tidak dapat digunakan."),
            "other" to listOf("Catat kronologi kejadian dengan jelas.", "Simpan bukti pendukung yang tersedia.", "Laporkan dampak yang dirasakan.")
        )
        val defaultEvidence = mapOf(
            "data-breach" to listOf("Jenis data terdampak", "Screenshot atau notifikasi kebocoran", "Waktu kejadian"),
            "malware-virus" to listOf("Nama file/aplikasi mencurigakan", "Screenshot peringatan antivirus", "Waktu kejadian"),
            "phishing" to listOf("Screenshot email/pesan", "Alamat pengirim", "Link mencurigakan"),
            "unauthorized-access" to listOf("Screenshot aktivitas login", "Waktu akses mencurigakan", "Akun yang terdampak"),
            "network-intrusion" to listOf("Alamat IP/log jaringan", "Waktu kejadian", "Sistem atau jaringan terdampak"),
            "physical-security" to listOf("Foto lokasi/bukti", "Waktu kejadian", "Perangkat atau area terdampak"),
            "social-engineering" to listOf("Screenshot percakapan", "Nomor/email pelaku", "Kronologi kejadian"),
            "denial-of-service" to listOf("Screenshot error layanan", "Waktu gangguan", "Nama layanan terdampak"),
            "other" to listOf("Screenshot/foto bukti", "Waktu kejadian", "Kronologi singkat")
        )
        for ((name, slug) in categories) {
            categoriesCollection.add(
                mapOf(
                    "name" to name,
                    "slug" to slug,
                    "description" to null,
                    "mitigationTips" to (defaultMitigationTips[slug] ?: emptyList<String>()),
                    "recommendedEvidence" to (defaultEvidence[slug] ?: emptyList<String>()),
                    "isActive" to true,
                    "createdBy" to "system",
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
            ).await()
        }
    }

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    fun observeActiveCategories(): Flow<List<CategoryDto>> = callbackFlow {
        val listener = categoriesCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull {
                    it.toObject(CategoryDto::class.java)?.copy(id = it.id)
                }?.sortedBy { it.name } ?: emptyList()
                trySend(categories)
            }

        awaitClose { listener.remove() }
    }

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    fun observeAllCategories(): Flow<List<CategoryDto>> = callbackFlow {
        val listener = categoriesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull {
                    it.toObject(CategoryDto::class.java)?.copy(id = it.id)
                }?.filter { it.deletedAt == null }
                    ?.sortedBy { it.name }
                    ?: emptyList()
                trySend(categories)
            }

        awaitClose { listener.remove() }
    }

    // FLOW: Admin membuat kategori laporan baru agar user dapat memilihnya di form.
    suspend fun createCategory(name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>, createdBy: String): String {
        val docRef = categoriesCollection.document()
        val data = mapOf(
            "name" to name,
            "slug" to slug,
            "description" to description,
            "mitigationTips" to mitigationTips,
            "recommendedEvidence" to recommendedEvidence,
            "isActive" to true,
            "createdBy" to createdBy,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )
        docRef.set(data).await()
        return docRef.id
    }

    // FLOW: Admin memperbarui nama/deskripsi/panduan kategori yang sudah ada.
    suspend fun updateCategory(categoryId: String, name: String, slug: String, description: String?, mitigationTips: List<String>, recommendedEvidence: List<String>) {
        categoriesCollection.document(categoryId).update(
            mapOf(
                "name" to name,
                "slug" to slug,
                "description" to description,
                "mitigationTips" to mitigationTips,
                "recommendedEvidence" to recommendedEvidence,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    // FLOW: Admin mengaktifkan atau menonaktifkan kategori tanpa menghapus datanya.
    suspend fun toggleCategoryActive(categoryId: String, isActive: Boolean) {
        categoriesCollection.document(categoryId).update(
            mapOf(
                "isActive" to isActive,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    // FLOW: Admin menghapus kategori dan mencatat aktivitas pengelolaan kategori.
    suspend fun deleteCategory(categoryId: String) {
        categoriesCollection.document(categoryId).update(
            mapOf(
                "isActive" to false,
                "deletedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }
}
