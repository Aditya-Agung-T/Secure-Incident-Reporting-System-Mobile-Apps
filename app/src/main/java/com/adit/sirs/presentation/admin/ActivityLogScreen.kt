package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.core.util.DateFormatter
import com.adit.sirs.domain.model.ActivityLog
import com.adit.sirs.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `ActivityLogScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
fun ActivityLogScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val logsState by viewModel.activityLogs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadActivityLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Aktivitas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = logsState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorView(state.message, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState("Belum ada log aktivitas", modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data, key = { it.id }) { log ->
                            ActivityLogCard(log)
                        }
                    }
                }
            }
            is UiState.Idle -> {}
        }
    }
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `ActivityLogCard` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
private fun ActivityLogCard(log: ActivityLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(log.action, style = MaterialTheme.typography.titleSmall)
            Text(log.description, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Oleh: ${log.actorName ?: "Sistem"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    DateFormatter.formatTimestamp(log.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
