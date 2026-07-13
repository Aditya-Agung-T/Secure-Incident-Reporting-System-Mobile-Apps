# Dokumentasi Tech Stack, Environment, dan Konfigurasi SIRS

Dokumen ini menjelaskan teknologi yang dipakai pada project SIRS, lokasi konfigurasi, serta pemakaian setiap teknologi di dalam source code. Isi dokumen disusun berdasarkan file project yang ditemukan saat inspeksi.

## 1. Identitas Project

| Item | Nilai | Lokasi |
|---|---|---|
| Nama project Gradle | `SIRS` | `settings.gradle.kts:25` |
| Module aplikasi | `:app` | `settings.gradle.kts:26` |
| Package/namespace Android | `com.adit.sirs` | `app/build.gradle.kts:11`, `app/build.gradle.kts:15` |
| Platform | Android native | `app/build.gradle.kts` |
| Bahasa utama | Kotlin | `gradle/libs.versions.toml:3` |
| UI framework | Jetpack Compose + Material 3 | `app/build.gradle.kts:53-55`, `gradle/libs.versions.toml:32-41` |
| Arsitektur kode | MVVM + Clean Architecture ringan | `presentation/`, `domain/`, `data/`, `core/` |
| Backend utama | Firebase | `app/google-services.json`, `firebase.json`, `firestore.rules` |
| Upload media | Cloudinary via HTTP API | `CloudinaryUploadDataSource.kt` |

## 2. Build System dan Toolchain

