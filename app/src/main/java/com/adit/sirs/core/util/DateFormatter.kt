package com.adit.sirs.core.util

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// FLOW: `DateFormatter` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object DateFormatter {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // FLOW: Fungsi `formatTimestamp` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "-"
        return displayFormat.format(timestamp.toDate())
    }

    // FLOW: Fungsi `formatDate` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun formatDate(date: Date?): String {
        if (date == null) return "-"
        return dateOnlyFormat.format(date)
    }

    // FLOW: Fungsi `formatDateOnly` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun formatDateOnly(timestamp: Timestamp?): String {
        if (timestamp == null) return "-"
        return dateOnlyFormat.format(timestamp.toDate())
    }

    // FLOW: Fungsi `formatIso` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    fun formatIso(date: Date?): String {
        if (date == null) return ""
        return isoFormat.format(date)
    }
}
