package com.adit.sirs

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// FLOW: `SirsFirebaseMessagingService` menjadi bagian dari alur utama aplikasi SIRS.
class SirsFirebaseMessagingService : FirebaseMessagingService() {

    // FLOW: Fungsi `onNewToken` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token refresh handled by FcmTokenDataSource on next login
    }

    // FLOW: Fungsi `onMessageReceived` menjalankan langkah khusus pada file ini dan menjaga alur menjadi bagian dari alur utama aplikasi SIRS.
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Handle FCM message if needed
    }
}
