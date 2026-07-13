package com.adit.sirs.presentation.reports

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.common.UiState
import com.adit.sirs.core.security.FileValidation
import com.adit.sirs.core.security.SlaCalculator
import com.adit.sirs.data.mapper.ReportMapper
import com.adit.sirs.domain.model.*
import com.adit.sirs.domain.repository.AdminRepository
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.domain.repository.CategoryRepository
import com.adit.sirs.domain.repository.ReportRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

import com.adit.sirs.core.firebase.AnalyticsHelper
import com.adit.sirs.core.common.RateLimiter

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `ReportViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    private val adminRepository: AdminRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val rateLimiter: RateLimiter
) : ViewModel() {

    // FLOW: `private val _formState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _formState = MutableStateFlow(ReportFormState())
    // FLOW: `val formState: StateFlow<ReportFormState>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()

    // FLOW: `private val _categories` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // FLOW: `val categories: StateFlow<List<Category>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // FLOW: `private val _userReports` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _userReports = MutableStateFlow<UiState<List<IncidentReport>>>(UiState.Loading)
    // FLOW: `val userReports: StateFlow<UiState<List<IncidentReport>>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val userReports: StateFlow<UiState<List<IncidentReport>>> = _userReports.asStateFlow()

    // FLOW: `private val _reportDetail` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _reportDetail = MutableStateFlow<UiState<IncidentReport>>(UiState.Loading)
    // FLOW: `val reportDetail: StateFlow<UiState<IncidentReport>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val reportDetail: StateFlow<UiState<IncidentReport>> = _reportDetail.asStateFlow()

    // FLOW: `private val _statusHistories` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _statusHistories = MutableStateFlow<List<StatusHistory>>(emptyList())
    // FLOW: `val statusHistories: StateFlow<List<StatusHistory>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val statusHistories: StateFlow<List<StatusHistory>> = _statusHistories.asStateFlow()

    // FLOW: `private val _submitState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _submitState = MutableStateFlow<UiState<String>>(UiState.Idle)
    // FLOW: `val submitState: StateFlow<UiState<String>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val submitState: StateFlow<UiState<String>> = _submitState.asStateFlow()

    // FLOW: `private val _uploadProgress` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _uploadProgress = MutableStateFlow(false)
    // FLOW: `val uploadProgress: StateFlow<Boolean>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val uploadProgress: StateFlow<Boolean> = _uploadProgress.asStateFlow()

    // FLOW: `private val _statusFilter` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _statusFilter = MutableStateFlow<String?>(null)
    // FLOW: `val statusFilter: StateFlow<String?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // FLOW: `private val _severityFilter` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _severityFilter = MutableStateFlow<String?>(null)
    // FLOW: `val severityFilter: StateFlow<String?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val severityFilter: StateFlow<String?> = _severityFilter.asStateFlow()

    // FLOW: `private var allLoadedReports: List<IncidentReport>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private var allLoadedReports: List<IncidentReport> = emptyList()

    // FLOW: `private var currentUser: User?` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private var currentUser: User? = null

    init {
        loadCategories()
        loadCurrentUser()
    }

    // FLOW: Mengecek user aktif sebagai titik awal penentuan dashboard dan hak akses.
    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    currentUser = result.data
                    // Auto-load reports when user is ready
                    loadUserReports()
                }
                else -> {}
            }
        }
    }

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.observeActiveCategories()
                .catch { _categories.value = emptyList() }
                .collect { _categories.value = it }
        }
    }

    // FLOW: Mengambil laporan milik user login secara realtime dari repository.
    fun loadUserReports(statusFilter: String? = null) {
        // FLOW: `val uid` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            reportRepository.observeUserReports(uid, statusFilter)
                .catch { _userReports.value = UiState.Error(it.message ?: "Failed to load reports") }
                .collect {
                    allLoadedReports = it
                    applyReportFilters()
                }
        }
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        loadUserReports(status)
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setSeverityFilter(severity: String?) {
        _severityFilter.value = severity
        applyReportFilters()
    }

    // FLOW: Fungsi `applyReportFilters` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
    private fun applyReportFilters() {
        val severity = _severityFilter.value
        val filtered = if (severity == null) {
            allLoadedReports
        } else {
            allLoadedReports.filter { it.severity.value == severity }
        }
        _userReports.value = UiState.Success(filtered)
    }

    // FLOW: Mengambil detail laporan beserta riwayat status agar layar detail selalu terbaru.
    fun loadReportDetail(reportId: String) {
        analyticsHelper.logReportStatusViewed("detail")
        viewModelScope.launch {
            reportRepository.observeReport(reportId)
                .catch { _reportDetail.value = UiState.Error(it.message ?: "Failed to load report") }
                .collect { report ->
                    if (report != null) {
                        _reportDetail.value = UiState.Success(report)
                    } else {
                        _reportDetail.value = UiState.Error("Report not found")
                    }
                }
        }
        viewModelScope.launch {
            reportRepository.observeStatusHistories(reportId)
                .catch { _statusHistories.value = emptyList() }
                .collect { _statusHistories.value = it }
        }
    }

    // FLOW: Fungsi `updateFormField` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
    fun updateFormField(update: ReportFormState.() -> ReportFormState) {
        _formState.value = _formState.value.update()
    }

    // FLOW: Memvalidasi file bukti sebelum disimpan ke form dan sebelum upload Cloudinary.
    fun setAttachment(
        contentResolver: ContentResolver,
        uri: Uri
    ) {
        val result = FileValidation.validateFile(contentResolver, uri)
        if (result.isValid) {
            _formState.value = _formState.value.copy(
                attachmentUri = uri,
                attachmentName = result.fileName,
                attachmentMimeType = result.mimeType,
                attachmentSize = result.fileSize
            )
        } else {
            _submitState.value = UiState.Error(result.errorMessage ?: "Invalid file")
        }
    }

    // FLOW: Menghapus pilihan attachment dari form tanpa mengubah data laporan lain.
    fun clearAttachment() {
        _formState.value = _formState.value.copy(
            attachmentUri = null,
            attachmentName = null,
            attachmentMimeType = null,
            attachmentSize = 0
        )
    }

    private var lastSuccessfulUpload: Attachment? = null

    // FLOW: Menjalankan submit laporan lengkap: validasi form, rate limit, upload bukti, SLA, simpan Firestore, dan activity log.
    fun submitReport(contentResolver: ContentResolver) {
        val form = _formState.value
        if (!form.isValid) {
            _submitState.value = UiState.Error("Lengkapi semua field wajib")
            return
        }

        // FLOW: `val user` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
        val user = currentUser ?: run {
            _submitState.value = UiState.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading

            // Rate limit check for report creation
            val rateCheck = rateLimiter.canCreateReport(user.uid)
            if (!rateCheck.allowed) {
                _submitState.value = UiState.Error(rateCheck.message ?: "Terlalu banyak permintaan")
                return@launch
            }

            // Upload attachment if present (reuse previous upload on retry)
            var attachment: Attachment? = lastSuccessfulUpload
            if (attachment == null && form.attachmentUri != null && form.attachmentMimeType != null && form.attachmentName != null) {
                val uploadRateCheck = rateLimiter.canUploadFile(user.uid)
                if (!uploadRateCheck.allowed) {
                    _submitState.value = UiState.Error(uploadRateCheck.message ?: "Mohon tunggu sebelum upload lagi")
                    return@launch
                }
                _uploadProgress.value = true
                analyticsHelper.logUploadStarted(form.attachmentMimeType, form.attachmentSize)
                when (val uploadResult = reportRepository.uploadAttachment(
                    contentResolver, form.attachmentUri, form.attachmentMimeType, form.attachmentName
                )) {
                    is Result.Success -> {
                        attachment = uploadResult.data
                        lastSuccessfulUpload = uploadResult.data
                        analyticsHelper.logUploadSuccess(form.attachmentMimeType, form.attachmentSize)
                    }
                    is Result.Error -> {
                        _uploadProgress.value = false
                        analyticsHelper.logUploadFailed(form.attachmentMimeType, form.attachmentSize, uploadResult.message ?: "Unknown")
                        _submitState.value = UiState.Error(uploadResult.message ?: "Upload failed")
                        return@launch
                    }
                    is Result.Loading -> {}
                }
                _uploadProgress.value = false
                rateLimiter.recordUpload(user.uid)
            }

            val submittedAt = Timestamp.now()
            val severity = Severity.fromString(form.severity)
            val reportData = mutableMapOf<String, Any?>(
                "slaDeadlineAt" to SlaCalculator.deadlineFrom(submittedAt, severity),
                "userId" to user.uid,
                "userName" to user.name,
                "userEmail" to user.email,
                "categoryId" to form.categoryId,
                "categoryName" to form.categoryName,
                "title" to form.title,
                "description" to form.description,
                "location" to form.location,
                "incidentDate" to Timestamp(Date(form.incidentDate!!)),
                "severity" to form.severity,
                "status" to "pending",
                "deletedAt" to null
            )

            if (attachment != null) {
                reportData["attachment"] = ReportMapper.attachmentToMap(attachment)
            }

            when (val result = reportRepository.createReport(reportData)) {
                is Result.Success -> {
                    lastSuccessfulUpload = null // clear cache on success
                    try {
                        adminRepository.logActivity(
                            actorId = user.uid,
                            actorName = user.name,
                            actorRole = user.role.value,
                            action = "report.created",
                            entityType = "incidentReport",
                            entityId = result.data,
                            description = "Report created: ${form.title}"
                        )
                        if (attachment != null) {
                            adminRepository.logActivity(
                                actorId = user.uid,
                                actorName = user.name,
                                actorRole = user.role.value,
                                action = "attachment.uploaded",
                                entityType = "incidentReport",
                                entityId = result.data,
                                description = "Attachment uploaded: ${attachment.originalName}"
                            )
                        }
                    } catch (_: Exception) { }

                    rateLimiter.recordReport(user.uid)
                    analyticsHelper.logReportCreated(form.severity, attachment != null)
                    _submitState.value = UiState.Success(result.data)
                    _formState.value = ReportFormState()
                }
                is Result.Error -> {
                    // If upload succeeded but Firestore failed, offer retry
                    android.util.Log.e("SIRS_REPORT", "Gagal Firestore: ${result.message}", (result as? Result.Error)?.exception)
                    if (attachment != null) {
                        _submitState.value = UiState.Error(
                            "Laporan gagal disimpan: ${result.message}. File sudah terupload."
                        )
                    } else {
                        _submitState.value = UiState.Error(result.message ?: "Gagal membuat laporan")
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Menyimpan perubahan laporan yang diedit pengguna selama masih memenuhi aturan akses.
    fun updateReport(reportId: String, contentResolver: ContentResolver) {
        val form = _formState.value
        if (!form.isValid) {
            _submitState.value = UiState.Error("Lengkapi semua field wajib")
            return
        }

        // FLOW: `val user` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
        val user = currentUser ?: run {
            _submitState.value = UiState.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading

            var attachment: Attachment? = null
            if (form.attachmentUri != null && form.attachmentMimeType != null && form.attachmentName != null) {
                _uploadProgress.value = true
                analyticsHelper.logUploadStarted(form.attachmentMimeType, form.attachmentSize)
                when (val uploadResult = reportRepository.uploadAttachment(
                    contentResolver, form.attachmentUri, form.attachmentMimeType, form.attachmentName
                )) {
                    is Result.Success -> {
                        attachment = uploadResult.data
                        analyticsHelper.logUploadSuccess(form.attachmentMimeType, form.attachmentSize)
                    }
                    is Result.Error -> {
                        _uploadProgress.value = false
                        _submitState.value = UiState.Error(uploadResult.message ?: "Upload failed")
                        return@launch
                    }
                    is Result.Loading -> {}
                }
                _uploadProgress.value = false
            }

            val updateData = mutableMapOf<String, Any?>(
                "categoryId" to form.categoryId,
                "categoryName" to form.categoryName,
                "title" to form.title,
                "description" to form.description,
                "location" to form.location,
                "incidentDate" to Timestamp(Date(form.incidentDate!!)),
                "severity" to form.severity
            )

            // Ensure deletedAt is not violated by accident, but we don't need to send it if rules don't require it on update
            if (attachment != null) {
                updateData["attachment"] = ReportMapper.attachmentToMap(attachment)
            }

            when (val result = reportRepository.updateReport(reportId, updateData)) {
                is Result.Success -> {
                    try {
                        adminRepository.logActivity(
                            actorId = user.uid,
                            actorName = user.name,
                            actorRole = user.role.value,
                            action = "report.updated",
                            entityType = "incidentReport",
                            entityId = reportId,
                            description = "Report updated: ${form.title}"
                        )
                    } catch (_: Exception) { }

                    _submitState.value = UiState.Success(reportId)
                }
                is Result.Error -> {
                    _submitState.value = UiState.Error(result.message ?: "Failed to update report")
                }
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Menghapus laporan beserta attachment terkait, lalu mencatat aktivitas jika dipanggil admin/user.
    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading
            when (val result = reportRepository.deleteReport(reportId)) {
                is Result.Success -> {
                    try {
                        // FLOW: `val user` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
                        val user = currentUser
                        if (user != null) {
                            adminRepository.logActivity(
                                actorId = user.uid,
                                actorName = user.name,
                                actorRole = user.role.value,
                                action = "report.deleted",
                                entityType = "incidentReport",
                                entityId = reportId,
                                description = "Report deleted"
                            )
                        }
                    } catch (_: Exception) { }
                    _submitState.value = UiState.Success("deleted")
                }
                is Result.Error -> {
                    _submitState.value = UiState.Error(result.message ?: "Failed to delete report")
                }
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Mengisi form edit dari data laporan yang sudah ada supaya user tidak mengetik ulang.
    fun prefillForm(report: IncidentReport) {
        _formState.value = ReportFormState(
            title = report.title,
            categoryId = report.categoryId,
            categoryName = report.categoryName,
            description = report.description,
            location = report.location,
            incidentDate = report.incidentDate?.toDate()?.time,
            severity = report.severity.value
        )
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }
}
