package com.adit.sirs.core.common

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `RateLimiter` menyediakan state/hasil umum yang dipakai banyak layer.
class RateLimiter @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("rate_limiter", Context.MODE_PRIVATE)

    companion object {
        private const val MAX_REPORTS_PER_WINDOW = 10
        private const val REPORT_WINDOW_MS = 30 * 60 * 1000L // 30 menit

        private const val MAX_UPLOADS_PER_WINDOW = 10
        private const val UPLOAD_WINDOW_MS = 30 * 60 * 1000L // 30 menit

        private const val COOLDOWN_MS = 10_000L // 10 detik
    }

    // FLOW: Menambahkan reportCode/status awal lalu menyimpan laporan baru ke Firestore.
    fun canCreateReport(userId: String): RateLimitResult {
        return checkLimit(userId, "report", MAX_REPORTS_PER_WINDOW, REPORT_WINDOW_MS)
    }

    // FLOW: Mengirim file valid ke Cloudinary dan mengembalikan metadata attachment untuk laporan.
    fun canUploadFile(userId: String): RateLimitResult {
        return checkLimit(userId, "upload", MAX_UPLOADS_PER_WINDOW, UPLOAD_WINDOW_MS)
    }

    // FLOW: Fungsi `recordReport` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    fun recordReport(userId: String) {
        recordAction(userId, "report")
    }

    // FLOW: Fungsi `recordUpload` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    fun recordUpload(userId: String) {
        recordAction(userId, "upload")
    }

    // FLOW: Fungsi `getTimestamps` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    private fun getTimestamps(userId: String, action: String): MutableList<Long> {
        val key = "${userId}_${action}"
        val raw = prefs.getString(key, null) ?: return mutableListOf()
        return raw.split(",").mapNotNull { it.toLongOrNull() }.toMutableList()
    }

    // FLOW: Fungsi `saveTimestamps` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    private fun saveTimestamps(userId: String, action: String, timestamps: List<Long>) {
        val key = "${userId}_${action}"
        val now = System.currentTimeMillis()
        val cleaned = timestamps.filter { now - it < REPORT_WINDOW_MS * 2 }
        if (cleaned.isEmpty()) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putString(key, cleaned.joinToString(",")).apply()
        }
    }

    // FLOW: Fungsi `checkLimit` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    private fun checkLimit(userId: String, action: String, max: Int, windowMs: Long): RateLimitResult {
        val now = System.currentTimeMillis()
        val timestamps = getTimestamps(userId, action).filter { now - it < windowMs }

        // Cooldown: min 60s between actions
        val lastAction = timestamps.lastOrNull()
        if (lastAction != null && now - lastAction < COOLDOWN_MS) {
            val remainingSec = ((COOLDOWN_MS - (now - lastAction)) / 1000) + 1
            return RateLimitResult(
                allowed = false,
                message = "Mohon tunggu $remainingSec detik sebelum aksi berikutnya",
                remainingTimeSec = remainingSec.toInt()
            )
        }

        if (timestamps.size >= max) {
            val oldest = timestamps.first()
            val resetSec = ((windowMs - (now - oldest)) / 1000) + 1
            return RateLimitResult(
                allowed = false,
                message = "Anda sudah mencapai batas $max. Coba lagi $resetSec detik lagi.",
                remainingTimeSec = resetSec.toInt()
            )
        }

        return RateLimitResult(allowed = true)
    }

    // FLOW: Fungsi `recordAction` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan state/hasil umum yang dipakai banyak layer.
    private fun recordAction(userId: String, action: String) {
        val timestamps = getTimestamps(userId, action)
        timestamps.add(System.currentTimeMillis())
        saveTimestamps(userId, action, timestamps)
    }

    // FLOW: `RateLimitResult` adalah struktur data yang membawa informasi antarbagian aplikasi.
    data class RateLimitResult(
        val allowed: Boolean,
        val message: String? = null,
        val remainingTimeSec: Int = 0
    )
}