| Teknologi | Versi/Konfigurasi | Lokasi | Dipakai untuk |
|---|---|---|---|
| Gradle Kotlin DSL | `.gradle.kts` | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts` | Konfigurasi build Android, plugin, dependency, BuildConfig. |
| Android Gradle Plugin | `9.1.1` | `gradle/libs.versions.toml:2` | Build aplikasi Android. |
| Kotlin | `2.2.10` | `gradle/libs.versions.toml:3` | Bahasa utama aplikasi. |
| KSP | `2.2.10-2.0.2` | `gradle/libs.versions.toml:19` | Annotation processing untuk Hilt. |
| Java compatibility | Java 17 | `app/build.gradle.kts:49-52` | Target source/bytecode Java. |
| Compile SDK | `36` | `app/build.gradle.kts:12` | SDK untuk compile aplikasi. |
| Minimum SDK | `26` | `app/build.gradle.kts:16` | Minimal Android yang didukung. |
| Target SDK | `36` | `app/build.gradle.kts:17` | Target behavior Android. |
| Gradle JVM args | `-Xmx2048m -Dfile.encoding=UTF-8` | `gradle.properties:9` | Memory dan encoding build. |
| Kotlin code style | `official` | `gradle.properties:15` | Style Kotlin. |

Perintah build utama:

```bash
./gradlew assembleDebug
```

Perintah deploy Firestore rules yang sudah dipakai:

```bash
npx firebase-tools deploy --only firestore:rules --project androapp-f262d --non-interactive
```

## 3. Plugin Gradle yang Dipakai

| Plugin | Versi | Lokasi versi | Dipasang di | Fungsi |
|---|---:|---|---|---|
| `com.android.application` | `9.1.1` | `gradle/libs.versions.toml:81` | `app/build.gradle.kts:2` | Membuild module Android application. |
| `org.jetbrains.kotlin.plugin.compose` | `2.2.10` | `gradle/libs.versions.toml:82` | `app/build.gradle.kts:3` | Mengaktifkan compiler plugin Compose untuk Kotlin. |
| `com.google.devtools.ksp` | `2.2.10-2.0.2` | `gradle/libs.versions.toml:83` | `app/build.gradle.kts:4` | KSP untuk Hilt compiler. |
| `com.google.dagger.hilt.android` | `2.60` | `gradle/libs.versions.toml:84` | `app/build.gradle.kts:5` | Dependency Injection Hilt. |
| `com.google.gms.google-services` | `4.5.0` | `gradle/libs.versions.toml:85` | `app/build.gradle.kts:6` | Membaca `google-services.json` dan menghubungkan app ke Firebase. |
| `com.google.firebase.crashlytics` | `3.0.3` | `gradle/libs.versions.toml:86` | `app/build.gradle.kts:7` | Integrasi Firebase Crashlytics. |
| `org.gradle.toolchains.foojay-resolver-convention` | `1.0.0` | `settings.gradle.kts:15` | `settings.gradle.kts` | Membantu resolusi Java toolchain Gradle. |

## 4. Struktur Layer dan Lokasi Pemakaian

| Layer | Lokasi | Jumlah file Kotlin terdeteksi | Fungsi |
|---|---|---:|---|
| Core | `app/src/main/java/com/adit/sirs/core/` | 13 | Konstanta, error mapper, rate limiter, security validation, Firebase helper, formatter. |
| Data | `app/src/main/java/com/adit/sirs/data/` | 22 | DTO, mapper, remote data source, repository implementation, dependency binding. |
| Domain | `app/src/main/java/com/adit/sirs/domain/` | 13 | Model bisnis dan kontrak repository. |
| Presentation | `app/src/main/java/com/adit/sirs/presentation/` | 35 | Screen Compose, ViewModel, navigasi, komponen UI, theme. |
| Entry point | `app/src/main/java/com/adit/sirs/` | 3 file utama | `MainActivity`, `SirsApplication`, FCM service. |

Alur umum aplikasi:

`Screen Compose -> ViewModel -> Repository interface -> Repository implementation -> Data source -> Firebase/Cloudinary -> Mapper -> Domain model -> StateFlow -> UI`

## 5. Android Native dan Manifest

| Komponen | Lokasi | Pemakaian |
|---|---|---|
| Manifest utama | `app/src/main/AndroidManifest.xml` | Deklarasi permission, application class, launcher activity, FCM service. |
| Permission internet | `AndroidManifest.xml:5` | Diperlukan untuk Firebase dan Cloudinary. |
| Permission notifikasi | `AndroidManifest.xml:6` | Diperlukan untuk Android 13+ saat memakai FCM/notification permission. |
| Application class | `AndroidManifest.xml:9`, `SirsApplication.kt` | Inisialisasi Firebase, App Check, Firestore offline persistence. |
| Launcher activity | `AndroidManifest.xml:19-28`, `MainActivity.kt` | Entry point UI Compose. |
| FCM service | `AndroidManifest.xml:30-36`, `SirsFirebaseMessagingService.kt` | Menerima event Firebase Messaging/token refresh. |
| Backup rules | `AndroidManifest.xml:11-12` | Mengarah ke konfigurasi XML backup/data extraction. |

## 6. Jetpack Compose dan Material 3

| Teknologi | Lokasi konfigurasi | Lokasi pemakaian | Fungsi |
|---|---|---|---|
| Compose build feature | `app/build.gradle.kts:53-55` | Seluruh `presentation/**Screen.kt` | UI deklaratif Android. |
| Compose BOM | `app/build.gradle.kts:68-74`, `gradle/libs.versions.toml:32-41` | Dependency Compose UI, graphics, tooling, Material 3. | Menyamakan versi library Compose. |
| Material 3 | `gradle/libs.versions.toml:40` | `presentation/theme/`, semua screen | Komponen UI modern Android. |
| Material Icons Extended | `gradle/libs.versions.toml:41` | Screen dan komponen UI | Icon UI. |
| Activity Compose | `gradle/libs.versions.toml:29` | `MainActivity.kt:34-37` | `setContent { SIRSTheme { AppNavGraph() } }`. |
| Compose Preview/Tooling | `app/build.gradle.kts:72`, `app/build.gradle.kts:116` | Debug/preview UI | Preview dan inspeksi UI di Android Studio. |

Screen Compose yang ditemukan:

- `presentation/auth/LoginScreen.kt`
- `presentation/auth/RegisterScreen.kt`
- `presentation/auth/ForgotPasswordScreen.kt`
- `presentation/dashboard/UserDashboardScreen.kt`
- `presentation/dashboard/AdminDashboardScreen.kt`
- `presentation/reports/CreateReportScreen.kt`
- `presentation/reports/EditReportScreen.kt`
- `presentation/reports/ReportDetailScreen.kt`
- `presentation/reports/ReportListScreen.kt`
- `presentation/admin/AdminReportListScreen.kt`
- `presentation/admin/AdminReportDetailScreen.kt`
- `presentation/admin/CategoryManagementScreen.kt`
- `presentation/admin/ActivityLogScreen.kt`
- `presentation/profile/ProfileScreen.kt`

## 7. Navigation Compose

| Item | Lokasi | Pemakaian |
|---|---|---|
| Dependency | `app/build.gradle.kts:66`, `gradle/libs.versions.toml:30` | Navigation antar screen Compose. |
| Route constants | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` | Menyimpan nama route seperti login, dashboard, detail laporan. |
| Navigation graph | `app/src/main/java/com/adit/sirs/presentation/navigation/AppNavGraph.kt` | `NavHost`, `composable`, pengambilan ViewModel via `hiltViewModel`. |
| Start destination | `AppNavGraph.kt` | Ditentukan dari `currentUser` dan role user/admin. |

Route utama yang didokumentasikan di project:

| Route | Fungsi |
|---|---|
| `login` | Login user/admin. |
| `register` | Registrasi user. |
| `forgot_password` | Reset password via email. |
| `user_dashboard` | Dashboard pengguna. |
| `admin_dashboard` | Dashboard admin. |
| `report_list` | Daftar laporan user. |
| `report_detail/{reportId}` | Detail laporan user. |
| `create_report` | Form pembuatan laporan. |
| `edit_report/{reportId}` | Edit laporan pending. |
| `admin_report_list` | Daftar laporan admin. |
| `admin_report_detail/{reportId}` | Detail laporan admin. |
| `admin_create_report` | Admin tambah laporan. |
| `category_management` | Manajemen kategori. |
| `activity_log` | Log aktivitas. |
| `profile` | Profil user. |

## 8. MVVM, StateFlow, dan Coroutines

| Teknologi | Lokasi dependency | Lokasi pemakaian | Fungsi |
|---|---|---|---|
| Lifecycle runtime KTX | `app/build.gradle.kts:62` | ViewModel/screen lifecycle | Integrasi lifecycle Android. |
| Lifecycle runtime compose | `app/build.gradle.kts:63` | Compose state lifecycle | Observasi state secara lifecycle-aware. |
| Lifecycle ViewModel Compose | `app/build.gradle.kts:64` | Screen Compose | Menghubungkan ViewModel ke UI. |
| Kotlin Coroutines Core/Android | `app/build.gradle.kts:99-102` | ViewModel dan repository | Operasi asynchronous Firebase/Cloudinary. |
| Coroutines Play Services | `app/build.gradle.kts:102` | Data source Firebase | `.await()` untuk Task Firebase. |
| StateFlow | ViewModel files | `AuthViewModel`, `ReportViewModel`, `AdminViewModel`, `DashboardViewModel`, `ProfileViewModel` | State UI realtime. |

ViewModel utama:

| ViewModel | Lokasi | Fungsi |
|---|---|---|
| `AuthViewModel` | `presentation/auth/AuthViewModel.kt` | Login, registrasi, reset password, current user state. |
| `ReportViewModel` | `presentation/reports/ReportViewModel.kt` | Form laporan, upload bukti, daftar/detail/edit/hapus laporan user. |
| `AdminViewModel` | `presentation/admin/AdminViewModel.kt` | Laporan admin, update status/severity/respon, kategori, activity log. |
| `DashboardViewModel` | `presentation/dashboard/DashboardViewModel.kt` | Statistik dashboard user/admin. |
| `ProfileViewModel` | `presentation/profile/ProfileViewModel.kt` | Data profil user. |

## 9. Dependency Injection: Hilt

| Item | Lokasi | Pemakaian |
|---|---|---|
| Plugin Hilt | `app/build.gradle.kts:5` | Mengaktifkan Hilt di module app. |
| Dependency Hilt | `app/build.gradle.kts:76-79` | Runtime dan compiler Hilt. |
| Application annotation | `SirsApplication.kt:17` | `@HiltAndroidApp` sebagai root DI. |
| Activity annotation | `MainActivity.kt:21` | `@AndroidEntryPoint` agar Activity bisa memakai dependency graph. |
| ViewModel annotation | `presentation/**/*ViewModel.kt` | `@HiltViewModel` untuk constructor injection. |
| Firebase provider module | `core/firebase/FirebaseProvider.kt` | Menyediakan FirebaseAuth, FirebaseFirestore, FirebaseMessaging, FirebaseAnalytics. |
| Repository binding module | `data/di/RepositoryModule.kt` | Mengikat interface domain ke implementation data. |
| Hilt navigation compose | `AppNavGraph.kt` | `hiltViewModel()` mengambil ViewModel di route Compose. |

Repository binding:

| Interface | Implementation | Lokasi binding |
|---|---|---|
| `AuthRepository` | `AuthRepositoryImpl` | `RepositoryModule.kt:26-28` |
| `ReportRepository` | `ReportRepositoryImpl` | `RepositoryModule.kt:30-32` |
| `CategoryRepository` | `CategoryRepositoryImpl` | `RepositoryModule.kt:34-36` |
| `AdminRepository` | `AdminRepositoryImpl` | `RepositoryModule.kt:38-40` |

## 10. Firebase Stack

Project Firebase yang dipakai berdasarkan `google-services.json`:

| Item | Nilai | Lokasi |
|---|---|---|
| Firebase project id | `androapp-f262d` | `app/google-services.json:5` |
| Project number | `123472963435` | `app/google-services.json:3` |
| Storage bucket | `androapp-f262d.firebasestorage.app` | `app/google-services.json:6` |
| Package client utama | `com.adit.sirs` | `app/google-services.json:13` |
| Package tambahan terdaftar | `com.adit.todoapp` | `app/google-services.json:32` |

### 10.1 Firebase Authentication

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:83` | Firebase Auth library. |
| Provider: `FirebaseProvider.kt:26` | Menyediakan `FirebaseAuth.getInstance()`. |
| Data source: `data/remote/FirebaseAuthDataSource.kt` | Sign in, sign up, sign out, reset password, cek current user. |
| Repository: `data/repository/AuthRepositoryImpl.kt` | Menggabungkan Auth, user profile Firestore, FCM token, activity log. |
| UI/ViewModel: `presentation/auth/AuthViewModel.kt` | Login/register/reset password dari screen. |

