# Lampiran 2 – Source Code: Listing Kode Penting Lengkap Aplikasi SIRS

Dokumen ini berisi *listing* lengkap kode sumber bagian penting aplikasi SIRS yang
mendukung alur kerja utama: navigasi berbasis peran, autentikasi, alur laporan,
alur admin, validasi bukti, unggah berkas ke Cloudinary, serta kontrol akses basis
data Firestore.

Daftar berkas yang dicantumkan:

| No | Berkas | Peran |
|----|--------|-------|
| 1 | `AppNavGraph.kt` | Navigasi antar layar berdasarkan peran (user/admin) |
| 2 | `AuthViewModel.kt` | Autentikasi (login, register, reset password, logout) |
| 3 | `ReportViewModel.kt` | Alur laporan insiden (buat, edit, hapus, filter) |
| 4 | `AdminViewModel.kt` | Alur admin (monitoring, ubah status, kategori, log) |
| 5 | `FileValidation.kt` | Validasi keamanan berkas bukti (magic bytes) |
| 6 | `CloudinaryUploadDataSource.kt` | Unggah & hapus berkas ke Cloudinary |
| 7 | `firestore.rules` | Kontrol akses (Authorization) basis data Firestore |

---

## 1. AppNavGraph.kt — Navigasi Role

Lokasi: `app/src/main/java/com/adit/sirs/presentation/navigation/AppNavGraph.kt`

Menentukan rute awal berdasarkan status login dan peran (`isAdmin`), lalu memetakan
setiap rute ke layar Compose-nya. Pengalihan dari login ke dashboard dilakukan dengan
`popUpTo` agar layar auth tidak bisa di-*back*.

