package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.core.security.SlaCalculator
import com.adit.sirs.core.util.DateFormatter
import com.adit.sirs.domain.model.StatusHistory
import com.adit.sirs.presentation.components.*
import com.adit.sirs.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `AdminReportDetailScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
fun AdminReportDetailScreen(
    reportId: String,
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val reportState by viewModel.reportDetail.collectAsState()
    val statusHistories by viewModel.statusHistories.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var showStatusSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
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

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            showStatusSheet = false
            if (showDeleteDialog) {
                showDeleteDialog = false
                onDeleted()
            }
            viewModel.resetUpdateState()
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
                    IconButton(onClick = { showStatusSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Update Status dan Severity")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Laporan")
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
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Informasi Pelapor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                DetailRow("Nama Lengkap", report.userName)
                                DetailRow("Alamat Email", report.userEmail)
                            }
                        }
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Detail Insiden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                DetailRow("Lokasi/IP/URL", report.location)
                                DetailRow("Waktu Kejadian", DateFormatter.formatDateOnly(report.incidentDate))
                                DetailRow("Tanggal Lapor", DateFormatter.formatTimestamp(report.createdAt))
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
                        
                        val (containerColor, contentColor) = when {
                            overdue -> Color(0xFFFFF0F0) to SeverityCritical
                            nearDeadline -> Color(0xFFFFF7ED) to SeverityHigh
                            else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                            border = BorderStroke(1.dp, if (overdue || nearDeadline) contentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Penanganan (SLA)", 
                                        style = MaterialTheme.typography.titleMedium, 
                                        fontWeight = FontWeight.Bold, 
                                        color = if (overdue || nearDeadline) contentColor else MaterialTheme.colorScheme.primary
                                    )
                                    InfoChip(
                                        text = statusText,
                                        containerColor = if (overdue || nearDeadline) contentColor else MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Countdown/Remaining time
                                Text(
                                    text = SlaCalculator.formatRemaining(report.slaDeadlineAt, nowMillis),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overdue) SeverityCritical else MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Details
                                val slaRule = when(report.severity.value) {
                                    "critical" -> "Maksimal 24 jam"
                                    "high" -> "Maksimal 48 jam"
                                    "medium" -> "Maksimal 3 hari"
                                    "low" -> "Maksimal 7 hari"
                                    else -> "Tidak ditentukan"
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Batas Waktu", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(DateFormatter.formatTimestamp(report.slaDeadlineAt), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text("Aturan Kebijakan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(slaRule, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Deskripsi Insiden",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = report.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

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
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    } catch (_: Exception) { }
                                }
                            )
                        }
                    }

                    if (report.adminResponse != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Respon Admin", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(report.adminResponse, style = MaterialTheme.typography.bodyLarge)
                                    if (report.handledByName != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Oleh: ${report.handledByName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (statusHistories.isNotEmpty()) {
                        item { 
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Riwayat Aktivitas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                        }
                        items(statusHistories) { history ->
                            StatusHistoryItem(history)
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }

                if (showStatusSheet) {
                    UpdateStatusSheet(
                        currentStatus = report.status.value,
                        currentSeverity = report.severity.value,
                        currentResponse = report.adminResponse,
                        updateState = updateState,
                        onDismiss = { showStatusSheet = false },
                        onUpdate = { newStatus, newSeverity, response ->
                            viewModel.updateReportStatus(
                                reportId = reportId,
                                newStatus = newStatus,
                                newSeverity = newSeverity,
                                adminResponse = response,
                                oldStatus = report.status.value,
                                oldSeverity = report.severity.value
                            )
                        }
                    )
                }
            }
            is UiState.Idle -> {}
        }
    }

    val reportForDelete = (reportState as? UiState.Success)?.data
    if (showDeleteDialog && reportForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Hapus Laporan") },
            text = { Text("Laporan \"${reportForDelete.title}\" akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteReport(reportId, reportForDelete.title) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }
}