### 10.2 Cloud Firestore

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:84` | Firestore database. |
| Provider: `FirebaseProvider.kt:31` | Menyediakan `FirebaseFirestore.getInstance()`. |
| Offline persistence: `SirsApplication.kt:33-37` | Cache/offline persistence Firestore. |
| Rules config: `firebase.json:2-4` | Menunjuk ke `firestore.rules`. |
| Rules file: `firestore.rules` | Hak akses database. |
| Data source user: `FirestoreUserDataSource.kt` | Collection `users`. |
| Data source laporan: `FirestoreReportDataSource.kt` | Collection `incidentReports` dan subcollection `statusHistories`. |
| Data source kategori: `FirestoreCategoryDataSource.kt` | Collection `categories`. |
| Data source log: `FirestoreActivityLogDataSource.kt` | Collection `activityLogs`. |
| Data source token: `FcmTokenDataSource.kt` | Collection `deviceTokens`. |

Collection yang dipakai:

| Collection | Konstanta | Lokasi | Fungsi |
|---|---|---|---|
| `users` | `COLLECTION_USERS` | `AppConstants.kt:30` | Profil user/admin. |
| `categories` | `COLLECTION_CATEGORIES` | `AppConstants.kt:31` | Kategori laporan. |
| `incidentReports` | `COLLECTION_REPORTS` | `AppConstants.kt:32` | Data laporan insiden. |
| `activityLogs` | `COLLECTION_ACTIVITY_LOGS` | `AppConstants.kt:33` | Audit log aktivitas penting. |
| `deviceTokens` | `COLLECTION_DEVICE_TOKENS` | `AppConstants.kt:34` | Token perangkat untuk FCM. |
| `statusHistories` | `SUBCOLLECTION_STATUS_HISTORIES` | `AppConstants.kt:35` | Riwayat perubahan status laporan. |

### 10.3 Firebase Cloud Messaging

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:85` | Firebase Messaging. |
| Provider: `FirebaseProvider.kt:36` | Menyediakan `FirebaseMessaging.getInstance()`. |
| Manifest service: `AndroidManifest.xml:30-36` | Service penerima pesan/token FCM. |
| Service class: `SirsFirebaseMessagingService.kt` | Callback `onNewToken` dan `onMessageReceived`. |
| Token data source: `FcmTokenDataSource.kt` | Menyimpan/menghapus token perangkat di Firestore. |
| Permission notification: `MainActivity.kt:41-49` | Request `POST_NOTIFICATIONS` untuk Android 13+. |

