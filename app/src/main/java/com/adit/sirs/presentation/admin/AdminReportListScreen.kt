package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `AdminReportListScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
fun AdminReportListScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val reportsState by viewModel.allReports.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val severityFilter by viewModel.severityFilter.collectAsState()
    val dateStart by viewModel.dateStart.collectAsState()
    val dateEnd by viewModel.dateEnd.collectAsState()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        viewModel.loadAllReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semua Laporan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Laporan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari judul, kode, pelapor, lokasi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 2.dp),
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

            // Severity filter chips
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

            // Date range filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            cal.set(y, m, d, 0, 0, 0)
                            viewModel.setDateRange(cal.timeInMillis, dateEnd)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(dateStart?.let { dateFormat.format(Date(it)) } ?: "Tanggal Awal")
                }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            cal.set(y, m, d, 23, 59, 59)
                            viewModel.setDateRange(dateStart, cal.timeInMillis)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(dateEnd?.let { dateFormat.format(Date(it)) } ?: "Tanggal Akhir")
                }

                if (dateStart != null || dateEnd != null) {
                    IconButton(onClick = { viewModel.clearDateRange() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear dates", modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            when (val state = reportsState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadAllReports(statusFilter) })
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyState("Tidak ada laporan sesuai filter")
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
                                    showReporter = true,
                                    onClick = { onNavigateToDetail(report.id) }
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
