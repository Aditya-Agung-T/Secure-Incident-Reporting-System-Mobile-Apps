package com.adit.sirs.presentation.dashboard

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.IncidentReport
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AdminRepository
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.domain.repository.ReportRepository
import com.adit.sirs.core.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// FLOW: `DashboardStats` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class DashboardStats(
    val total: Int = 0,
    val pending: Int = 0,
    val investigating: Int = 0,
    val resolved: Int = 0,
    val rejected: Int = 0,
    val criticalUnresolved: Int = 0,
    val highUnresolved: Int = 0
)

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `DashboardViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    // FLOW: `private val _currentUser` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _currentUser = MutableStateFlow<User?>(null)
    // FLOW: `val currentUser: StateFlow<User?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // FLOW: `private val _reports` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _reports = MutableStateFlow<UiState<List<IncidentReport>>>(UiState.Loading)
    // FLOW: `val reports: StateFlow<UiState<List<IncidentReport>>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val reports: StateFlow<UiState<List<IncidentReport>>> = _reports.asStateFlow()

    // FLOW: `private val _stats` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _stats = MutableStateFlow(DashboardStats())
    // FLOW: `val stats: StateFlow<DashboardStats>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    init {
        loadCurrentUser()
    }

    // FLOW: Mengecek user aktif sebagai titik awal penentuan dashboard dan hak akses.
    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    result.data?.let { user ->
                        if (user.isAdmin) loadAdminReports() else loadUserReports(user.uid)
                    }
                }
                is Result.Error -> {
                    _reports.value = UiState.Error(result.message ?: "Failed to load user")
                }
                is Result.Loading -> {}
            }
        }
    }

    // FLOW: Mengambil laporan milik user login secara realtime dari repository.
    private fun loadUserReports(userId: String) {
        viewModelScope.launch {
            reportRepository.observeUserReports(userId)
                .catch { 
                    _reports.value = UiState.Success(emptyList())
                    _stats.value = DashboardStats()
                }
                .collect { reports ->
                    _reports.value = UiState.Success(reports)
                    _stats.value = computeStats(reports)
                }
        }
    }

    // FLOW: Mengambil seluruh laporan untuk dashboard atau daftar admin.
    private fun loadAdminReports() {
        viewModelScope.launch {
            adminRepository.observeAllReports()
                .catch { 
                    _reports.value = UiState.Success(emptyList())
                    _stats.value = DashboardStats()
                }
                .collect { reports ->
                    _reports.value = UiState.Success(reports)
                    _stats.value = computeStats(reports)
                }
        }
    }

    // FLOW: Menghitung statistik dashboard dari daftar laporan yang diterima.
    private fun computeStats(reports: List<IncidentReport>): DashboardStats {
        return DashboardStats(
            total = reports.size,
            pending = reports.count { it.status == IncidentStatus.PENDING },
            investigating = reports.count { it.status == IncidentStatus.INVESTIGATING },
            resolved = reports.count { it.status == IncidentStatus.RESOLVED },
            rejected = reports.count { it.status == IncidentStatus.REJECTED },
            criticalUnresolved = reports.count {
                it.severity == Severity.CRITICAL && it.status in listOf(IncidentStatus.PENDING, IncidentStatus.INVESTIGATING)
            },
            highUnresolved = reports.count {
                it.severity == Severity.HIGH && it.status in listOf(IncidentStatus.PENDING, IncidentStatus.INVESTIGATING)
            }
        )
    }
}
