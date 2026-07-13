# SIRS — Secure Incident Reporting System (Android)

Aplikasi pelaporan insiden aman (SIRS) untuk Android, dibangun dengan Kotlin + Jetpack Compose,
Hilt, Firebase (Auth/Firestore), dan Cloudinary (upload bukti laporan).

## Prasyarat (wajib agar bisa compile)

- **JDK 17 atau lebih baru** (build ini diuji dengan JDK 21).
- **Android SDK Platform 36** dan **Android SDK Build-Tools 36** (install lewat SDK Manager).
- **Android Studio** (versi terbaru direkomendasikan) — atau cukup command line + Android SDK.
- Koneksi internet saat build **pertama** (Gradle 9.3.1 dan dependensi di-download otomatis
  lewat Gradle Wrapper; build berikutnya offline-friendly).

## Cara compile / build

Clone lalu build langsung dengan Gradle Wrapper (tidak perlu install Gradle manual):

```bash
git clone <repo-url> SIRS
cd SIRS
./gradlew assembleDebug        # Linux/macOS
gradlew.bat assembleDebug      # Windows
```

Atau buka folder ini di Android Studio dan jalankan `Run 'app'` (Shift+F10).

APK hasil build: `app/build/outputs/apk/debug/app-debug.apk`

## Catatan konfigurasi

- `local.properties` (path SDK lokal) **tidak di-commit** — akan dibuat otomatis oleh
  Android Studio, atau buat manual: `sdk.dir=/path/ke/Android/Sdk`.
- `google-services.json` sudah disertakan di `app/` (wajib untuk build Firebase).
- `CLOUDINARY_DELETE_ENDPOINT` diambil dari `gradle.properties`; jika kosong, fitur hapus
  file Cloudinary tidak aktif tetapi aplikasi tetap ter-compile dan jalan.

## Tech stack

AGP 9.1.1 · Gradle 9.3.1 · Kotlin 2.2.10 · compileSdk/targetSdk 36 · minSdk 26 ·
Jetpack Compose (BOM 2025.06.01) · Hilt · Firebase BOM 33.15.0 · Coil 3 · OkHttp 4.
