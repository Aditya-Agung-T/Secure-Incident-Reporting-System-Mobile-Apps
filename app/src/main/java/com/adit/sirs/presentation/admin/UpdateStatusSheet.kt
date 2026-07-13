package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.IncidentStatus
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.presentation.components.SirsTextField

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `UpdateStatusSheet` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
fun UpdateStatusSheet(
    currentStatus: String,
    currentSeverity: String,
    currentResponse: String?,
    updateState: UiState<Unit>,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, newSeverity: String, response: String?) -> Unit
) {
    var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }
    var selectedSeverity by remember(currentSeverity) { mutableStateOf(currentSeverity) }
    var response by remember(currentResponse) { mutableStateOf(currentResponse ?: "") }
    var statusExpanded by remember { mutableStateOf(false) }
    var severityExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Update Status & Severity", style = MaterialTheme.typography.titleLarge)

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    SirsTextField(
                        value = IncidentStatus.fromString(selectedStatus).displayName,
                        onValueChange = {},
                        label = "Status",
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        IncidentStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    selectedStatus = status.value
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = severityExpanded,
                    onExpandedChange = { severityExpanded = it }
                ) {
                    SirsTextField(
                        value = Severity.fromString(selectedSeverity).displayName,
                        onValueChange = {},
                        label = "Severity",
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = severityExpanded,
                        onDismissRequest = { severityExpanded = false }
                    ) {
                        Severity.entries.forEach { severity ->
                            DropdownMenuItem(
                                text = { Text(severity.displayName) },
                                onClick = {
                                    selectedSeverity = severity.value
                                    severityExpanded = false
                                }
                            )
                        }
                    }
                }

                SirsTextField(
                    value = response,
                    onValueChange = { response = it },
                    label = "Respon Admin (opsional)",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5
                )

                if (updateState is UiState.Error) {
                    Text(
                        updateState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { onUpdate(selectedStatus, selectedSeverity, response.ifBlank { null }) },
                        modifier = Modifier.weight(1f),
                        enabled = updateState !is UiState.Loading &&
                            (selectedStatus != currentStatus ||
                                selectedSeverity != currentSeverity ||
                                response.trim() != (currentResponse ?: ""))
                    ) {
                        if (updateState is UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}
