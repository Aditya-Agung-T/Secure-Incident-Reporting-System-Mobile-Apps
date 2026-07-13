package com.adit.sirs.presentation.dashboard

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.presentation.components.*
import com.adit.sirs.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `AdminDashboardScreen` menjalankan langkah khusus pada file ini dan menjaga alur menampilkan ringkasan dashboard berdasarkan role pengguna.
fun AdminDashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAdminReportList: () -> Unit,
    onNavigateToAdminReportDetail: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToActivityLog: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    // FLOW: `val currentUser by viewModel.currentUser.collectAsState()` menyimpan state yang diamati UI agar perubahan data langsung memperbarui tampilan.
    val currentUser by viewModel.currentUser.collectAsState()
    val reportsState by viewModel.reports.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard Admin", 
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Selamat datang,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser?.name ?: "Administrator",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Stats Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Total Summary
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Laporan", 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stats.total.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 2x2 Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Pending", stats.pending.toString(), StatusPending, Modifier.weight(1f))
                        StatCard("Investigasi", stats.investigating.toString(), StatusInvestigating, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Selesai", stats.resolved.toString(), StatusResolved, Modifier.weight(1f))
                        StatCard("Ditolak", stats.rejected.toString(), StatusRejected, Modifier.weight(1f))
                    }
                }
            }

            // Urgent Alert Section
            if (stats.criticalUnresolved > 0 || stats.highUnresolved > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0))
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            // Left border accent
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(SeverityCritical)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "Peringatan",
                                    tint = SeverityCritical,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Perlu Segera Ditangani",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SeverityCritical
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (stats.criticalUnresolved > 0) {
                                        Text("${stats.criticalUnresolved} Critical belum selesai", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                    }
                                    if (stats.highUnresolved > 0) {
                                        Text("${stats.highUnresolved} High belum selesai", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Aksi Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard("Laporan", Icons.Default.Description, Modifier.weight(1f)) {
                            onNavigateToAdminReportList()
                        }
                        ActionCard("Kategori", Icons.Default.Category, Modifier.weight(1f)) {
                            onNavigateToCategories()
                        }
                        ActionCard("Log Aktivitas", Icons.Default.History, Modifier.weight(1f)) {
                            onNavigateToActivityLog()
                        }
                    }
                }
            }

            // Recent reports
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Laporan Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToAdminReportList) {
                        Text("Lihat Semua", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            when (val state = reportsState) {
                is UiState.Loading -> {
                    item { LoadingView(modifier = Modifier.height(200.dp)) }
                }
                is UiState.Error -> {
                    item { ErrorView(state.message, modifier = Modifier.height(200.dp)) }
                }
                is UiState.Success -> {
                    val reports = state.data.take(5)
                    if (reports.isEmpty()) {
                        item { EmptyState("Belum ada laporan", modifier = Modifier.height(220.dp)) }
                    } else {
                        items(reports, key = { it.id }) { report ->
                            ReportCardItem(
                                report = report,
                                showReporter = true,
                                onClick = { onNavigateToAdminReportDetail(report.id) },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
                is UiState.Idle -> {}
            }
        }
    }
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `StatCard` menjalankan langkah khusus pada file ini dan menjaga alur menampilkan ringkasan dashboard berdasarkan role pengguna.
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value, 
                style = MaterialTheme.typography.headlineMedium, 
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `ActionCard` menjalankan langkah khusus pada file ini dan menjaga alur menampilkan ringkasan dashboard berdasarkan role pengguna.
private fun ActionCard(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                contentDescription = label, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label, 
                style = MaterialTheme.typography.labelMedium, 
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