```kotlin
package com.adit.sirs.presentation.navigation

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adit.sirs.presentation.admin.*
import com.adit.sirs.presentation.auth.*
import com.adit.sirs.presentation.dashboard.*
import com.adit.sirs.presentation.profile.*
import com.adit.sirs.presentation.reports.*

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `AppNavGraph` menjalankan langkah khusus pada file ini dan menjaga alur menghubungkan route aplikasi dengan layar yang harus dibuka.
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // FLOW: `val currentUser by authViewModel.currentUser.collectAsState()` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser by authViewModel.currentUser.collectAsState()

    // FLOW: `val startDestination` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val startDestination = if (currentUser != null) {
        if (currentUser!!.isAdmin) Routes.ADMIN_DASHBOARD else Routes.USER_DASHBOARD
    } else {
        Routes.LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Auth
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = { isAdmin ->
                    val dest = if (isAdmin) Routes.ADMIN_DASHBOARD else Routes.USER_DASHBOARD
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.USER_DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // User Dashboard
        composable(Routes.USER_DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            UserDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToReportList = { navController.navigate(Routes.REPORT_LIST) },
                onNavigateToCreateReport = { navController.navigate(Routes.CREATE_REPORT) },
                onNavigateToReportDetail = { navController.navigate(Routes.reportDetail(it)) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Dashboard
        composable(Routes.ADMIN_DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            AdminDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAdminReportList = { navController.navigate(Routes.ADMIN_REPORT_LIST) },
                onNavigateToAdminReportDetail = { navController.navigate(Routes.adminReportDetail(it)) },
                onNavigateToCategories = { navController.navigate(Routes.CATEGORY_MANAGEMENT) },
                onNavigateToActivityLog = { navController.navigate(Routes.ACTIVITY_LOG) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Report List
        composable(Routes.REPORT_LIST) {
            val reportViewModel: ReportViewModel = hiltViewModel()
            ReportListScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateReport = { navController.navigate(Routes.CREATE_REPORT) },
                onNavigateToReportDetail = { navController.navigate(Routes.reportDetail(it)) }
            )
        }

        // Report Detail
        composable(
            Routes.REPORT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            val reportViewModel: ReportViewModel = hiltViewModel()
            ReportDetailScreen(
                reportId = reportId,
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(Routes.editReport(it)) }
            )
        }

        // Create Report
        composable(Routes.CREATE_REPORT) {
            val reportViewModel: ReportViewModel = hiltViewModel()
            CreateReportScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        // Edit Report
        composable(
            Routes.EDIT_REPORT,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            val reportViewModel: ReportViewModel = hiltViewModel()
            EditReportScreen(
                reportId = reportId,
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }

        // Admin Report List
        composable(Routes.ADMIN_REPORT_LIST) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminReportListScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(Routes.adminReportDetail(it)) },
                onNavigateToCreate = { navController.navigate(Routes.ADMIN_CREATE_REPORT) }
            )
        }

        // Admin Create Report
        composable(Routes.ADMIN_CREATE_REPORT) {
            val reportViewModel: ReportViewModel = hiltViewModel()
            CreateReportScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                title = "Tambah Laporan",
                submitLabel = "Simpan Laporan"
            )
        }

        // Admin Report Detail
        composable(
            Routes.ADMIN_REPORT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminReportDetailScreen(
                reportId = reportId,
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        // Category Management
        composable(Routes.CATEGORY_MANAGEMENT) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            CategoryManagementScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Activity Log
        composable(Routes.ACTIVITY_LOG) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            ActivityLogScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Routes.PROFILE) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 2. AuthViewModel.kt — Autentikasi

Lokasi: `app/src/main/java/com/adit/sirs/presentation/auth/AuthViewModel.kt`

Mengelola status autentikasi (`loginState`, `registerState`, `resetPasswordState`,
`currentUser`) dengan `StateFlow`. Password divalidasi secara ketat (≥12 karakter,
huruf besar/kecil, angka, simbol) sebelum registrasi.

```kotlin
package com.adit.sirs.presentation.auth

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.User
import com.adit.sirs.domain.repository.AuthRepository
import com.adit.sirs.core.common.Result
import com.adit.sirs.core.firebase.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// FLOW: Annotation ini membuat ViewModel dapat menerima dependency dari Hilt secara otomatis.
@HiltViewModel
// FLOW: `AuthViewModel` menyimpan state layar dan menjadi penghubung antara UI Compose dengan repository.
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    // FLOW: `private val _loginState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    // FLOW: `val loginState: StateFlow<UiState<User>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    // FLOW: `private val _registerState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    // FLOW: `val registerState: StateFlow<UiState<User>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    // FLOW: `private val _resetPasswordState` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _resetPasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    // FLOW: `val resetPasswordState: StateFlow<UiState<Unit>>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val resetPasswordState: StateFlow<UiState<Unit>> = _resetPasswordState.asStateFlow()

    // FLOW: `private val _currentUser` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    private val _currentUser = MutableStateFlow<User?>(null)
    // FLOW: `val currentUser: StateFlow<User?>` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    // FLOW: Mengecek user aktif sebagai titik awal penentuan dashboard dan hak akses.
    private fun checkCurrentUser() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                when (val result = authRepository.getCurrentUser()) {
                    is Result.Success -> _currentUser.value = result.data
                    else -> _currentUser.value = null
                }
            }
        }
    }

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _loginState.value = UiState.Success(result.data)
                    analyticsHelper.logLoginSuccess(result.data.role.value)
                }
                is Result.Error -> {
                    _loginState.value = UiState.Error(result.message ?: "Login failed")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    fun register(name: String, email: String, password: String) {
        val passwordValidationError = validatePassword(password)
        if (passwordValidationError != null) {
            _registerState.value = UiState.Error(passwordValidationError)
            return
        }

        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(name, email, password)) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    _registerState.value = UiState.Success(result.data)
                    analyticsHelper.logLoginSuccess(result.data.role.value)
                }
                is Result.Error -> {
                    _registerState.value = UiState.Error(result.message ?: "Pendaftaran gagal")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Memvalidasi kekuatan password sebelum registrasi agar akun memenuhi standar keamanan aplikasi.
    private fun validatePassword(password: String): String? {
        return when {
            password.length < 12 -> "Password minimal 12 karakter"
            !password.any { it.isUpperCase() } -> "Password harus memiliki huruf besar"
            !password.any { it.isLowerCase() } -> "Password harus memiliki huruf kecil"
            !password.any { it.isDigit() } -> "Password harus memiliki angka"
            !password.any { !it.isLetterOrDigit() } -> "Password harus memiliki karakter khusus"
            else -> null
        }
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            when (val result = authRepository.sendPasswordReset(email)) {
                is Result.Success -> {
                    _resetPasswordState.value = UiState.Success(Unit)
                }
                is Result.Error -> {
                    _resetPasswordState.value = UiState.Error(result.message ?: "Failed to send reset email")
                }
                is Result.Loading -> { }
            }
        }
    }

    // FLOW: Menghapus sesi/token lokal lalu mengembalikan aplikasi ke halaman login.
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
        }
    }

    // FLOW: Memproses login user, mengubah state UI, lalu mengarahkan user sesuai role.
    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    // FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }

    // FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
    fun resetPasswordResetState() {
        _resetPasswordState.value = UiState.Idle
    }
}
```

---

## 3. ReportViewModel.kt — Alur Laporan

Lokasi: `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt`

Mengatur siklus laporan: muat daftar (realtime), filter status/severity, detail +
riwayat status, validasi & unggah bukti (`setAttachment`), `submitReport` (rate limit →
upload → SLA → simpan Firestore → activity log), `updateReport`, `deleteReport`.

```kotlin
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
```

---

## 4. AdminViewModel.kt — Alur Admin

Lokasi: `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt`

Menangani monitoring semua laporan dengan filter (search, severity, tanggal, status),
ubah status/severity/respon laporan (`updateReportStatus`), manajemen kategori
(buat/ubah/aktif/nonaktif/hapus), serta baca *activity log* audit.

```kotlin
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
```

---

## 5. FileValidation.kt — Validasi Bukti

Lokasi: `app/src/main/java/com/adit/sirs/core/security/FileValidation.kt`

Melakukan validasi keamanan berkas bukti: memeriksa MIME, ukuran (maks 2 MB), ekstensi,
dan terpenting **magic bytes / signature asli** (JPEG `FF D8 FF`, PNG `89 50 4E 47 0D 0A 1A 0A`,
PDF `25 50 44 46 2D`) agar ekstensi palsu tidak lolos.

```kotlin
package com.adit.sirs.core.security

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.adit.sirs.core.constants.AppConstants
import java.io.IOException
import java.util.Locale

