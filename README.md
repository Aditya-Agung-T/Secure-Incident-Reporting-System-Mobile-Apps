# SIRS — Secure Incident Reporting System (Android)

Aplikasi pelaporan insiden aman (SIRS) untuk Android. Pengguna dapat membuat laporan
insiden, mengunggah bukti gambar, dan melacak status laporan. Sisi admin dapat memverifikasi
dan memproses laporan. Keamanan menjadi fokus utama: enkripsi lokal, autentikasi Firebase,
dan proteksi akses berbasis peran (user vs admin).

Dibangun dengan **Kotlin + Jetpack Compose**, **Hilt** (DI), **Firebase**
(Authentication / Firestore / Storage), dan **Cloudinary** (upload bukti laporan).

Repository: https://github.com/Aditya-Agung-T/Secure-Incident-Reporting-System-Mobile-Apps

## Fitur utama

- Autentikasi email/password via Firebase Authentication.
- Pembuatan laporan insiden dengan unggah bukti gambar (Cloudinary + Firebase Storage).
- Pelacakan status laporan (diajukan → diproses → selesai) secara real-time (Firestore).
- Peran ganda: **User** (buat & pantau laporan) dan **Admin** (verifikasi & kelola laporan).
- Profil pengguna (lihat & edit data diri, logout).
- Keamanan: enkripsi data sensitif di perangkat, validasi input, akses berbasis peran.

## Prasyarat (wahan agar bisa compile)

- **JDK 17 atau lebih baru** (build ini diuji dengan JDK 21).
- **Android SDK Platform 36** dan **Android SDK Build-Tools 36** (install lewat SDK Manager).
- **Android Studio** (versi terbaru direkomendasikan) atau command line + Android SDK.
- Koneksi internet saat build **pertama** — Gradle 9.3.1 dan seluruh dependensi di-download
  otomatis lewat Gradle Wrapper (tidak perlu install Gradle manual).

## Cara compile / build

Clone lalu build langsung dengan Gradle Wrapper:

```bash
# Linux / macOS
git clone https://github.com/Aditya-Agung-T/Secure-Incident-Reporting-System-Mobile-Apps.git
cd Secure-Incident-Reporting-System-Mobile-Apps
./gradlew assembleDebug

# Windows
git clone https://github.com/Aditya-Agung-T/Secure-Incident-Reporting-System-Mobile-Apps.git
cd Secure-Incident-Reporting-System-Mobile-Apps
gradlew.bat assembleDebug
```

Atau buka folder ini di Android Studio dan jalankan `Run 'app'` (Shift+F10).

APK hasil build: `app/build/outputs/apk/debug/app-debug.apk`

## Catatan konfigurasi

- `local.properties` (path SDK lokal) **tidak di-commit** — dibuat otomatis oleh Android Studio,
  atau buat manual: `sdk.dir=/path/ke/Android/Sdk`.
- `google-services.json` sudah disertakan di `app/` (wajib untuk build Firebase).
- `CLOUDINARY_DELETE_ENDPOINT` diambil dari `gradle.properties`; jika kosong, fitur hapus file
  Cloudinary tidak aktif namun aplikasi tetap ter-compile dan jalan.
- Firebase, Cloudinary, dan layanan backend harus sudah terkonfigurasi agar fitur penuh berjalan.
  Kode akan ter-compile dan aplikasi akan terbuka meski backend belum terhubung.

## Arsitektur

Menggunakan arsitektur bersih (Clean Architecture) dengan pemisahan layer:

```
app/src/main/java/com/adit/sirs/
├── core/            # util, security, error, constants, firebase helper
├── data/            # model, repository impl, remote source, DI (Hilt)
├── domain/          # model & interface repository (tidak bergantung framework)
├── presentation/    # UI (Compose): auth, dashboard, reports, admin, profile, navigation
└── ui/theme/        # tema & styling Compose
```

## Tech stack

| Komponen        | Versi / Teknologi                              |
|-----------------|------------------------------------------------|
| AGP             | 9.1.1                                          |
| Gradle          | 9.3.1 (via wrapper)                            |
| Kotlin          | 2.2.10                                         |
| compileSdk / targetSdk | 36                                       |
| minSdk          | 26                                             |
| UI              | Jetpack Compose BOM 2025.06.01                 |
| DI              | Hilt                                           |
| Backend         | Firebase BOM 33.15.0 (Auth, Firestore, Storage)|
| Gambar          | Coil 3                                         |
| Networking      | OkHttp 4                                       |

## Struktur repo

```
.
├── app/                      # Modul aplikasi Android (source + manifest + google-services.json)
├── docs/                     # Dokumentasi teknis (Firebase, Cloudinary, setup, security)
├── gradle/                   # Gradle wrapper
├── build.gradle.kts          # Build config level root
├── settings.gradle.kts       # Konfigurasi modul
├── gradle.properties         # Properti build (termasuk endpoint Cloudinary)
├── firebase.json             # Konfigurasi Firebase (rules/indexes)
├── firestore.rules           # Aturan keamanan Firestore
├── firestore.indexes.json    # Index Firestore
├── ketentuan.txt             # Ketentuan umum projek akhir
└── README.md
```
