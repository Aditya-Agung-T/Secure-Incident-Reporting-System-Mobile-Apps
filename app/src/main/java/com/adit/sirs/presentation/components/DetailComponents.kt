package com.adit.sirs.presentation.components

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.util.DateFormatter
import com.adit.sirs.domain.model.StatusHistory
import com.adit.sirs.domain.model.IncidentStatus
import java.util.Locale

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `DetailRow` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan komponen UI reusable agar tampilan konsisten.
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `StatusHistoryItem` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan komponen UI reusable agar tampilan konsisten.
fun StatusHistoryItem(history: StatusHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp).size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${history.toStatus.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    DateFormatter.formatTimestamp(history.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Diperbarui oleh: ${history.changedByName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!history.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "\"${history.note}\"", 
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `AttachmentCard` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan komponen UI reusable agar tampilan konsisten.
fun AttachmentCard(
    originalName: String,
    bytes: Long,
    format: String,
    mimeType: String = "",
    secureUrl: String,
    onOpen: (String) -> Unit
) {
    val displayFormat = rememberAttachmentFormat(format, mimeType, originalName)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lampiran Bukti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow("Nama File", originalName)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Ukuran", com.adit.sirs.core.util.FileSizeFormatter.format(bytes))
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("Format", displayFormat)
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onOpen(secureUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Lampiran", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// FLOW: Fungsi `rememberAttachmentFormat` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan komponen UI reusable agar tampilan konsisten.
private fun rememberAttachmentFormat(format: String, mimeType: String, originalName: String): String {
    val normalizedFormat = format.trim()
    if (normalizedFormat.isNotBlank()) return normalizedFormat.uppercase(Locale.ROOT)

    val normalizedMimeType = mimeType.trim()
    if (normalizedMimeType.isNotBlank()) {
        return when (normalizedMimeType.lowercase(Locale.ROOT)) {
            "image/jpeg" -> "JPG"
            "image/png" -> "PNG"
            "image/webp" -> "WEBP"
            "application/pdf" -> "PDF"
            else -> normalizedMimeType.substringAfterLast('/').uppercase(Locale.ROOT)
        }
    }

    val extension = originalName.substringAfterLast('.', missingDelimiterValue = "").trim()
    return extension.ifBlank { "Tidak diketahui" }.uppercase(Locale.ROOT)
}