// FLOW: `FileValidationResult` adalah struktur data yang membawa informasi antarbagian aplikasi.
data class FileValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val mimeType: String? = null,
    val fileSize: Long = 0,
    val fileName: String? = null
)

// FLOW: [FileValidation] merupakan objek keamanan (*security helper*) yang bertanggung jawab memvalidasi keaslian file.
// Tidak hanya mengecek ekstensi, tapi juga mencocokkan *Magic Bytes* / File Signature asli untuk mencegah injeksi malware.
object FileValidation {

    private const val MAX_SIGNATURE_BYTES = 8

    // FLOW: Memeriksa MIME, ukuran, ekstensi, dan magic bytes agar file upload benar-benar aman.
    fun validateFile(
        contentResolver: ContentResolver,
        uri: Uri
    ): FileValidationResult {
        val mimeType = contentResolver.getType(uri)?.lowercase(Locale.ROOT)
        if (!MimeTypeValidator.isAllowed(mimeType)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Tipe file tidak valid. Hanya JPG, PNG, dan PDF yang diperbolehkan."
            )
        }

        val metadata = readFileMetadata(contentResolver, uri)
        val fileSize = metadata.fileSize
        val fileName = metadata.fileName

        if (fileSize <= 0) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ukuran file tidak dapat dibaca."
            )
        }

        if (fileSize > AppConstants.MAX_ATTACHMENT_SIZE_BYTES) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "File terlalu besar. Ukuran maksimal 2 MB."
            )
        }

        val extension = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }

        if (extension != null && extension !in AppConstants.ALLOWED_EXTENSIONS) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ekstensi file tidak valid. Hanya JPG, PNG, dan PDF yang diperbolehkan."
            )
        }

        val expectedExtension = MimeTypeValidator.getExtensionFromMime(mimeType!!)
        if (extension != null && expectedExtension != null && !isCompatibleExtension(expectedExtension, extension)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Ekstensi file tidak sesuai dengan tipe file."
            )
        }

        val signature = readSignature(contentResolver, uri)
            ?: return FileValidationResult(
                isValid = false,
                errorMessage = "File tidak dapat dibaca untuk validasi keamanan."
            )

        val detectedMimeType = detectMimeTypeFromMagicBytes(signature)
            ?: return FileValidationResult(
                isValid = false,
                errorMessage = "Signature file tidak valid. Hanya file JPG, PNG, dan PDF asli yang diperbolehkan."
            )

        if (!isCompatibleMimeType(mimeType, detectedMimeType)) {
            return FileValidationResult(
                isValid = false,
                errorMessage = "Isi file tidak sesuai dengan tipe file yang dipilih."
            )
        }

        return FileValidationResult(
            isValid = true,
            mimeType = detectedMimeType,
            fileSize = fileSize,
            fileName = fileName
        )
    }

    // FLOW: Membaca metadata nama dan ukuran file dari Android ContentResolver tanpa meload seluruh isi file ke memori.
    private fun readFileMetadata(contentResolver: ContentResolver, uri: Uri): FileMetadata {
        var fileSize = 0L
        var fileName: String? = null

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) fileSize = cursor.getLong(sizeIndex)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) fileName = cursor.getString(nameIndex)
            }
        }

        if (fileSize <= 0) {
            fileSize = contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize.takeIf { it > 0 }
            } ?: 0L
        }

        return FileMetadata(fileSize = fileSize, fileName = fileName)
    }

    // FLOW: Fungsi `readSignature` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun readSignature(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(MAX_SIGNATURE_BYTES)
                val read = inputStream.read(buffer)
                if (read <= 0) null else buffer.copyOf(read)
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    // FLOW: Fungsi `detectMimeTypeFromMagicBytes` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun detectMimeTypeFromMagicBytes(bytes: ByteArray): String? {
        return when {
            bytes.size >= 3 &&
                bytes[0] == 0xFF.toByte() &&
                bytes[1] == 0xD8.toByte() &&
                bytes[2] == 0xFF.toByte() -> "image/jpeg"

            bytes.size >= 8 &&
                bytes[0] == 0x89.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x4E.toByte() &&
                bytes[3] == 0x47.toByte() &&
                bytes[4] == 0x0D.toByte() &&
                bytes[5] == 0x0A.toByte() &&
                bytes[6] == 0x1A.toByte() &&
                bytes[7] == 0x0A.toByte() -> "image/png"

            bytes.size >= 5 &&
                bytes[0] == 0x25.toByte() &&
                bytes[1] == 0x50.toByte() &&
                bytes[2] == 0x44.toByte() &&
                bytes[3] == 0x46.toByte() &&
                bytes[4] == 0x2D.toByte() -> "application/pdf"

            else -> null
        }
    }

    // FLOW: Fungsi `isCompatibleMimeType` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun isCompatibleMimeType(reportedMimeType: String, detectedMimeType: String): Boolean {
        return reportedMimeType == detectedMimeType ||
            (reportedMimeType == "image/jpg" && detectedMimeType == "image/jpeg")
    }

    // FLOW: Fungsi `isCompatibleExtension` menjalankan langkah khusus pada file ini dan menjaga alur menangani validasi keamanan seperti file upload dan SLA penanganan.
    private fun isCompatibleExtension(expectedExtension: String, actualExtension: String): Boolean {
        return expectedExtension == actualExtension ||
            (expectedExtension == "jpg" && actualExtension == "jpeg")
    }

    private data class FileMetadata(
        val fileSize: Long,
        val fileName: String?
    )
}
```

---

## 6. CloudinaryUploadDataSource.kt — Upload Bukti

Lokasi: `app/src/main/java/com/adit/sirs/data/remote/CloudinaryUploadDataSource.kt`

Mengunggah berkas ke Cloudinary dengan alur aman: (1) meminta *signature* berbasis
token Firebase JWT dari Cloudflare Worker, (2) mengunggah memakai signature tersebut
(bukan preset *unsigned*). Juga menyediakan `deleteFile` untuk hapus permanen.

```kotlin
package com.adit.sirs.data.remote

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.ContentResolver
import android.net.Uri
import com.adit.sirs.BuildConfig
import com.adit.sirs.domain.model.Attachment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// FLOW: `CloudinaryUploadDataSource` adalah akses langsung ke layanan eksternal/database untuk satu jenis data.
class CloudinaryUploadDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // FLOW: Mengirim file valid ke Cloudinary dan mengembalikan metadata attachment untuk laporan.
    suspend fun uploadFile(
        contentResolver: ContentResolver,
        fileUri: Uri,
        mimeType: String,
        fileName: String
    ): Attachment = withContext(Dispatchers.IO) {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        val folder = BuildConfig.CLOUDINARY_UPLOAD_FOLDER

        val resourceType = if (mimeType == "application/pdf") "raw" else "image"
        val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"

        val inputStream = contentResolver.openInputStream(fileUri)
            ?: throw IOException("Cannot open file")

        val fileBytes = inputStream.use { it.readBytes() }
        val mediaType = mimeType.toMediaType()

        // LANGKAH 1: Minta "Signature" ke Cloudflare Worker
        val userToken = auth.currentUser?.getIdToken(false)?.await()?.token
            ?: throw IOException("User token not available")

        // Memastikan URL berakhiran /generate-signature
        val baseUrl = BuildConfig.CLOUDINARY_DELETE_ENDPOINT.trimEnd('/')
        val signatureUrl = if (baseUrl.endsWith("/destroy")) {
            baseUrl.replace("/destroy", "/generate-signature")
        } else {
            "$baseUrl/generate-signature"
        }

        val sigRequest = Request.Builder()
            .url(signatureUrl)
            .get()
            .addHeader("Authorization", "Bearer $userToken")
            .build()

        val sigResponse = client.newCall(sigRequest).execute()
        if (!sigResponse.isSuccessful) {
            throw IOException("Gagal mendapatkan otorisasi upload: ${sigResponse.body?.string()}")
        }

        val sigJson = JSONObject(sigResponse.body!!.string())
        val signature = sigJson.getString("signature")
        val timestamp = sigJson.getString("timestamp")
        val apiKey = sigJson.getString("api_key")
        val folderConfig = sigJson.getString("folder")

        // LANGKAH 2: Upload ke Cloudinary memakai Signature (BUKAN Unsigned Preset lagi)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", fileName, fileBytes.toRequestBody(mediaType))
            .addFormDataPart("folder", folderConfig)
            .addFormDataPart("api_key", apiKey)
            .addFormDataPart("timestamp", timestamp)
            .addFormDataPart("signature", signature)
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("Upload failed: $errorBody")
        }

        val responseBody = response.body?.string()
            ?: throw IOException("Empty response from Cloudinary")

        val json = JSONObject(responseBody)

        Attachment(
            originalName = fileName,
            publicId = json.optString("public_id", ""),
            secureUrl = json.optString("secure_url", ""),
            resourceType = json.optString("resource_type", resourceType),
            format = json.optString("format", ""),
            mimeType = mimeType,
            bytes = json.optLong("bytes", 0),
            uploadedAt = Timestamp.now()
        )
    }

    // FLOW: Menghapus file secara permanen dari Cloudinary dengan memanggil backend Serverless yang memvalidasi otorisasi Token Firebase JWT pengguna.
    suspend fun deleteFile(publicId: String, resourceType: String) = withContext(Dispatchers.IO) {
        if (publicId.isBlank()) return@withContext

        val endpoint = BuildConfig.CLOUDINARY_DELETE_ENDPOINT
        if (endpoint.isBlank()) {
            throw IOException("Konfigurasi hapus file Cloudinary belum tersedia. Set CLOUDINARY_DELETE_ENDPOINT ke endpoint backend.")
        }

        val user = auth.currentUser ?: throw IOException("User tidak terautentikasi")
        val idToken = try {
            user.getIdToken(false).await().token
        } catch (e: Exception) {
            throw IOException("Gagal mendapatkan Firebase ID Token: ${e.message}")
        }
        if (idToken.isNullOrBlank()) throw IOException("Token autentikasi tidak valid")

        val payload = JSONObject()
            .put("publicId", publicId)
            .put("resourceType", resourceType.ifBlank { "image" })
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $idToken")
            .post(payload)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw IOException("Gagal menghapus file Cloudinary: $errorBody")
        }
    }
}
```

---

## 7. firestore.rules — Kontrol Akses Database

Lokasi: `firestore.rules`

Aturan keamanan Firestore (rules_version 2) yang menerapkan *authorization* berlapis:
fungsi bantu `signedIn`, `isOwner`, `isAdmin`, `isActiveUser`, serta validasi isi dokumen
(`validUserCreate`, `validReportCreate`, `onlyPendingOwnerEdit`). Ditutup dengan
`match /{document=**} { allow read, write: if false; }` (default deny).

```javascript
// Menggunakan versi aturan terbaru (v2) dari Firebase Firestore
rules_version = '2';

