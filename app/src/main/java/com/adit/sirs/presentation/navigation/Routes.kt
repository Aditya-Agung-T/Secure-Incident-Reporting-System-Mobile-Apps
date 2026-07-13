package com.adit.sirs.presentation.navigation

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


// FLOW: `Routes` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val USER_DASHBOARD = "user_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val REPORT_LIST = "report_list"
    const val REPORT_DETAIL = "report_detail/{reportId}"
    const val CREATE_REPORT = "create_report"
    const val EDIT_REPORT = "edit_report/{reportId}"
    const val ADMIN_REPORT_LIST = "admin_report_list"
    const val ADMIN_REPORT_DETAIL = "admin_report_detail/{reportId}"
    const val ADMIN_CREATE_REPORT = "admin_create_report"
    const val CATEGORY_MANAGEMENT = "category_management"
    const val ACTIVITY_LOG = "activity_log"
    const val PROFILE = "profile"

    // FLOW: Fungsi `reportDetail` menjalankan langkah khusus pada file ini dan menjaga alur menghubungkan route aplikasi dengan layar yang harus dibuka.
    fun reportDetail(reportId: String) = "report_detail/$reportId"
    // FLOW: Fungsi `editReport` menjalankan langkah khusus pada file ini dan menjaga alur menghubungkan route aplikasi dengan layar yang harus dibuka.
    fun editReport(reportId: String) = "edit_report/$reportId"
    // FLOW: Fungsi `adminReportDetail` menjalankan langkah khusus pada file ini dan menjaga alur menghubungkan route aplikasi dengan layar yang harus dibuka.
    fun adminReportDetail(reportId: String) = "admin_report_detail/$reportId"
}
