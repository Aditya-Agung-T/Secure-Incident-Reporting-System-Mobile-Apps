package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.ActivityLog
import com.adit.sirs.domain.model.Category
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.model.StatusHistory
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AdminRepository
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.domain.repository.CategoryRepository
import com.adit.sirs.domain.repository.ReportRepository
import com.adit.sirs.core.firebase.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `AdminViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val categoryRepository: CategoryRepository,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    // FLOW: `private val _allReports` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _allReports = MutableStateFlow<UiState<List<IncidentReport>>>(UiState.Loading)
    // FLOW: `val allReports: StateFlow<UiState<List<IncidentReport>>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val allReports: StateFlow<UiState<List<IncidentReport>>> = _allReports.asStateFlow()

    // FLOW: `private val _reportDetail` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _reportDetail = MutableStateFlow<UiState<IncidentReport>>(UiState.Loading)
    // FLOW: `val reportDetail: StateFlow<UiState<IncidentReport>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val reportDetail: StateFlow<UiState<IncidentReport>> = _reportDetail.asStateFlow()

    // FLOW: `private val _statusHistories` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _statusHistories = MutableStateFlow<List<StatusHistory>>(emptyList())
    // FLOW: `val statusHistories: StateFlow<List<StatusHistory>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val statusHistories: StateFlow<List<StatusHistory>> = _statusHistories.asStateFlow()

    // FLOW: `private val _categories` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    // FLOW: `val categories: StateFlow<UiState<List<Category>>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()

    // FLOW: `private val _activityLogs` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _activityLogs = MutableStateFlow<UiState<List<ActivityLog>>>(UiState.Loading)
    // FLOW: `val activityLogs: StateFlow<UiState<List<ActivityLog>>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val activityLogs: StateFlow<UiState<List<ActivityLog>>> = _activityLogs.asStateFlow()

    // FLOW: `private val _updateState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    // FLOW: `val updateState: StateFlow<UiState<Unit>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    // FLOW: `private val _statusFilter` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _statusFilter = MutableStateFlow<String?>(null)
    // FLOW: `val statusFilter: StateFlow<String?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // FLOW: `private val _searchQuery` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _searchQuery = MutableStateFlow("")
    // FLOW: `val searchQuery: StateFlow<String>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // FLOW: `private val _severityFilter` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _severityFilter = MutableStateFlow<String?>(null)
    // FLOW: `val severityFilter: StateFlow<String?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val severityFilter: StateFlow<String?> = _severityFilter.asStateFlow()

    // FLOW: `private val _dateStart` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _dateStart = MutableStateFlow<Long?>(null)
    // FLOW: `val dateStart: StateFlow<Long?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val dateStart: StateFlow<Long?> = _dateStart.asStateFlow()

    // FLOW: `private val _dateEnd` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _dateEnd = MutableStateFlow<Long?>(null)
    // FLOW: `val dateEnd: StateFlow<Long?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val dateEnd: StateFlow<Long?> = _dateEnd.asStateFlow()

    // FLOW: `private var allLoadedReports: List<IncidentReport>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private var allLoadedReports: List<IncidentReport> = emptyList()

    // FLOW: `private var currentAdmin: User?` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private var currentAdmin: User? = null

    init {
        loadCurrentUser()
    }

    // FLOW: Mengecek user aktif sebagai titik awal penentuan dashboard dan hak akses.
    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    currentAdmin = result.data
                    loadAllReports()
                }
                else -> {}
            }
        }
    }

    // FLOW: Mengambil seluruh laporan untuk dashboard atau daftar admin.
    fun loadAllReports(statusFilter: String? = null) {
        viewModelScope.launch {
            adminRepository.observeAllReports(statusFilter)
                .catch { _allReports.value = UiState.Error(it.message ?: "Failed to load reports") }
                .collect { reports ->
                    allLoadedReports = reports
                    applyFilters()
                }
        }
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setSeverityFilter(severity: String?) {
        _severityFilter.value = severity
        applyFilters()
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setDateRange(start: Long?, end: Long?) {
        _dateStart.value = start
        _dateEnd.value = end
        applyFilters()
    }

    // FLOW: Fungsi `clearDateRange` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
    fun clearDateRange() {
        _dateStart.value = null
        _dateEnd.value = null
        applyFilters()
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        loadAllReports(status)
    }

    // FLOW: Mengubah kriteria filter lalu menyusun ulang daftar laporan yang tampil di UI.
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val severity = _severityFilter.value
        val dateStart = _dateStart.value
        val dateEnd = _dateEnd.value
        var filtered = allLoadedReports

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.reportCode.lowercase().contains(query) ||
                it.userName.lowercase().contains(query) ||
                it.location.lowercase().contains(query)
            }
        }

        if (severity != null) {
            filtered = filtered.filter { it.severity.value == severity }
        }

        if (dateStart != null) {
            filtered = filtered.filter {
                it.createdAt?.toDate()?.time?.let { ts -> ts >= dateStart } ?: true
            }
        }

        if (dateEnd != null) {
            filtered = filtered.filter {
                it.createdAt?.toDate()?.time?.let { ts -> ts <= dateEnd } ?: true
            }
        }

        _allReports.value = UiState.Success(filtered)
    }

    // FLOW: Mengambil detail laporan beserta riwayat status agar layar detail selalu terbaru.
    fun loadReportDetail(reportId: String) {
        analyticsHelper.logReportStatusViewed("detail")
        viewModelScope.launch {
            reportRepository.observeReport(reportId)
                .catch { _reportDetail.value = UiState.Error(it.message ?: "Failed") }
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

    // FLOW: Admin mengubah status/severity/respon laporan lalu sistem membuat histori dan log aktivitas.
    fun updateReportStatus(
        reportId: String,
        newStatus: String,
        newSeverity: String,
        adminResponse: String?,
        oldStatus: String,
        oldSeverity: String
    ) {
        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
        val admin = currentAdmin ?: return
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = adminRepository.updateReportStatus(
                reportId = reportId,
                newStatus = newStatus,
                newSeverity = newSeverity,
                adminResponse = adminResponse,
                adminUid = admin.uid,
                adminName = admin.name,
                oldStatus = oldStatus,
                oldSeverity = oldSeverity
            )) {
                is Result.Success -> {
                    // Log response_updated separately if only response changed
                    if (newStatus == oldStatus && newSeverity == oldSeverity && adminResponse != null) {
                        try {
                            adminRepository.logActivity(
                                actorId = admin.uid,
                                actorName = admin.name,
                                actorRole = "administrator",
                                action = "report.response_updated",
                                entityType = "incidentReport",
                                entityId = reportId,
                                description = "Admin response updated"
                            )
                        } catch (_: Exception) { }
                    }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Failed to update")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Memuat kategori dari Firestore untuk form user atau halaman manajemen admin.
    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.observeAllCategories()
                .catch { _categories.value = UiState.Error(it.message ?: "Failed") }
                .collect { _categories.value = UiState.Success(it) }
        }
    }

    // FLOW: Admin membuat kategori laporan baru agar user dapat memilihnya di form.
    fun createCategory(
        name: String,
        description: String?,
        mitigationTips: List<String> = emptyList(),
        recommendedEvidence: List<String> = emptyList()
    ) {
        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
        val admin = currentAdmin ?: return
        val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = categoryRepository.createCategory(name, slug, description, mitigationTips, recommendedEvidence, admin.uid)) {
                is Result.Success -> {
                    try {
                        adminRepository.logActivity(
                            actorId = admin.uid,
                            actorName = admin.name,
                            actorRole = "administrator",
                            action = "category.created",
                            entityType = "category",
                            entityId = result.data,
                            description = "Category created: $name"
                        )
                    } catch (_: Exception) { }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Failed")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Admin memperbarui nama/deskripsi/panduan kategori yang sudah ada.
    fun updateCategory(
        categoryId: String,
        name: String,
        description: String?,
        mitigationTips: List<String> = emptyList(),
        recommendedEvidence: List<String> = emptyList()
    ) {
        val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = categoryRepository.updateCategory(categoryId, name, slug, description, mitigationTips, recommendedEvidence)) {
                is Result.Success -> {
                    try {
                        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
                        val admin = currentAdmin
                        if (admin != null) {
                            adminRepository.logActivity(
                                actorId = admin.uid,
                                actorName = admin.name,
                                actorRole = "administrator",
                                action = "category.updated",
                                entityType = "category",
                                entityId = categoryId,
                                description = "Category updated: $name"
                            )
                        }
                    } catch (_: Exception) { }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Failed")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Admin mengaktifkan atau menonaktifkan kategori tanpa menghapus datanya.
    fun toggleCategory(categoryId: String, isActive: Boolean) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = categoryRepository.toggleCategoryActive(categoryId, isActive)) {
                is Result.Success -> {
                    try {
                        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
                        val admin = currentAdmin
                        if (admin != null) {
                            adminRepository.logActivity(
                                actorId = admin.uid,
                                actorName = admin.name,
                                actorRole = "administrator",
                                action = if (isActive) "category.updated" else "category.disabled",
                                entityType = "category",
                                entityId = categoryId,
                                description = "Category ${if (isActive) "enabled" else "disabled"}"
                            )
                        }
                    } catch (_: Exception) { }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Failed")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Admin menghapus kategori dan mencatat aktivitas pengelolaan kategori.
    fun deleteCategory(categoryId: String, categoryName: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = categoryRepository.deleteCategory(categoryId)) {
                is Result.Success -> {
                    try {
                        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
                        val admin = currentAdmin
                        if (admin != null) {
                            adminRepository.logActivity(
                                actorId = admin.uid,
                                actorName = admin.name,
                                actorRole = "administrator",
                                action = "category.deleted",
                                entityType = "category",
                                entityId = categoryId,
                                description = "Kategori dihapus: $categoryName"
                            )
                        }
                    } catch (_: Exception) { }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Gagal menghapus kategori")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Menghapus laporan beserta attachment terkait, lalu mencatat aktivitas jika dipanggil admin/user.
    fun deleteReport(reportId: String, reportTitle: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            when (val result = reportRepository.deleteReport(reportId)) {
                is Result.Success -> {
                    try {
                        // FLOW: `val admin` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
                        val admin = currentAdmin
                        if (admin != null) {
                            adminRepository.logActivity(
                                actorId = admin.uid,
                                actorName = admin.name,
                                actorRole = "administrator",
                                action = "report.deleted",
                                entityType = "incidentReport",
                                entityId = reportId,
                                description = "Laporan dihapus: $reportTitle"
                            )
                        }
                    } catch (_: Exception) { }
                    _updateState.value = UiState.Success(Unit)
                }
                is Result.Error -> _updateState.value = UiState.Error(result.message ?: "Gagal menghapus laporan")
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Mencatat atau membaca audit trail aktivitas penting aplikasi.
    fun loadActivityLogs() {
        viewModelScope.launch {
            adminRepository.observeActivityLogs()
                .catch { _activityLogs.value = UiState.Error(it.message ?: "Gagal memuat log aktivitas") }
                .collect { _activityLogs.value = UiState.Success(it) }
        }
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }
}
