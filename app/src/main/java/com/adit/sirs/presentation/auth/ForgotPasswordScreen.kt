package com.adit.sirs.presentation.auth

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.presentation.components.SirsTextField

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Mengatur reset password atau reset state agar UI kembali ke kondisi awal.
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val resetState by viewModel.resetPasswordState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Masukkan email untuk menerima tautan reset password.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SirsTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (resetState) {
                is UiState.Error -> {
                    Text(
                        text = (resetState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                is UiState.Success -> {
                    Text(
                        text = "Email reset terkirim. Periksa kotak masuk Anda.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                else -> {}
            }

            Button(
                onClick = { viewModel.sendPasswordReset(email.trim()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is UiState.Loading && email.isNotBlank()
            ) {
                if (resetState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Kirim Email Reset")
                }
            }
        }
    }
}
