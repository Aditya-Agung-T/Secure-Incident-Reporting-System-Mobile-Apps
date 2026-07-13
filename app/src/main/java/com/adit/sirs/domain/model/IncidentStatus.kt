package com.adit.sirs.domain.model

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


// FLOW: `IncidentStatus` membatasi nilai pilihan agar status/role/severity konsisten di seluruh aplikasi.
enum class IncidentStatus(val value: String, val displayName: String) {
    PENDING("pending", "Pending"),
    INVESTIGATING("investigating", "Investigating"),
    RESOLVED("resolved", "Resolved"),
    REJECTED("rejected", "Rejected");

    companion object {
        // FLOW: Fungsi `fromString` menjalankan langkah khusus pada file ini dan menjaga alur mendefinisikan model bisnis utama yang dipakai lintas layer.
        fun fromString(value: String): IncidentStatus {
            return entries.firstOrNull { it.value == value } ?: PENDING
        }
    }
}
