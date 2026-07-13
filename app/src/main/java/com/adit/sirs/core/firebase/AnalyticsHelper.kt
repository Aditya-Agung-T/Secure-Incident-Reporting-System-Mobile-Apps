package com.adit.sirs.core.firebase

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `AnalyticsHelper` menyiapkan helper dan provider Firebase.
class AnalyticsHelper @Inject constructor() {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    // PRD: "Event yang boleh dicatat: login_success, report_created, report_status_viewed, upload_started, upload_success, upload_failed"
    // PRD: "Tidak boleh mencatat: title, description, location, email, admin response, secure_url"

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    fun logLoginSuccess(role: String) {
        val bundle = Bundle().apply {
            putString("role", role)
        }
        analytics.logEvent("login_success", bundle)
    }

    // FLOW: Fungsi `logReportCreated` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun logReportCreated(severity: String, hasAttachment: Boolean) {
        val bundle = Bundle().apply {
            putString("severity", severity)
            putBoolean("has_attachment", hasAttachment)
        }
        analytics.logEvent("report_created", bundle)
    }

    // FLOW: Fungsi `logReportStatusViewed` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun logReportStatusViewed(status: String) {
        val bundle = Bundle().apply {
            putString("status", status)
        }
        analytics.logEvent("report_status_viewed", bundle)
    }

    // FLOW: Fungsi `logUploadStarted` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun logUploadStarted(mimeType: String, sizeInBytes: Long) {
        val bundle = Bundle().apply {
            putString("mime_type", mimeType)
            putString("size_bucket", getSizeBucket(sizeInBytes))
        }
        analytics.logEvent("upload_started", bundle)
    }

    // FLOW: Fungsi `logUploadSuccess` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun logUploadSuccess(mimeType: String, sizeInBytes: Long) {
        val bundle = Bundle().apply {
            putString("mime_type", mimeType)
            putString("size_bucket", getSizeBucket(sizeInBytes))
        }
        analytics.logEvent("upload_success", bundle)
    }

    // FLOW: Fungsi `logUploadFailed` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun logUploadFailed(mimeType: String, sizeInBytes: Long, errorType: String) {
        val bundle = Bundle().apply {
            putString("mime_type", mimeType)
            putString("size_bucket", getSizeBucket(sizeInBytes))
            putString("error_type", errorType)
        }
        analytics.logEvent("upload_failed", bundle)
    }

    // FLOW: Fungsi `getSizeBucket` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    private fun getSizeBucket(size: Long): String {
        return when {
            size < 500_000 -> "<500KB"
            size < 1_000_000 -> "500KB-1MB"
            size < 2_000_000 -> "1MB-2MB"
            else -> ">2MB" // Shouldn't happen due to our 2MB limit, but good to catch
        }
    }
}
