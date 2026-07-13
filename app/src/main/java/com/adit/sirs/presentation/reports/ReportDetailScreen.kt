package com.adit.sirs.presentation.reports

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.core.security.SlaCalculator
import com.adit.sirs.core.util.DateFormatter
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.presentation.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `ReportDetailScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
fun ReportDetailScreen(
    reportId: String,
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val reportState by viewModel.reportDetail.collectAsState()
    val statusHistories by viewModel.statusHistories.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val submitState by viewModel.submitState.collectAsState()
    val context = LocalContext.current
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }

    LaunchedEffect(reportId) {
        viewModel.loadReportDetail(reportId)
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success && (submitState as UiState.Success).data == "deleted") {
            onNavigateBack()
            viewModel.resetSubmitState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Laporan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val report = (reportState as? UiState.Success)?.data
                    if (report?.isPending == true) {
                        IconButton(onClick = { onNavigateToEdit(reportId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = reportState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorView(state.message, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                val report = state.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.reportCode,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    StatusBadge(status = report.status)
                                }
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    SeverityBadge(severity = report.severity)
                                    InfoChip(
                                        text = report.categoryName,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Informasi Laporan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                DetailRow("Lokasi", report.location)
                                DetailRow("Tanggal Insiden", DateFormatter.formatDateOnly(report.incidentDate))
                                DetailRow("Dibuat", DateFormatter.formatTimestamp(report.createdAt))
                            }
                        }
                    }
                    item {
                        val overdue = SlaCalculator.isOverdue(report.slaDeadlineAt, nowMillis)
                        val nearDeadline = SlaCalculator.isNearDeadline(report.slaDeadlineAt, nowMillis)
                        val statusText = when {
                            report.slaDeadlineAt == null -> "BELUM ADA SLA"
                            overdue -> "TERLAMBAT"
                            nearDeadline -> "SEGERA DITANGANI"
                            else -> "DALAM BATAS SLA"
                        }
                        val containerColor = when {
                            overdue -> MaterialTheme.colorScheme.errorContainer
                            nearDeadline -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.55f))) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SLA Penanganan", style = MaterialTheme.typography.titleMedium)
                                    InfoChip(
                                        text = statusText,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    SlaCalculator.formatRemaining(report.slaDeadlineAt, nowMillis),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text("Deadline: ${DateFormatter.formatTimestamp(report.slaDeadlineAt)}", style = MaterialTheme.typography.bodyMedium)
                                
                                val slaRule = when(report.severity.value) {
                                    "critical" -> "Maksimal 24 jam sejak laporan dikirim."
                                    "high" -> "Maksimal 48 jam sejak laporan dikirim."
                                    "medium" -> "Maksimal 3 hari sejak laporan dikirim."
                                    "low" -> "Maksimal 7 hari sejak laporan dikirim."
                                    else -> "Aturan SLA tidak ditentukan."
                                }
                                Text("Aturan: $slaRule", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    val category = categories.firstOrNull { it.id == report.categoryId }
                    if (category != null && (category.mitigationTips.isNotEmpty() || category.recommendedEvidence.isNotEmpty())) {
                        item {
                            CategoryGuidanceCard(
                                mitigationTips = category.mitigationTips,
                                recommendedEvidence = category.recommendedEvidence
                            )
                        }
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Deskripsi Insiden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(report.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Attachment
                    if (report.attachment != null) {
                        item {
                            AttachmentCard(
                                originalName = report.attachment.originalName,
                                bytes = report.attachment.bytes,
                                format = report.attachment.format,
                                mimeType = report.attachment.mimeType,
                                secureUrl = report.attachment.secureUrl,
                                onOpen = { url ->
                                    try {
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                                    } catch (_: Exception) { }
                                }
                            )
                        }
                    }

                    // Admin Response
                    if (report.adminResponse != null) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Admin Response", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(report.adminResponse)
                                    if (report.handledByName != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Handled by: ${report.handledByName}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    if (report.handledAt != null) {
                                        Text(
                                            "At: ${DateFormatter.formatTimestamp(report.handledAt)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Status History
                    if (statusHistories.isNotEmpty()) {
                        item {
                            Text("Status History", style = MaterialTheme.typography.titleSmall)
                        }
                        items(statusHistories) { history ->
                            StatusHistoryItem(history)
                        }
                    }
                }
            }
            is UiState.Idle -> {}
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report") },
            text = { Text("Are you sure you want to delete this report?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteReport(reportId)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `CategoryGuidanceCard` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
private fun CategoryGuidanceCard(
    mitigationTips: List<String>,
    recommendedEvidence: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mitigationTips.isNotEmpty()) {
                Text("Panduan tindakan awal", style = MaterialTheme.typography.titleSmall)
                mitigationTips.forEach { tip ->
                    Text("• $tip", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (recommendedEvidence.isNotEmpty()) {
                Text("Bukti yang disarankan", style = MaterialTheme.typography.titleSmall)
                recommendedEvidence.forEach { evidence ->
                    Text("• $evidence", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
