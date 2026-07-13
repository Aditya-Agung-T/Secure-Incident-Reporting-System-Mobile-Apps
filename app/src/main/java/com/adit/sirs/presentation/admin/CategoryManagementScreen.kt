package com.adit.sirs.presentation.admin

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.domain.model.Category
import com.adit.sirs.presentation.components.*
import com.adit.sirs.presentation.theme.StatusResolved
import com.adit.sirs.presentation.theme.StatusRejected

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `CategoryManagementScreen` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
fun CategoryManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val categoriesState by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }
    var mitigationTipsText by remember { mutableStateOf("") }
    var recommendedEvidenceText by remember { mutableStateOf("") }
    var deletingCategory by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kategori") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCategory = null
                categoryName = ""
                categoryDescription = ""
                mitigationTipsText = ""
                recommendedEvidenceText = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { padding ->
        when (val state = categoriesState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorView(state.message, onRetry = { viewModel.loadCategories() }, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState("Belum ada kategori", modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                onEdit = {
                                    editingCategory = category
                                    categoryName = category.name
                                    categoryDescription = category.description ?: ""
                                    mitigationTipsText = category.mitigationTips.joinToString("\n")
                                    recommendedEvidenceText = category.recommendedEvidence.joinToString("\n")
                                    showDialog = true
                                },
                                onToggle = { viewModel.toggleCategory(category.id, !category.isActive) },
                                onDelete = { deletingCategory = category }
                            )
                        }
                    }
                }
            }
            is UiState.Idle -> {}
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingCategory == null) "Buat Kategori" else "Edit Kategori") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SirsTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = "Nama"
                    )
                    SirsTextField(
                        value = categoryDescription,
                        onValueChange = { categoryDescription = it },
                        label = "Deskripsi (opsional)",
                        singleLine = false,
                        minLines = 2
                    )
                    SirsTextField(
                        value = mitigationTipsText,
                        onValueChange = { mitigationTipsText = it },
                        label = "Panduan tindakan awal (satu baris per item)",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 4
                    )
                    SirsTextField(
                        value = recommendedEvidenceText,
                        onValueChange = { recommendedEvidenceText = it },
                        label = "Bukti yang disarankan (satu baris per item)",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val mitigationTips = mitigationTipsText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        val recommendedEvidence = recommendedEvidenceText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        if (editingCategory != null) {
                            viewModel.updateCategory(editingCategory!!.id, categoryName, categoryDescription.ifBlank { null }, mitigationTips, recommendedEvidence)
                        } else {
                            viewModel.createCategory(categoryName, categoryDescription.ifBlank { null }, mitigationTips, recommendedEvidence)
                        }
                        showDialog = false
                    },
                    enabled = categoryName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Hapus Kategori") },
            text = { Text("Kategori \"${category.name}\" akan dihapus dari daftar admin dan tidak bisa dipilih user untuk laporan baru. Laporan lama tetap aman.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id, category.name)
                        deletingCategory = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text("Batal") }
            }
        )
    }

}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `CategoryCard` menjalankan langkah khusus pada file ini dan menjaga alur mengatur fitur admin seperti monitoring laporan, status, kategori, dan activity log.
private fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                if (category.description != null) {
                    Text(category.description, style = MaterialTheme.typography.bodySmall)
                }
                if (category.mitigationTips.isNotEmpty()) {
                    Text("${category.mitigationTips.size} panduan tindakan", style = MaterialTheme.typography.bodySmall)
                }
                if (category.recommendedEvidence.isNotEmpty()) {
                    Text("${category.recommendedEvidence.size} bukti disarankan", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    if (category.isActive) "Aktif" else "Nonaktif",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (category.isActive) StatusResolved else StatusRejected
                )
            }
            Row {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onToggle) {
                    Text(if (category.isActive) "Nonaktifkan" else "Aktifkan")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            }
        }
    }
}
