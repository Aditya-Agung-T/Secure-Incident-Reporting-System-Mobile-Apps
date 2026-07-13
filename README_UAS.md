# SIRS - Sistem Informasi Respons Insiden Keamanan Siber

SIRS adalah aplikasi Android untuk pelaporan dan penanganan insiden keamanan siber. Pengguna dapat membuat laporan insiden, melampirkan bukti pendukung, memantau status laporan, dan melihat respon penanganan. Admin dapat memantau laporan masuk, mengubah status dan tingkat keparahan, memberikan respon, mengelola kategori, serta melihat aktivitas penting aplikasi.

## Teknologi

- Kotlin
- Jetpack Compose
- MVVM / Clean Architecture
- Firebase Authentication
- Cloud Firestore
- Firebase App Check
- Firebase Analytics / Crashlytics
- Cloudinary untuk upload bukti laporan

## Cara Menjalankan Project

1. Extract file ZIP project.
2. Buka folder project di Android Studio.
3. Pastikan koneksi internet aktif.
4. Tunggu proses Gradle Sync selesai.
5. Jalankan aplikasi ke emulator atau perangkat Android.
6. Login/register akun pengguna melalui aplikasi.

## Konfigurasi Firebase

File `app/google-services.json` sudah disertakan agar project dapat langsung dibuild dan terhubung ke Firebase project yang digunakan saat pengembangan.

Aplikasi menggunakan:

- Firebase Authentication untuk login, registrasi, reset password, dan session pengguna.
- Cloud Firestore untuk menyimpan data profil, laporan insiden, kategori, riwayat status, dan activity log.

## Catatan Akun Admin

Akun admin tidak dibuat melalui halaman registrasi publik. Role admin dikelola melalui data pengguna di Firestore agar pembuatan admin tidak terbuka untuk semua pengguna.

## Catatan Upload Bukti

Upload bukti laporan menggunakan Cloudinary. Aplikasi melakukan validasi file sebelum upload, seperti tipe file, ekstensi, ukuran, dan signature file. Format bukti yang didukung adalah JPG, PNG, dan PDF.

## Catatan Keamanan

Aplikasi menerapkan beberapa fitur keamanan, antara lain:

- Autentikasi pengguna.
- Session management menggunakan Firebase Authentication.
- Validasi password pada registrasi.
- Validasi input laporan.
- Validasi keamanan file upload.
- Pembatasan aksi tertentu menggunakan rate limiting.
- Pencatatan aktivitas penting aplikasi.

## Struktur Penting Project

- `app/src/main/java/com/adit/sirs/` - source code aplikasi Android.
- `app/src/main/res/` - resource UI Android.
- `app/google-services.json` - konfigurasi Firebase Android.
- `firestore.rules` - aturan akses Firestore.
- `firestore.indexes.json` - konfigurasi index Firestore.
- `docs/` - dokumentasi tambahan project.
