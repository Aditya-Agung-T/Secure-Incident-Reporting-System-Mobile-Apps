# Panduan Menjalankan Project

## Kebutuhan

- Android Studio versi terbaru yang mendukung Kotlin dan Jetpack Compose.
- JDK 17.
- Android SDK sesuai konfigurasi project.
- Emulator Android atau perangkat Android fisik.
- Koneksi internet aktif.

## Langkah Menjalankan

1. Extract file ZIP source code.
2. Buka folder project menggunakan Android Studio.
3. Tunggu proses Gradle Sync sampai selesai.
4. Pastikan file `app/google-services.json` tersedia.
5. Jalankan aplikasi ke emulator atau perangkat Android.
6. Login atau registrasi akun melalui aplikasi.

## Firebase

Project ini menggunakan Firebase Authentication dan Cloud Firestore. File `app/google-services.json` sudah disertakan agar aplikasi dapat langsung terhubung ke Firebase project yang digunakan saat pengembangan.

Jika menjalankan project di laptop lain, pastikan koneksi internet aktif agar proses autentikasi dan akses data Firestore dapat berjalan.

## Akun Admin

Akun admin tidak dibuat dari halaman registrasi umum. Untuk menjadikan akun sebagai admin, field `role` pengguna di collection `users` pada Firestore perlu diubah menjadi `administrator`.

## Upload Bukti Laporan

Aplikasi mendukung upload bukti laporan dalam format JPG, PNG, dan PDF. Upload membutuhkan koneksi internet.

## Build Manual

Jika ingin build melalui terminal:

```bash
./gradlew :app:assembleDebug
```

Pada Windows juga dapat menggunakan:

```bash
gradlew.bat :app:assembleDebug
```

## Catatan local.properties

File `local.properties` tidak disertakan dalam ZIP karena berisi lokasi Android SDK di laptop masing-masing. Android Studio biasanya akan membuat file ini otomatis saat project dibuka.

Jika build dari terminal gagal dengan pesan SDK location not found, buka project melalui Android Studio terlebih dahulu atau buat `local.properties` yang berisi path Android SDK lokal.
