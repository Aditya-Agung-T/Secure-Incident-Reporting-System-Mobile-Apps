package com.adit.sirs.data.mapper

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.data.model.CategoryDto
import com.adit.sirs.domain.model.Category

// FLOW: `CategoryMapper` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object CategoryMapper {
    // FLOW: Mengubah bentuk data antar layer agar UI memakai model domain, bukan struktur Firestore mentah.
    fun toDomain(dto: CategoryDto): Category {
        val fallback = defaultGuidance(dto.slug.ifBlank { dto.name.lowercase() })
        return Category(
            id = dto.id,
            name = dto.name,
            slug = dto.slug,
            description = dto.description,
            mitigationTips = dto.mitigationTips.ifEmpty { fallback.first },
            recommendedEvidence = dto.recommendedEvidence.ifEmpty { fallback.second },
            isActive = dto.isActive,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            deletedAt = dto.deletedAt
        )
    }

    // FLOW: Fungsi `defaultGuidance` menjalankan langkah khusus pada file ini dan menjaga alur mengubah DTO dari database menjadi model domain yang dipakai aplikasi.
    private fun defaultGuidance(slugOrName: String): Pair<List<String>, List<String>> {
        val key = slugOrName.lowercase()
        return when {
            key.contains("phish") -> listOf(
                "Jangan klik tautan atau lampiran mencurigakan.",
                "Jangan memasukkan password pada halaman yang tidak resmi.",
                "Ganti password jika sudah terlanjur memasukkan kredensial."
            ) to listOf("Screenshot pesan/email", "Alamat pengirim", "Link mencurigakan", "Waktu kejadian")
            key.contains("malware") || key.contains("virus") -> listOf(
                "Putuskan koneksi internet perangkat terdampak.",
                "Jangan membuka ulang file/aplikasi mencurigakan.",
                "Jalankan pemindaian antivirus bila memungkinkan."
            ) to listOf("Nama file/aplikasi", "Screenshot peringatan", "Waktu kejadian", "Perangkat terdampak")
            key.contains("breach") || key.contains("data") || key.contains("bocor") -> listOf(
                "Identifikasi data yang berpotensi terdampak.",
                "Jangan menyebarkan data sensitif.",
                "Ganti kredensial akun terkait jika diperlukan."
            ) to listOf("Jenis data terdampak", "Sumber dugaan kebocoran", "Screenshot/log", "Waktu kejadian")
            key.contains("access") || key.contains("akses") -> listOf(
                "Ganti password akun terkait.",
                "Catat aktivitas login atau perubahan yang tidak dikenal.",
                "Aktifkan autentikasi tambahan bila tersedia."
            ) to listOf("Log aktivitas", "Screenshot notifikasi login", "Akun terdampak", "Waktu kejadian")
            key.contains("network") || key.contains("jaringan") -> listOf(
                "Catat perangkat atau jaringan yang terdampak.",
                "Hindari mengubah konfigurasi sebelum bukti dicatat.",
                "Laporkan waktu dan lokasi gangguan sejelas mungkin."
            ) to listOf("IP/perangkat terdampak", "Screenshot error", "Log jaringan", "Lokasi kejadian")
            else -> listOf(
                "Catat kronologi kejadian secara jelas.",
                "Simpan bukti sebelum melakukan perubahan.",
                "Laporkan dampak yang dirasakan pengguna atau sistem."
            ) to listOf("Screenshot/log", "Waktu kejadian", "Lokasi/unit terdampak", "Kronologi singkat")
        }
    }
}
