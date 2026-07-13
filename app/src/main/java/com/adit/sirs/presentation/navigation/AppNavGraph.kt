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
