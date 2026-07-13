package com.adit.sirs

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.adit.sirs.presentation.navigation.AppNavGraph
import com.adit.sirs.presentation.theme.SIRSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// FLOW: `MainActivity` menjadi bagian dari alur utama aplikasi SIRS.
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, continue */ }

    // FLOW: Fungsi `onCreate` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            SIRSTheme {
                AppNavGraph()
            }
        }
    }

    // FLOW: Fungsi `requestNotificationPermission` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