Catatan: `SirsFirebaseMessagingService.kt` saat ini hanya menyiapkan callback dasar. Penanganan notifikasi kustom bisa dikembangkan dari file ini.

### 10.4 Firebase Analytics

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:86` | Firebase Analytics. |
| Provider: `FirebaseProvider.kt:41` | Menyediakan `FirebaseAnalytics`. |
| Helper: `core/firebase/AnalyticsHelper.kt` | Logging event login, report created, status viewed, error. |
| Pemanggil | `AuthViewModel`, `ReportViewModel`, dan bagian lain yang membutuhkan event analytics. |

### 10.5 Firebase Crashlytics

| Lokasi | Pemakaian |
|---|---|
| Plugin: `app/build.gradle.kts:7` | Integrasi Crashlytics plugin. |
| Dependency: `app/build.gradle.kts:87` | Runtime Crashlytics. |
| Versi plugin: `gradle/libs.versions.toml:21` | `3.0.3`. |

Catatan: Dependency dan plugin Crashlytics sudah ada. Jika ingin mencatat error manual, bisa ditambahkan pemanggilan Crashlytics API di error handling.

### 10.6 Firebase App Check

| Lokasi | Pemakaian |
|---|---|
| Dependency Play Integrity | `app/build.gradle.kts:88` | App Check production/release. |
| Dependency Debug App Check | `app/build.gradle.kts:89` | App Check debug build. |
| Inisialisasi | `SirsApplication.kt:25-31` | Debug memakai `DebugAppCheckProviderFactory`; release memakai `PlayIntegrityAppCheckProviderFactory`. |

## 11. Firestore Security Rules

| Item | Lokasi | Fungsi |
|---|---|---|
| Firebase deploy config | `firebase.json` | Menunjukkan file rules: `firestore.rules`. |
| Rules utama | `firestore.rules` | Hak akses users, categories, incidentReports, statusHistories, activityLogs, deviceTokens. |
| Helper signed in | `firestore.rules:5-7` | Cek user login. |
| Helper admin | `firestore.rules:17-22` | Cek role `administrator` dan user aktif. |
| Validasi create report | `firestore.rules:38-52` | Cek owner, status pending, field wajib, batas panjang, severity. |
| Delete report | `firestore.rules:66-91` | Admin boleh delete; user owner boleh delete jika laporan pending. |
| Fallback deny all | `firestore.rules:120-122` | Menolak akses collection lain yang tidak didefinisikan. |

Rules sudah dideploy ke project Firebase `androapp-f262d` menggunakan Firebase CLI via `npx firebase-tools`.

## 12. Cloudinary dan Upload File

| Item | Lokasi | Nilai/Pemakaian |
|---|---|---|
| Nama *Cloud* | `app/build.gradle.kts:23` | `da26s6yet`. |
| Folder unggahan | `app/build.gradle.kts:25` | `sirs/incident-reports`. |
| Delete endpoint | `app/build.gradle.kts:26-31`, `gradle.properties` | Dibaca dari Gradle property/environment variable `CLOUDINARY_DELETE_ENDPOINT`. |
| Delete secret | `app/build.gradle.kts:32-37`, `gradle.properties` | Dibaca dari Gradle property/environment variable `CLOUDINARY_DELETE_SECRET`. Nilai secret tidak ditulis di dokumentasi ini. |
| Data source | `data/remote/CloudinaryUploadDataSource.kt` | Upload dan delete file Cloudinary. |
| HTTP client | `OkHttpClient` | Dipakai untuk POST multipart upload dan POST delete endpoint. |
| Validasi sebelum upload | `core/security/FileValidation.kt` | MIME, extension, size, magic bytes. |

Flow upload bukti:

1. User memilih file di screen laporan.
2. `ReportViewModel.setAttachment` memanggil `FileValidation.validateFile`.
3. Validasi mengecek MIME, ukuran maksimal 2 MB, ekstensi, dan magic bytes.
4. `ReportRepositoryImpl.uploadAttachment` memanggil `CloudinaryUploadDataSource.uploadFile`.
5. File dikirim ke endpoint Cloudinary `https://api.cloudinary.com/v1_1/{cloudName}/{resourceType}/upload`.
6. Metadata attachment disimpan bersama laporan di Firestore.

