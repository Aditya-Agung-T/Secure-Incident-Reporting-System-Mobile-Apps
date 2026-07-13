package com.adit.sirs.core.constants

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


// FLOW: `AppConstants` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object AppConstants {
    const val MAX_ATTACHMENT_SIZE_BYTES = 2 * 1024 * 1024L // 2 MB
    const val TITLE_MIN_LENGTH = 5
    const val TITLE_MAX_LENGTH = 150
    const val DESCRIPTION_MIN_LENGTH = 20
    const val DESCRIPTION_MAX_LENGTH = 4000
    const val LOCATION_MIN_LENGTH = 3
    const val LOCATION_MAX_LENGTH = 255
    const val REPORT_PAGE_SIZE = 20
    const val ACTIVITY_LOG_PAGE_SIZE = 50

    val ALLOWED_MIME_TYPES = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "application/pdf"
    )

    val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "pdf")

    // Firestore collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CATEGORIES = "categories"
    const val COLLECTION_REPORTS = "incidentReports"
    const val COLLECTION_ACTIVITY_LOGS = "activityLogs"
    const val COLLECTION_DEVICE_TOKENS = "deviceTokens"
    const val SUBCOLLECTION_STATUS_HISTORIES = "statusHistories"
}
