# Project Overview

## Judul

SIRS - Sistem Informasi Respons Insiden Keamanan Siber

## Deskripsi Singkat

SIRS adalah aplikasi Android untuk membantu proses pelaporan dan penanganan insiden keamanan siber. Aplikasi ini menyediakan alur pelaporan bagi pengguna dan alur pemantauan bagi admin, sehingga laporan insiden dapat dicatat, dipantau, ditindaklanjuti, dan terdokumentasi dengan lebih baik.

Pengguna dapat membuat laporan insiden dengan mengisi informasi kejadian, memilih kategori, menentukan tingkat keparahan, menambahkan lokasi dan tanggal kejadian, serta melampirkan bukti pendukung. Admin dapat memantau laporan yang masuk, mengubah status penanganan, menentukan tingkat keparahan, memberikan respon, dan mengelola kategori laporan.

## Tujuan Aplikasi

- Memudahkan pengguna dalam melaporkan insiden keamanan siber.
- Membantu admin memantau dan menangani laporan secara terstruktur.
- Menyediakan dokumentasi riwayat laporan dan perubahan status.
- Memberikan panduan awal berdasarkan kategori insiden.
- Menentukan batas waktu penanganan berdasarkan tingkat keparahan laporan.

## Peran Pengguna

### Pengguna

- Registrasi dan login akun.
- Membuat laporan insiden.
- Melihat daftar dan detail laporan sendiri.
- Mengedit atau menghapus laporan yang masih berstatus pending.
- Melihat respon dan perkembangan status laporan.

### Admin

- Melihat seluruh laporan pengguna.
- Mencari dan memfilter laporan.
- Mengubah status dan tingkat keparahan laporan.
- Memberikan respon atau catatan penanganan.
- Mengelola kategori laporan.
- Melihat aktivitas penting aplikasi.

## Fitur Utama

1. Login, registrasi, dan reset password.
2. Dashboard pengguna dan dashboard admin.
3. Pembuatan laporan insiden keamanan siber.
4. Upload bukti pendukung laporan.
5. Daftar dan detail laporan.
6. Filter laporan berdasarkan status dan tingkat keparahan.
7. Pencarian laporan untuk admin.
8. Perubahan status dan tingkat keparahan oleh admin.
9. Respon penanganan dari admin.
10. Riwayat perubahan status laporan.
11. Manajemen kategori laporan.
12. Panduan tindakan awal berdasarkan kategori insiden.
13. Perhitungan batas waktu penanganan berdasarkan tingkat keparahan.
14. Pencatatan aktivitas penting aplikasi.

## Teknologi

- Kotlin
- Jetpack Compose
- MVVM / Clean Architecture
- Firebase Authentication
- Cloud Firestore
- Firebase App Check
- Firebase Analytics / Crashlytics
- Cloudinary untuk upload bukti laporan

## Struktur Umum Project

- `app/src/main/java/com/adit/sirs/core/`  
  Berisi utilitas umum, konstanta, error handler, validator keamanan, dan helper Firebase.

- `app/src/main/java/com/adit/sirs/data/`  
  Berisi DTO, mapper, data source Firebase/Cloudinary, dan implementasi repository.

- `app/src/main/java/com/adit/sirs/domain/`  
  Berisi model domain dan interface repository.

- `app/src/main/java/com/adit/sirs/presentation/`  
  Berisi screen Jetpack Compose, ViewModel, komponen UI, navigasi, dan theme aplikasi.

## Catatan Penilaian

Aplikasi dibuat dengan Kotlin dan Jetpack Compose sesuai ketentuan project akhir. Struktur kode dibuat menggunakan pendekatan MVVM/Clean Architecture agar pemisahan tanggung jawab antar layer lebih jelas dan mudah dikembangkan.
