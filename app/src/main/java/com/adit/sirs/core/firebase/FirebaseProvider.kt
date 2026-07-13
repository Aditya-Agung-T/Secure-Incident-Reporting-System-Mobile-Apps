package com.adit.sirs.core.firebase

// DOKUMENTASI ALUR SIRS
// Komentar berikut menjelaskan peran kode dengan bahasa sederhana,
// sehingga alur fitur dapat dibaca dari source code saat presentasi atau maintenance.


import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// FLOW: `FirebaseProvider` berisi helper/konstanta bersama yang dipakai oleh beberapa file.
object FirebaseProvider {

    @Provides
    @Singleton
    // FLOW: Fungsi `provideFirebaseAuth` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    // FLOW: Fungsi `provideFirebaseFirestore` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    // FLOW: Fungsi `provideFirebaseMessaging` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
    
    @Provides
    @Singleton
    // FLOW: Fungsi `provideFirebaseAnalytics` menjalankan langkah khusus pada file ini dan menjaga alur menyiapkan helper dan provider Firebase.
    fun provideFirebaseAnalytics(): FirebaseAnalytics = FirebaseAnalytics.getInstance(com.google.firebase.FirebaseApp.getInstance().applicationContext)
}
