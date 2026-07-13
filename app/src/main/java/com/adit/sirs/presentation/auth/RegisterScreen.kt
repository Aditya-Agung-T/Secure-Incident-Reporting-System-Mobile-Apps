package com.adit.sirs.presentation.auth

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.adit.sirs.core.common.UiState
import com.adit.sirs.presentation.components.SirsTextField

@OptIn(ExperimentalMaterial3Api::class)
// FLOW: Annotation ini menandai fungsi sebagai UI Jetpack Compose yang dirender di layar.
@Composable
// FLOW: Memproses pendaftaran akun user baru dan menyiapkan profil awal di Firestore.
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordRules = listOf(
        "Minimal 12 karakter" to (password.length >= 12),
        "Memiliki huruf besar" to password.any { it.isUpperCase() },
        "Memiliki huruf kecil" to password.any { it.isLowerCase() },
        "Memiliki angka" to password.any { it.isDigit() },
        "Memiliki karakter khusus/simbol (! @ # $ % dll.)" to password.any { !it.isLetterOrDigit() }
    )
    val passwordError = when {
        password.isEmpty() -> null
        passwordRules.any { !it.second } -> "Password belum memenuhi standar keamanan"
        else -> null
    }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            onRegisterSuccess()
            viewModel.resetRegisterState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Akun") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SirsTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama Lengkap"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SirsTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SirsTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = passwordError,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Standar password OWASP/NIST:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                passwordRules.forEach { (rule, isValid) ->
                    Text(
                        text = "${if (isValid) "✓" else "•"} $rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            password.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                            isValid -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SirsTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Password",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = if (confirmPassword.isNotEmpty() && password != confirmPassword) "Konfirmasi password tidak sama" else null
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (registerState is UiState.Error) {
                Text(
                    text = (registerState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.register(name.trim(), email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = registerState !is UiState.Loading
                        && name.isNotBlank()
                        && email.isNotBlank()
                        && password.isNotBlank()
                        && passwordError == null
                        && password == confirmPassword
            ) {
                if (registerState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Daftar")
                }
            }
        }
    }
}