// Mendefinisikan aturan keamanan untuk layanan Cloud Firestore
service cloud.firestore {
  
  // Berlaku untuk semua database dan dokumen di dalamnya
  match /databases/{database}/documents {

    // =====================================================================
    // FUNGSI BANTUAN (HELPER FUNCTIONS)
    // =====================================================================

    // Mengecek apakah pengguna sudah login (memiliki token autentikasi Firebase)
    function signedIn() {
      return request.auth != null;
    }

    // Mengecek apakah pengguna sudah login DAN UID-nya cocok dengan UID yang sedang diakses
    function isOwner(uid) {
      return signedIn() && request.auth.uid == uid;
    }

    // Mengambil profil pengguna yang sedang login dari koleksi 'users' di database
    function currentUserDoc() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid));
    }

    // Mengecek apakah pengguna adalah Admin. 
    // Syarat: Login -> Profilnya ada di database -> rolenya 'administrator' -> statusnya aktif
    function isAdmin() {
      return signedIn()
        && exists(/databases/$(database)/documents/users/$(request.auth.uid))
        && currentUserDoc().data.role == 'administrator'
        && currentUserDoc().data.isActive == true;
    }

    // Mengecek apakah pengguna adalah user biasa yang akunnya sedang aktif (tidak diblokir)
    function isActiveUser() {
      return signedIn()
        && exists(/databases/$(database)/documents/users/$(request.auth.uid))
        && currentUserDoc().data.isActive == true;
    }

    // Memvalidasi saat proses registrasi/pembuatan user baru:
    // Harus pemilik akun sendiri, UID dan Email harus cocok dengan data token Firebase Auth,
    // role otomatis di-set 'user', dan akun langsung aktif.
    function validUserCreate(uid) {
      return isOwner(uid)
        && request.resource.data.uid == uid
        && request.resource.data.email == request.auth.token.email
        && request.resource.data.role == 'user'
        && request.resource.data.isActive == true;
    }

    // Memvalidasi saat pembuatan laporan baru (Cegah data kotor masuk):
    // Pembuat harus user aktif, userId harus sesuai UID login, status awal wajib 'pending', belum dihapus,
    // serta memeriksa batas karakter untuk judul (5-150), deskripsi (20-4000), lokasi (3-255), 
    // dan tingkat keparahan (low/medium/high/critical).
    function validReportCreate() {
      return isActiveUser()
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.status == 'pending'
        && request.resource.data.deletedAt == null
        && request.resource.data.title is string
        && request.resource.data.title.size() >= 5
        && request.resource.data.title.size() <= 150
        && request.resource.data.description is string
        && request.resource.data.description.size() >= 20
        && request.resource.data.description.size() <= 4000
        && request.resource.data.location is string
        && request.resource.data.location.size() >= 3
        && request.resource.data.location.size() <= 255
        && request.resource.data.severity in ['low', 'medium', 'high', 'critical'];
    }

    // Mengecek apakah laporan yang sedang dibaca/diubah benar-benar milik pengguna yang sedang login
    function ownsExistingReport() {
      return isActiveUser() && resource.data.userId == request.auth.uid;
    }

    // Mengizinkan penarikan daftar laporan: Admin boleh menarik semua laporan, 
    // sedangkan User aktif diizinkan (dengan catatan di kode Android querynya hanya menarik laporannya sendiri)
    function canListReports() {
      return isAdmin() || (isActiveUser() && request.auth.uid != null); // Allow query if it's filtered to user's own uid in Android code
    }

    // Memvalidasi pengeditan laporan oleh user:
    // Laporan harus miliknya, statusnya harus masih 'pending', dan user dilarang meretas/mengubah userId atau statusnya sendiri
    function onlyPendingOwnerEdit() {
      return ownsExistingReport()
        && resource.data.status == 'pending'
        && request.resource.data.userId == resource.data.userId
        && request.resource.data.status == resource.data.status;
    }

    // Menentukan siapa yang berhak menghapus laporan: 
    // Admin boleh hapus semuanya. Pemilik hanya boleh menghapus miliknya sendiri selama masih 'pending'.
    function canDeleteExistingReport() {
      return isAdmin()
        || (ownsExistingReport() && resource.data.status == 'pending');
    }

    // =====================================================================
    // ATURAN PER KOLEKSI (COLLECTION RULES)
    // =====================================================================

    // 1. Aturan untuk koleksi 'users'
    match /users/{uid} {
      // Boleh menyimpan data jika validasinya lolos (user baru registrasi) ATAU jika dia Admin
      allow create: if validUserCreate(uid) || isAdmin();
      // Boleh melihat profil jika itu adalah profilnya sendiri ATAU jika dia Admin
      allow read: if isOwner(uid) || isAdmin();
      // Pemilik profil boleh update data, TAPI dilarang mengubah UID, Email, Role, dan status Aktif (Cegah eskalasi hak)
      allow update: if isOwner(uid)
        && request.resource.data.uid == resource.data.uid
        && request.resource.data.email == resource.data.email
        && request.resource.data.role == resource.data.role
        && request.resource.data.isActive == resource.data.isActive;
      // Hanya Admin yang punya kontrol penuh untuk memblokir (update) atau menghapus user
      allow update, delete: if isAdmin();
    }

    // 2. Aturan untuk koleksi 'categories'
    match /categories/{categoryId} {
      // Semua user yang aktif boleh membaca daftar kategori untuk mengisi form
      allow read: if isActiveUser();
      // Hanya Admin yang bisa menambah, mengubah, atau menghapus kategori
      allow create, update, delete: if isAdmin();
    }

    // 3. Aturan untuk koleksi 'incidentReports' (Laporan Insiden Utama)
    match /incidentReports/{reportId} {
      // Boleh buat laporan baru jika datanya lolos semua validasi
      allow create: if validReportCreate();
      // Boleh baca detail 1 laporan jika dia Admin ATAU jika dia yang membuat laporan tersebut
      allow get: if isAdmin() || ownsExistingReport();
      // Boleh tarik daftar banyak laporan (Sesuai kondisi canListReports)
      allow list: if canListReports();
      // Boleh ubah data jika dia Admin (misal: proses laporan) ATAU jika dia pemilik laporan & statusnya masih pending
      allow update: if isAdmin() || onlyPendingOwnerEdit();
      // Boleh hapus laporan jika dia Admin ATAU jika dia pemilik laporan & statusnya masih pending
      allow delete: if canDeleteExistingReport();

      // 3.1. Aturan untuk sub-koleksi 'statusHistories' (Jejak riwayat perubahan status di dalam laporan)
      match /statusHistories/{historyId} {
        // Boleh baca riwayat jika dia Admin ATAU jika dia pemilik dari laporan induknya
        allow read: if isAdmin()
          || (isActiveUser()
            && get(/databases/$(database)/documents/incidentReports/$(reportId)).data.userId == request.auth.uid);
        // Hanya Admin yang boleh menambah atau mengubah riwayat status
        allow create, update: if isAdmin();
        // Admin boleh hapus riwayat. User juga otomatis diizinkan menghapus HANYA saat 
        // dia menghapus laporan utamanya (fitur hapus berantai/batch delete) yang masih pending.
        allow delete: if isAdmin()
          || (isActiveUser()
            && get(/databases/$(database)/documents/incidentReports/$(reportId)).data.userId == request.auth.uid
            && get(/databases/$(database)/documents/incidentReports/$(reportId)).data.status == 'pending');
      }
    }

    // 4. Aturan untuk koleksi 'activityLogs' (Catatan Rekam Jejak Sistem / Audit)
    match /activityLogs/{logId} {
      // Hanya Admin yang boleh melihat log aktivitas sistem
      allow read: if isAdmin();
      // User aktif (dan Admin) boleh merekam log, asalkan aktor ID-nya sesuai dengan ID aslinya
      allow create: if isActiveUser()
        && request.resource.data.actorId == request.auth.uid
        && request.resource.data.actorRole in ['user', 'administrator'];
      // Log aktivitas TIDAK BOLEH diubah atau dihapus oleh SIAPAPUN (Menjaga keaslian barang bukti jejak sistem)
      allow update, delete: if false;
    }

    // 5. Aturan untuk koleksi 'deviceTokens' (Token Push Notification FCM)
    match /deviceTokens/{tokenId} {
      // Hanya Admin yang boleh menarik token untuk mengirim notifikasi push
      allow read: if isAdmin();
      // User aktif boleh menyimpan/update token perangkatnya sendiri
      allow create, update: if isActiveUser()
        && request.resource.data.uid == request.auth.uid;
      // User aktif boleh menghapus token perangkatnya sendiri (misalnya saat logout)
      allow delete: if isActiveUser() && resource.data.uid == request.auth.uid;
    }

    // 6. Aturan Kunci (Default Deny)
    // Kunci semua akses untuk URL/koleksi lain yang tidak didefinisikan secara spesifik di atas
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```
