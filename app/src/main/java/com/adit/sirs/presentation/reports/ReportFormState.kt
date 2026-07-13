package com.adit.sirs.presentation.reports

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.net.Uri
import com.adit.sirs.core.constants.AppConstants

// FLOW: `ReportFormState` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class ReportFormState(
    val title: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val description: String = "",
    val location: String = "",
    val incidentDate: Long? = null,
    val severity: String = "low",
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentSize: Long = 0
) {
    val titleError: String?
        get() = when {
            title.isBlank() -> "Judul wajib diisi"
            title.length < AppConstants.TITLE_MIN_LENGTH -> "Judul minimal ${AppConstants.TITLE_MIN_LENGTH} karakter"
            title.length > AppConstants.TITLE_MAX_LENGTH -> "Judul maksimal ${AppConstants.TITLE_MAX_LENGTH} karakter"
            else -> null
        }

    val descriptionError: String?
        get() = when {
            description.isBlank() -> "Deskripsi wajib diisi"
            description.length < AppConstants.DESCRIPTION_MIN_LENGTH -> "Deskripsi minimal ${AppConstants.DESCRIPTION_MIN_LENGTH} karakter"
            description.length > AppConstants.DESCRIPTION_MAX_LENGTH -> "Deskripsi maksimal ${AppConstants.DESCRIPTION_MAX_LENGTH} karakter"
            else -> null
        }

    val locationError: String?
        get() = when {
            location.isBlank() -> "Lokasi wajib diisi"
            location.length < AppConstants.LOCATION_MIN_LENGTH -> "Lokasi minimal ${AppConstants.LOCATION_MIN_LENGTH} karakter"
            location.length > AppConstants.LOCATION_MAX_LENGTH -> "Lokasi maksimal ${AppConstants.LOCATION_MAX_LENGTH} karakter"
            else -> null
        }

    val categoryError: String?
        get() = if (categoryId.isBlank()) "Kategori wajib dipilih" else null

    val dateError: String?
        get() = when {
            incidentDate == null -> "Tanggal insiden wajib diisi"
            incidentDate > System.currentTimeMillis() -> "Tanggal tidak boleh di masa depan"
            else -> null
        }

    val isValid: Boolean
        get() = titleError == null && descriptionError == null && locationError == null
                && categoryError == null && dateError == null && title.isNotBlank()
                && description.isNotBlank() && location.isNotBlank() && categoryId.isNotBlank()
                && incidentDate != null
}