Flow delete bukti:

1. `ReportRepositoryImpl.deleteReport` mengambil attachment laporan.
2. Jika `publicId` ada, repository memanggil `CloudinaryUploadDataSource.deleteFile`.
3. Delete file dikirim ke endpoint backend/Cloudflare Worker melalui `CLOUDINARY_DELETE_ENDPOINT` dengan secret dari BuildConfig.
4. Setelah file dihapus, dokumen laporan di Firestore dihapus sesuai rules.

## 13. OkHttp

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:95-97` | HTTP client dan logging interceptor. |
| Versi: `gradle/libs.versions.toml:16` | `4.12.0`. |
| Data source: `CloudinaryUploadDataSource.kt:29-33` | Membuat `OkHttpClient` dengan timeout connect/write/read. |
| Upload: `CloudinaryUploadDataSource.kt:55-67` | Multipart upload file ke Cloudinary. |
| Delete: `CloudinaryUploadDataSource.kt:105-117` | POST JSON ke endpoint delete Cloudinary. |

## 14. Coil

| Lokasi | Pemakaian |
|---|---|
| Dependency: `app/build.gradle.kts:91-93` | Coil Compose dan network OkHttp. |
| Versi: `gradle/libs.versions.toml:15` | `3.2.0`. |
| Fungsi | Library image loading untuk Compose. |

Catatan source saat ini: dependency Coil tersedia, tetapi dari pencarian source tidak ditemukan pemakaian langsung `AsyncImage`. Jika nanti preview gambar attachment ditambahkan, Coil bisa dipakai di komponen detail laporan.

## 15. Security dan Validasi Lokal

| Komponen | Lokasi | Fungsi |
|---|---|---|
| File validation | `core/security/FileValidation.kt` | Validasi MIME, ukuran, ekstensi, magic bytes JPG/PNG/PDF. |
| MIME validator | `core/security/MimeTypeValidator.kt` | Whitelist MIME dan mapping ekstensi. |
| SLA calculator | `core/security/SlaCalculator.kt` | Hitung deadline penanganan berdasarkan severity. |
| Rate limiter | `core/common/RateLimiter.kt` | Membatasi aksi seperti create report/upload agar tidak spam. |
| Error mapper | `core/error/ErrorMapper.kt` | Mengubah Firebase/Auth/Firestore exception menjadi pesan user-friendly. |
| Firestore rules | `firestore.rules` | Security utama di sisi backend Firebase. |
| App Check | `SirsApplication.kt:25-31` | Mengurangi penyalahgunaan akses Firebase dari aplikasi tidak sah. |
| Password validation | `AuthViewModel.kt` | Validasi password minimal 12 karakter, huruf besar/kecil, angka, karakter khusus. |

Konstanta validasi utama:

| Konstanta | Nilai | Lokasi |
|---|---|---|
| Max attachment size | `2 MB` | `AppConstants.kt:10` |
| Min title | `5` | `AppConstants.kt:11` |
| Max title | `150` | `AppConstants.kt:12` |
| Min description | `20` | `AppConstants.kt:13` |
| Max description | `4000` | `AppConstants.kt:14` |
| Min location | `3` | `AppConstants.kt:15` |
| Max location | `255` | `AppConstants.kt:16` |
| Report page size | `20` | `AppConstants.kt:17` |
| Activity log page size | `50` | `AppConstants.kt:18` |
| Allowed MIME | JPG, PNG, PDF | `AppConstants.kt:20-25` |
| Allowed extension | `jpg`, `jpeg`, `png`, `pdf` | `AppConstants.kt:27` |

## 16. Environment dan Konfigurasi Sensitif

| Konfigurasi | Lokasi | Cara dibaca | Keterangan |
|---|---|---|---|
| Firebase project config | `app/google-services.json` | Google Services plugin | Berisi project id, app id, API key Firebase client. |
| Firestore rules config | `firebase.json` | Firebase CLI | Menentukan `firestore.rules`. |
| Cloudinary cloud name | `app/build.gradle.kts:23` | `BuildConfig.CLOUDINARY_CLOUD_NAME` | Nama cloud dari dashboard Cloudinary. |
| Cloudinary folder | `app/build.gradle.kts:25` | `BuildConfig.CLOUDINARY_UPLOAD_FOLDER` | Folder target untuk upload bukti laporan. |
| Cloudinary delete endpoint | Gradle property/env var | `BuildConfig.CLOUDINARY_DELETE_ENDPOINT` | Endpoint backend/Worker untuk delete asset. |
| Cloudinary delete auth | Firebase ID Token | `CloudinaryUploadDataSource.deleteFile` | JWT Token dari user login (otomatis dikirim di header `Authorization`). |
| JVM memory build | `gradle.properties:9` | Gradle daemon | `-Xmx2048m`. |
| Kotlin style | `gradle.properties:15` | Gradle/Kotlin | `official`. |

Prioritas keamanan konfigurasi:

- Secret Cloudinary tidak lagi disimpan di Android (`CLOUDINARY_DELETE_SECRET` telah dihapus).
- Aplikasi Android memverifikasi dirinya ke backend menggunakan Firebase ID Token (Bearer token).
- Backend (Cloudflare Worker/Firebase Function) bertugas memverifikasi JWT token tersebut lalu memakai secret Cloudinary yang ada di server-side untuk menghapus gambar.
- `google-services.json` pada Android memang berisi client config, tetapi tetap jangan diposting sembarangan jika tidak perlu.

## 17. Testing Stack

| Teknologi | Lokasi dependency | Fungsi |
|---|---|---|
| JUnit 4 | `app/build.gradle.kts:108`, `gradle/libs.versions.toml:75` | Unit test JVM. |
| Coroutines Test | `app/build.gradle.kts:109`, `gradle/libs.versions.toml:78` | Test coroutine/ViewModel. |
| AndroidX JUnit | `app/build.gradle.kts:110`, `gradle/libs.versions.toml:76` | Instrumented test Android. |
| Espresso | `app/build.gradle.kts:111`, `gradle/libs.versions.toml:77` | UI test klasik Android. |
| Compose UI Test JUnit4 | `app/build.gradle.kts:112-113` | UI test Compose. |
| Hilt Android Testing | `app/build.gradle.kts:114-115` | Test dengan Hilt injection. |
| Test runner | `app/build.gradle.kts:21` | `com.adit.sirs.HiltTestRunner`. |

Catatan: dependency testing sudah tersedia. Untuk membuktikan behavior UI/database secara otomatis, perlu test case spesifik di folder `app/src/test` atau `app/src/androidTest`.

## 18. Resource, Theme, dan UI Configuration

| Item | Lokasi | Fungsi |
|---|---|---|
| Theme manifest | `AndroidManifest.xml:17`, `AndroidManifest.xml:23` | Theme Android untuk application/activity. |
| Compose theme | `presentation/theme/Theme.kt` | Theme Compose `SIRSTheme`. |
| App name | `app/src/main/res/values/strings.xml` | Label aplikasi. |
| Backup rules | `app/src/main/res/xml/backup_rules.xml` | Konfigurasi backup Android. |
| Data extraction rules | `app/src/main/res/xml/data_extraction_rules.xml` | Konfigurasi data extraction Android. |
| Launcher icon | `app/src/main/res/mipmap-*` | Icon aplikasi. |

## 19. Mapping Teknologi ke Fitur

| Fitur | Teknologi utama | File utama |
|---|---|---|
| Login/register/reset password | Firebase Auth, Firestore, Hilt, StateFlow | `AuthViewModel.kt`, `AuthRepositoryImpl.kt`, `FirebaseAuthDataSource.kt`, `FirestoreUserDataSource.kt` |
| Role-based navigation | Navigation Compose, Firebase user profile | `AppNavGraph.kt`, `Routes.kt`, `AuthViewModel.kt` |
| Dashboard user/admin | Compose, ViewModel, Firestore realtime flow | `DashboardViewModel.kt`, `UserDashboardScreen.kt`, `AdminDashboardScreen.kt` |
| Create laporan | Compose form, StateFlow, RateLimiter, Firestore | `CreateReportScreen.kt`, `ReportViewModel.kt`, `ReportRepositoryImpl.kt`, `FirestoreReportDataSource.kt` |
| Upload bukti | FileValidation, OkHttp, Cloudinary | `FileValidation.kt`, `CloudinaryUploadDataSource.kt`, `ReportRepositoryImpl.kt` |
| Detail laporan | Compose, Firestore observe document, status histories | `ReportDetailScreen.kt`, `AdminReportDetailScreen.kt`, `FirestoreReportDataSource.kt` |
| Update status/severity/admin response | Admin ViewModel, Firestore batch, activity log | `AdminViewModel.kt`, `AdminRepositoryImpl.kt`, `FirestoreReportDataSource.kt` |
| Manajemen kategori | Compose admin screen, Firestore categories | `CategoryManagementScreen.kt`, `AdminViewModel.kt`, `FirestoreCategoryDataSource.kt` |
| Activity log | Firestore activity logs, Compose list | `FirestoreActivityLogDataSource.kt`, `ActivityLogScreen.kt`, `AdminViewModel.kt` |
| Push notification/token | Firebase Messaging, Firestore deviceTokens | `SirsFirebaseMessagingService.kt`, `FcmTokenDataSource.kt` |
| Analytics | Firebase Analytics | `AnalyticsHelper.kt` |
| Crash reporting | Firebase Crashlytics | `app/build.gradle.kts`, Firebase plugin/dependency |
| Access control backend | Firestore security rules | `firestore.rules` |

## 20. File Konfigurasi Penting yang Harus Dijelaskan Saat Presentasi

| File | Peran |
|---|---|
| `settings.gradle.kts` | Nama project, module `:app`, repository plugin/dependency. |
| `build.gradle.kts` | Plugin global project. |
| `app/build.gradle.kts` | Namespace, SDK, BuildConfig Cloudinary, Compose, dependency Firebase/Hilt/OkHttp/test. |
| `gradle/libs.versions.toml` | Sumber versi dependency dan plugin. |
| `gradle.properties` | Setting Gradle dan property Cloudinary delete. Secret jangan dibuka ke publik. |
| `app/google-services.json` | Konfigurasi Firebase Android project. |
| `firebase.json` | Konfigurasi deploy Firebase rules. |
| `firestore.rules` | Aturan akses database Firestore. |
| `AndroidManifest.xml` | Permission, Application, MainActivity, FCM service. |
| `SirsApplication.kt` | Inisialisasi Firebase, App Check, Firestore offline persistence. |
| `MainActivity.kt` | Entry point Compose dan permission notifikasi. |
| `RepositoryModule.kt` | Binding repository interface ke implementation. |
| `FirebaseProvider.kt` | Provider Firebase untuk dependency injection. |

## 21. Cara Menjalankan dan Mengelola Environment

### Build debug lokal

```bash
./gradlew assembleDebug
```

### Deploy Firestore rules

```bash
npx firebase-tools deploy --only firestore:rules --project androapp-f262d --non-interactive
```

### Konfigurasi Cloudinary delete

Aplikasi membaca konfigurasi endpoint dari Gradle property atau environment variable:

```text
CLOUDINARY_DELETE_ENDPOINT
```

Lokasi pembacaan:

- `app/build.gradle.kts:26-31`
- Dipakai sebagai `BuildConfig.CLOUDINARY_DELETE_ENDPOINT`
- Digunakan di `CloudinaryUploadDataSource.deleteFile` bersama dengan Firebase ID Token.

### Firebase config

File yang wajib ada untuk koneksi Firebase Android:

```text
app/google-services.json
```

Project id yang terbaca:

```text
androapp-f262d
```

## 22. Ringkasan Jawaban Presentasi

### Kenapa memakai Firebase?

Karena aplikasi membutuhkan autentikasi, database realtime, activity log, token device, analytics, crash reporting, dan security rules. Firebase menyediakan komponen tersebut tanpa backend server custom penuh.

### Kenapa masih memakai Cloudinary?

Cloudinary dipakai khusus untuk penyimpanan bukti laporan berupa JPG/PNG/PDF. Aplikasi melakukan validasi file terlebih dahulu, lalu mengupload ke Cloudinary dan menyimpan metadata file di Firestore.

### Kenapa memakai Hilt?

Hilt membuat dependency seperti repository, data source, dan Firebase provider bisa diinjeksi ke ViewModel tanpa membuat object manual di setiap screen. Ini membuat struktur MVVM lebih rapi.

### Kenapa memakai Compose?

Compose dipakai karena UI dapat ditulis deklaratif dengan Kotlin, state dari ViewModel bisa langsung mengubah tampilan, dan Material 3 mendukung desain modern Android.

### Dimana environment dikonfigurasi?

- Firebase: `app/google-services.json`, `firebase.json`, `firestore.rules`.
- Android build: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`.
- Cloudinary: `app/build.gradle.kts` dan Gradle property/environment variable `CLOUDINARY_DELETE_ENDPOINT`, `CLOUDINARY_DELETE_SECRET`.

### Apakah rules database sudah dideploy?

Ya, `firestore.rules` sudah dideploy ke Firebase project `androapp-f262d` menggunakan Firebase CLI via `npx firebase-tools`.

## 23. Catatan Validasi

- Dokumentasi ini dibuat dari inspeksi source code project saat ini.
- Build debug terakhir berhasil menggunakan `./gradlew assembleDebug` setelah perubahan rules/dokumentasi sebelumnya.
- Secret Cloudinary tidak ditulis eksplisit di dokumen ini untuk keamanan.
- Dependency Coil tersedia, tetapi belum ditemukan pemakaian langsung `AsyncImage` di source code saat inspeksi.
- Crashlytics dependency/plugin tersedia; dokumentasi ini tidak mengklaim adanya custom manual crash logging selain integrasi library/plugin.
