package com.adit.sirs.presentation.reports

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `ReportListScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
fun ReportListScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreateReport: () -> Unit,
    onNavigateToReportDetail: (String) -> Unit
) {
    val reportsState by viewModel.userReports.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val severityFilter by viewModel.severityFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Saya") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onNavigateToCreateReport) {
                Icon(Icons.Default.Add, contentDescription = "Buat Laporan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("Semua Status") }
                )
                IncidentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = statusFilter == status.value,
                        onClick = { viewModel.setStatusFilter(status.value) },
                        label = { Text(status.displayName) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = severityFilter == null,
                    onClick = { viewModel.setSeverityFilter(null) },
                    label = { Text("Semua Severity") }
                )
                Severity.entries.forEach { severity ->
                    FilterChip(
                        selected = severityFilter == severity.value,
                        onClick = { viewModel.setSeverityFilter(severity.value) },
                        label = { Text(severity.displayName) }
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            when (val state = reportsState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadUserReports(statusFilter) })
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyState(
                            message = "Tidak ada laporan sesuai filter",
                            actionLabel = "Buat Laporan",
                            onAction = onNavigateToCreateReport
                        )
                    } else {
                        Text(
                            "${state.data.size} laporan ditemukan",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data, key = { it.id }) { report ->
                                ReportCardItem(
                                    report = report,
                                    showReporter = false,
                                    onClick = { onNavigateToReportDetail(report.id) }
                                )
                            }
                        }
                    }
                }
                is UiState.Idle -> {}
            }
        }
    }
}
