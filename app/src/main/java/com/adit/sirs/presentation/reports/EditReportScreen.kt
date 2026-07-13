package com.adit.sirs.presentation.reports

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MenuAnchorType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.core.util.FileSizeFormatter
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.presentation.components.SirsTextField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `EditReportScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur alur laporan insiden dari form, daftar, detail, edit, sampai submit.
fun EditReportScreen(
    reportId: String,
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    title: String = "Edit Laporan",
    saveLabel: String = "Simpan Perubahan"
) {
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val reportDetail by viewModel.reportDetail.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    var categoryExpanded by remember { mutableStateOf(false) }
    var severityExpanded by remember { mutableStateOf(false) }
    var showValidation by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        viewModel.loadReportDetail(reportId)
    }

    LaunchedEffect(reportDetail) {
        if (!prefilled && reportDetail is UiState.Success) {
            val report = (reportDetail as UiState.Success).data
            viewModel.prefillForm(report)
            prefilled = true
        }
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            onSuccess()
            viewModel.resetSubmitState()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setAttachment(contentResolver, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SirsTextField(
                value = formState.title,
                onValueChange = { viewModel.updateFormField { copy(title = it) } },
                label = "Judul *",
                errorMessage = if (showValidation) formState.titleError else null
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                SirsTextField(
                    value = formState.categoryName,
                    onValueChange = {},
                    label = "Kategori *",
                    readOnly = true,
                    errorMessage = if (showValidation) formState.categoryError else null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                viewModel.updateFormField { copy(categoryId = cat.id, categoryName = cat.name) }
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            SirsTextField(
                value = formState.description,
                onValueChange = { viewModel.updateFormField { copy(description = it) } },
                label = "Deskripsi *",
                singleLine = false,
                minLines = 4,
                maxLines = 8,
                errorMessage = if (showValidation) formState.descriptionError else null
            )

            SirsTextField(
                value = formState.location,
                onValueChange = { viewModel.updateFormField { copy(location = it) } },
                label = "Lokasi *",
                errorMessage = if (showValidation) formState.locationError else null
            )

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            SirsTextField(
                value = if (formState.incidentDate != null) dateFormat.format(Date(formState.incidentDate!!)) else "",
                onValueChange = {},
                label = "Tanggal Insiden *",
                readOnly = true,
                errorMessage = if (showValidation) formState.dateError else null,
                trailingIcon = {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        if (formState.incidentDate != null) cal.timeInMillis = formState.incidentDate!!
                        DatePickerDialog(context, { _, year, month, day ->
                            cal.set(year, month, day, 0, 0, 0)
                            viewModel.updateFormField { copy(incidentDate = cal.timeInMillis) }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                            datePicker.maxDate = System.currentTimeMillis()
                        }.show()
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pilih tanggal")
                    }
                }
            )

            ExposedDropdownMenuBox(
                expanded = severityExpanded,
                onExpandedChange = { severityExpanded = it }
            ) {
                SirsTextField(
                    value = Severity.fromString(formState.severity).displayName,
                    onValueChange = {},
                    label = "Severity *",
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = severityExpanded,
                    onDismissRequest = { severityExpanded = false }
                ) {
                    Severity.entries.forEach { sev ->
                        DropdownMenuItem(
                            text = { Text(sev.displayName) },
                            onClick = {
                                viewModel.updateFormField { copy(severity = sev.value) }
                                severityExpanded = false
                            }
                        )
                    }
                }
            }

            // Attachment
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lampiran (opsional)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (formState.attachmentName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(formState.attachmentName!!, style = MaterialTheme.typography.bodyMedium)
                                Text(FileSizeFormatter.format(formState.attachmentSize), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.clearAttachment() }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus")
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { filePicker.launch(arrayOf("image/jpeg", "image/png", "application/pdf")) }
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pilih File")
                        }
                    }
                }
            }

            if (submitState is UiState.Error) {
                Text(
                    text = (submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    showValidation = true
                    if (formState.isValid) {
                        viewModel.updateReport(reportId, contentResolver)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = submitState !is UiState.Loading
            ) {
                if (submitState is UiState.Loading || uploadProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uploadProgress) "Mengunggah..." else "Menyimpan...")
                } else {
                    Text(saveLabel)
                }
            }
        }
    }
}
