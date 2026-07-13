package com.adit.sirs.presentation.components

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adit.sirs.domain.model.Severity
import com.adit.sirs.presentation.theme.*

// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Fungsi `SeverityBadge` menjalankan langkah khusus pada file ini dan menjaga alur menyediakan komponen UI reusable agar tampilan konsisten.
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (severity) {
        Severity.LOW -> SeverityLow.copy(alpha = 0.15f) to SeverityLow
        Severity.MEDIUM -> SeverityMedium.copy(alpha = 0.15f) to SeverityMedium
        Severity.HIGH -> SeverityHigh.copy(alpha = 0.15f) to SeverityHigh
        Severity.CRITICAL -> SeverityCritical.copy(alpha = 0.15f) to SeverityCritical
    }

    Text(
        text = severity.displayName,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
