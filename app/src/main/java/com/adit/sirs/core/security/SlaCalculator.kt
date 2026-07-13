package com.adit.sirs.core.security

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.adit.sirs.domain.model.Severity
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

// FLOW: `SlaCalculator` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object SlaCalculator {
    // FLOW: Menghitung jam maksimal (SLA) berdasarkan tingkat keparahan insiden.
    fun hoursFor(severity: Severity): Long = when (severity) {
        Severity.CRITICAL -> 24L
        Severity.HIGH -> 48L
        Severity.MEDIUM -> 72L
        Severity.LOW -> 120L
    }

    // FLOW: Menghitung tanggal dan jam pasti (Timestamp) batas akhir penanganan berdasarkan waktu submit.
    fun deadlineFrom(submittedAt: Timestamp, severity: Severity): Timestamp {
        val deadlineMillis = submittedAt.toDate().time + TimeUnit.HOURS.toMillis(hoursFor(severity))
        return Timestamp(java.util.Date(deadlineMillis))
    }

    // FLOW: Memeriksa apakah waktu penanganan sudah melewati tenggat waktu (Overdue).
    fun isOverdue(deadline: Timestamp?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return deadline != null && deadline.toDate().time < nowMillis
    }

    // FLOW: Memeriksa apakah waktu penanganan sudah mendekati tenggat waktu (kurang dari 3 jam).
    fun isNearDeadline(deadline: Timestamp?, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (deadline == null) return false
        val diff = deadline.toDate().time - nowMillis
        return diff in 0..TimeUnit.HOURS.toMillis(3)
    }

    // FLOW: Memformat sisa waktu SLA menjadi teks yang mudah dibaca pengguna (hari, jam, menit, detik).
    fun formatRemaining(deadline: Timestamp?, nowMillis: Long = System.currentTimeMillis()): String {
        if (deadline == null) return "SLA belum tersedia"
        val diff = deadline.toDate().time - nowMillis
        val abs = kotlin.math.abs(diff)
        val days = TimeUnit.MILLISECONDS.toDays(abs)
        val hours = TimeUnit.MILLISECONDS.toHours(abs) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(abs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(abs) % 60
        val text = when {
            days > 0 -> "${days} hari ${hours} jam ${minutes} menit ${seconds} detik"
            hours > 0 -> "${hours} jam ${minutes} menit ${seconds} detik"
            else -> "${minutes} menit ${seconds} detik"
        }
        return if (diff >= 0) "Sisa waktu: $text" else "Terlambat: $text"
    }
}
