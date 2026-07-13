# Dokumentasi Lengkap Fitur dan Alur Kode SIRS

Dokumen ini dibuat berdasarkan inspeksi langsung pada project Android SIRS saat ini. Fokus dokumen: menjelaskan fitur, flow antar-layer, file kode terkait, dan nomor line yang bisa ditunjukkan saat presentasi.

## 1. Ringkasan Project

- Nama aplikasi: SIRS - Sistem Informasi Respons Insiden Keamanan Siber.
- Platform: Android native.
- Bahasa/framework: Kotlin, Jetpack Compose, Material 3, MVVM/Clean Architecture.
- Backend/service: Firebase Authentication, Cloud Firestore, Firebase Messaging, Analytics, Crashlytics, App Check.
- Upload bukti: Cloudinary signed upload dengan validasi lokal, signature generation, dan delete yang dilindungi Cloudflare Worker JWT.
- Package utama: `com.adit.sirs`.
- Manifest launcher: `app/src/main/AndroidManifest.xml:19`.
- Dependency utama: `app/build.gradle.kts:59`.

## 2. Arsitektur Layer

| Layer | Folder | Fungsi |
|---|---|---|
| Core | `app/src/main/java/com/adit/sirs/core/` | Utilitas umum, rate limiter, error mapper, validasi file, SLA, formatter, helper Firebase. |
| Data | `app/src/main/java/com/adit/sirs/data/` | DTO, mapper, remote data source Firebase/Cloudinary, implementasi repository. |
| Domain | `app/src/main/java/com/adit/sirs/domain/` | Model bisnis dan interface repository agar UI tidak bergantung langsung ke Firebase. |
| Presentation | `app/src/main/java/com/adit/sirs/presentation/` | Screen Jetpack Compose, ViewModel, komponen UI, navigasi, theme. |

Alur umum: Screen Compose -> ViewModel -> Repository Interface -> Repository Impl -> Data Source -> Firebase/Cloudinary -> Mapper -> Domain Model -> StateFlow -> Screen Compose.

## 3. Dokumentasi Fitur dan Mapping File/Line

Sumber daftar fitur: `docs/form-project-akhir.md:12-38`. Bagian ini sengaja dibuat 25 item agar sama persis dengan form project akhir.

| No | Fitur sesuai form-project-akhir | Flow implementasi saat ini | File dan line utama |
|---:|---|---|---|
| 1 | Login dan registrasi akun | Login: Firebase Auth -> profil Firestore -> role dashboard. Registrasi: membuat akun Auth, profil user, activity log, token FCM. | `app/src/main/java/com/adit/sirs/presentation/auth/AuthViewModel.kt:67`<br>`app/src/main/java/com/adit/sirs/presentation/auth/AuthViewModel.kt:85`<br>`app/src/main/java/com/adit/sirs/presentation/navigation/AppNavGraph.kt:47` |
| 2 | Reset password melalui email | Email reset dikirim via Firebase Authentication dan state UI menampilkan loading/sukses/error. | `app/src/main/java/com/adit/sirs/presentation/auth/AuthViewModel.kt:121`<br>`app/src/main/java/com/adit/sirs/data/remote/FirebaseAuthDataSource.kt:42` |
| 3 | Dashboard pengguna | Dashboard user membaca laporan user login dan menghitung ringkasan status/severity untuk kartu statistik. | `app/src/main/java/com/adit/sirs/presentation/dashboard/DashboardViewModel.kt:86`<br>`app/src/main/java/com/adit/sirs/presentation/dashboard/UserDashboardScreen.kt:34` |
| 4 | Dashboard admin | Dashboard admin membaca seluruh laporan dan menampilkan statistik global serta akses ke laporan/kategori/log. | `app/src/main/java/com/adit/sirs/presentation/dashboard/DashboardViewModel.kt:101`<br>`app/src/main/java/com/adit/sirs/presentation/dashboard/AdminDashboardScreen.kt:35` |
| 5 | Pembuatan laporan insiden keamanan siber | Submit laporan memvalidasi form, rate limit, upload opsional, hitung SLA, dan simpan incidentReports status pending. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:220`<br>`app/src/main/java/com/adit/sirs/data/repository/ReportRepositoryImpl.kt:54`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:48` |
| 6 | Pengisian data laporan: judul, kategori, tingkat keparahan, deskripsi, lokasi, tanggal kejadian | State form menyimpan field laporan dan validasi memastikan field wajib terisi sebelum submit. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:185`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportFormState.kt:9`<br>`app/src/main/java/com/adit/sirs/presentation/reports/CreateReportScreen.kt:33` |
| 7 | Upload bukti pendukung laporan | Attachment divalidasi MIME/ekstensi/ukuran/magic bytes, minta signature (JWT) ke worker, lalu diupload ke Cloudinary. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:190`<br>`app/src/main/java/com/adit/sirs/core/security/FileValidation.kt:28`<br>`app/src/main/java/com/adit/sirs/data/remote/CloudinaryUploadDataSource.kt:34` |
| 8 | Daftar laporan milik pengguna | User membaca laporan miliknya secara realtime berdasarkan UID current user. Firestore Rule mengizinkan `list` asalkan request sudah terfilter dan user login. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:127`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:79` |
| 9 | Detail laporan pengguna | Detail user memuat satu laporan, status histories, attachment, response admin, dan opsi edit jika pending. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:164`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportDetailScreen.kt:31` |
| 10 | Filter laporan berdasarkan status | Status filter mengubah query observeUserReports agar daftar hanya menampilkan status tertentu. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:141`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:127` |
| 11 | Filter laporan berdasarkan tingkat keparahan | Severity filter diterapkan di ViewModel pada data yang sudah dimuat. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:147`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:153` |
| 12 | Edit laporan oleh pengguna | Form edit diprefill dari detail laporan, lalu updateReport mengirim perubahan ke repository. Rules membatasi edit owner hanya saat pending. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:448`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:341`<br>`app/src/main/java/com/adit/sirs/presentation/reports/EditReportScreen.kt:33` |
| 13 | Hapus laporan oleh pengguna | User memanggil deleteReport dari ViewModel/repository. Penghapusan foto memanggil Worker dengan verifikasi Token Firebase. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:414`<br>`app/src/main/java/com/adit/sirs/data/repository/ReportRepositoryImpl.kt:78`<br>`firestore.rules:91` |
| 14 | Daftar seluruh laporan untuk admin | Admin repository observeAllReports membaca semua incidentReports untuk dashboard/list admin. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:119`<br>`app/src/main/java/com/adit/sirs/data/repository/AdminRepositoryImpl.kt:30`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:108` |
| 15 | Pencarian laporan oleh admin | Search query admin memfilter title, reportCode, userName, dan location. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:131`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:163` |
| 16 | Filter laporan admin berdasarkan status, tingkat keparahan, dan tanggal | Admin memakai status query ke Firestore plus severity/date filtering di ViewModel. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:157`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:137`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:143` |
| 17 | Detail laporan admin | Admin detail memuat laporan dan status histories dengan fitur update/delete. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:200`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminReportDetailScreen.kt:38` |
| 18 | Perubahan status laporan oleh admin | Admin update status menulis status baru, handledBy/handledAt, status history, dan activity log. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:221`<br>`app/src/main/java/com/adit/sirs/data/repository/AdminRepositoryImpl.kt:37`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:151` |
| 19 | Perubahan tingkat keparahan laporan oleh admin | Severity baru dikirim bersama update status dan dipakai untuk perubahan data laporan serta histori. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:224`<br>`app/src/main/java/com/adit/sirs/data/repository/AdminRepositoryImpl.kt:40`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:154` |
| 20 | Respon atau catatan penanganan dari admin | adminResponse disimpan saat update laporan; jika hanya response berubah, activity log response_updated dicatat. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:225`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:250`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:171` |
| 21 | Hapus laporan oleh admin | Admin detail/list bisa memanggil deleteReport, Firestore rules mengizinkan admin menghapus laporan, dan activity log mencatat `report.deleted`. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:398`<br>`app/src/main/java/com/adit/sirs/data/repository/ReportRepositoryImpl.kt:78`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminReportDetailScreen.kt:42` |
| 22 | Riwayat perubahan status laporan | Subcollection statusHistories dibaca di detail dan ditulis saat admin update status/severity/response. | `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:178`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:214`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt:196` |
| 23 | Manajemen kategori laporan | Admin CRUD/toggle kategori, user membaca kategori aktif untuk pilihan form dan guidance. | `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:275`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:307`<br>`app/src/main/java/com/adit/sirs/data/remote/FirestoreCategoryDataSource.kt:82` |
| 24 | Pencatatan aktivitas penting pada aplikasi | Activity log dibuat untuk register, laporan, kategori, status, response, delete dan dibaca di layar admin. | `app/src/main/java/com/adit/sirs/data/remote/FirestoreActivityLogDataSource.kt:27`<br>`app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt:426`<br>`app/src/main/java/com/adit/sirs/presentation/admin/ActivityLogScreen.kt:24` |
| 25 | Perhitungan batas waktu penanganan berdasarkan tingkat keparahan laporan | SLA deadline dihitung berdasarkan severity saat laporan dibuat, lalu dapat dipakai UI untuk overdue/near deadline. | `app/src/main/java/com/adit/sirs/core/security/SlaCalculator.kt:13`<br>`app/src/main/java/com/adit/sirs/core/security/SlaCalculator.kt:21`<br>`app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt:276` |

## 4. Flow Detail yang Harus Dijelaskan

### 4.1 Flow Autentikasi
1. `AppNavGraph` membaca `currentUser` dari `AuthViewModel`.
2. Jika user kosong, route awal adalah login.
3. Login/register memanggil `AuthViewModel`, kemudian `AuthRepositoryImpl`.
4. `FirebaseAuthDataSource` mengurus akun Firebase Auth, `FirestoreUserDataSource` mengambil/menyimpan profil user.
5. Role `administrator` masuk ke admin dashboard, role `user` masuk ke user dashboard.

### 4.2 Flow Pembuatan Laporan
1. User membuka form laporan dari dashboard/list.
2. `CreateReportScreen` mengisi `ReportFormState` melalui `ReportViewModel.updateFormField`.
3. Jika user memilih attachment, `ReportViewModel.setAttachment` memanggil `FileValidation.validateFile`.
4. Saat submit, `ReportViewModel.submitReport` mengecek form, user login, rate limit, upload attachment, SLA, dan payload laporan.
5. `ReportRepositoryImpl.createReport` membuat `reportCode`, UI ViewModel menyimpan statenya mencegah upload gambar Cloudinary ganda jika Firestore menolak data teks.
6. Data laporan disimpan ke collection `incidentReports`; status awal selalu `pending`.
7. Activity log dibuat agar admin bisa menelusuri kejadian.

### 4.3 Flow Penanganan Admin
1. Admin dashboard/list memanggil `AdminViewModel.loadAllReports`.
2. Data semua laporan diambil realtime dari repository admin.
3. Admin memilih detail, lalu `AdminViewModel.loadReportDetail` memuat laporan dan status histories.
4. Update status/severity/respon dilakukan lewat `AdminViewModel.updateReportStatus`.
5. Repository menulis perubahan laporan, menambah dokumen status history, dan mencatat activity log.

### 4.4 Flow Keamanan Upload
1. Android ContentResolver membaca MIME dari file pilihan user.
2. `MimeTypeValidator` memastikan MIME hanya JPG/PNG/PDF.
3. Metadata file dibaca untuk ukuran dan nama file.
4. Ekstensi dibandingkan dengan MIME.
5. Byte awal file dibaca dan dibandingkan dengan magic bytes JPG/PNG/PDF.
6. Aplikasi Android memanggil `/generate-signature` pada Cloudflare Worker dengan Firebase Token.
7. Jika lolos, Worker mengirimkan Tanda Tangan (Signature) anti-spam, dan Android membawanya untuk melakukan upload.
8. Penghapusan file juga diproses melalui serverless backend (Cloudflare Worker) yang memverifikasi JWT sebelum mengakses API Secret.

## 5. Mapping Route Navigasi

| Route | Tujuan layar | File |
|---|---|---|
| `login` | Login | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `register` | Registrasi | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `forgot_password` | Reset password | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `user_dashboard` | Dashboard user | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `admin_dashboard` | Dashboard admin | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `report_list` | Daftar laporan user | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `report_detail/{reportId}` | Detail laporan user | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `create_report` | Buat laporan | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `edit_report/{reportId}` | Edit laporan | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `admin_report_list` | Daftar laporan admin | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `admin_report_detail/{reportId}` | Detail laporan admin | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `admin_create_report` | Admin tambah laporan | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `category_management` | Manajemen kategori | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `activity_log` | Log aktivitas | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |
| `profile` | Profil | `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` dan `AppNavGraph.kt` |

## 6. Mapping Data Firestore

| Collection | Isi data | Akses rules | Kode utama |
|---|---|---|---|
| `users` | Profil uid, nama, email, role, status aktif, last login. | User hanya profil sendiri; admin bisa baca/ubah. | `FirestoreUserDataSource.kt`, `UserDto.kt`, `User.kt` |
| `categories` | Kategori insiden, slug, deskripsi, tips mitigasi, bukti rekomendasi, aktif/tidak. | User aktif baca; admin CRUD. | `FirestoreCategoryDataSource.kt`, `CategoryDto.kt`, `Category.kt` |
| `incidentReports` | Laporan insiden, report code, user, kategori, severity, status, response, attachment, SLA, deletedAt. | Admin semua; user pemilik; update owner hanya pending. | `FirestoreReportDataSource.kt`, `IncidentReportDto.kt`, `IncidentReport.kt` |
| `incidentReports/{id}/statusHistories` | Riwayat perubahan status/severity/respon. | Admin tulis; admin/pemilik baca. | `StatusHistoryDto.kt`, `StatusHistory.kt` |
| `activityLogs` | Audit aktivitas penting. | Admin baca; user aktif boleh create log miliknya. | `FirestoreActivityLogDataSource.kt`, `ActivityLog.kt` |
| `deviceTokens` | Token FCM perangkat user. | User aktif kelola token sendiri; admin baca. | `FcmTokenDataSource.kt` |

## 7. File Kode yang Sudah Diberi Komentar Alur

Semua file Kotlin di `app/src/main/java/com/adit/sirs/` sudah diberi komentar `DOKUMENTASI ALUR SIRS` dan komentar `FLOW:` pada class/function/state utama. Komentar tersebut tidak mengubah logic, hanya menjelaskan hubungan antarbagian kode.

| File | Simbol/line penting |
|---|---|
| `app/src/main/java/com/adit/sirs/core/common/RateLimiter.kt` | 14: class RateLimiter @Inject constructor(; 32: fun canCreateReport(userId: String): RateLimitResult {; 37: fun canUploadFile(userId: String): RateLimitResult {; 42: fun recordReport(userId: String) {; ... |
| `app/src/main/java/com/adit/sirs/core/common/Result.kt` | 8: data class Success<T>(val data: T) : Result<T>(); 10: data class Error(val exception: Throwable, val message: String? = null |
| `app/src/main/java/com/adit/sirs/core/common/UiState.kt` | 10: data class Success<T>(val data: T) : UiState<T>(); 12: data class Error(val message: String) : UiState<Nothing>() |
| `app/src/main/java/com/adit/sirs/core/constants/AppConstants.kt` | 7: object AppConstants { |
| `app/src/main/java/com/adit/sirs/core/error/AppError.kt` | 8: data class AuthError(override val message: String) : AppError(); 10: data class NetworkError(override val message: String) : AppError(); 12: data class FirestoreError(override val message: String) : AppError(); 14: data class ValidationError(override val message: String) : AppError(); ... |
| `app/src/main/java/com/adit/sirs/core/error/ErrorMapper.kt` | 10: object ErrorMapper {; 12: fun mapException(e: Throwable): AppError {; 24: private fun mapAuthError(e: FirebaseAuthException): AppError.AuthError; 39: private fun mapFirestoreError(e: FirebaseFirestoreException): AppError; ... |
| `app/src/main/java/com/adit/sirs/core/firebase/AnalyticsHelper.kt` | 15: class AnalyticsHelper @Inject constructor() {; 23: fun logLoginSuccess(role: String) {; 31: fun logReportCreated(severity: String, hasAttachment: Boolean) {; 40: fun logReportStatusViewed(status: String) {; ... |
| `app/src/main/java/com/adit/sirs/core/firebase/FirebaseProvider.kt` | 19: object FirebaseProvider {; 24: fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance(); 29: fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.; 34: fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.; ... |
| `app/src/main/java/com/adit/sirs/core/security/FileValidation.kt` | 14: data class FileValidationResult(; 23: object FileValidation {; 28: fun validateFile(; 106: private fun readFileMetadata(contentResolver: ContentResolver, uri: Ur; ... |
| `app/src/main/java/com/adit/sirs/core/security/MimeTypeValidator.kt` | 9: object MimeTypeValidator {; 11: fun isAllowed(mimeType: String?): Boolean {; 16: fun getExtensionFromMime(mimeType: String): String? { |
| `app/src/main/java/com/adit/sirs/core/security/SlaCalculator.kt` | 11: object SlaCalculator {; 13: fun hoursFor(severity: Severity): Long = when (severity) {; 21: fun deadlineFrom(submittedAt: Timestamp, severity: Severity): Timestam; 27: fun isOverdue(deadline: Timestamp?, nowMillis: Long = System.currentTi; ... |
| `app/src/main/java/com/adit/sirs/core/util/DateFormatter.kt` | 12: object DateFormatter {; 18: fun formatTimestamp(timestamp: Timestamp?): String {; 24: fun formatDate(date: Date?): String {; 30: fun formatDateOnly(timestamp: Timestamp?): String {; ... |
| `app/src/main/java/com/adit/sirs/core/util/FileSizeFormatter.kt` | 7: object FileSizeFormatter {; 9: fun format(bytes: Long): String { |
| `app/src/main/java/com/adit/sirs/data/mapper/ActivityLogMapper.kt` | 10: object ActivityLogMapper {; 12: fun toDomain(dto: ActivityLogDto): ActivityLog { |
| `app/src/main/java/com/adit/sirs/data/mapper/CategoryMapper.kt` | 10: object CategoryMapper {; 12: fun toDomain(dto: CategoryDto): Category {; 30: private fun defaultGuidance(slugOrName: String): Pair<List<String>, Li |
| `app/src/main/java/com/adit/sirs/data/mapper/ReportMapper.kt` | 14: object ReportMapper {; 16: fun toDomain(dto: IncidentReportDto): IncidentReport {; 43: private fun mapAttachment(map: Map<String, Any>): Attachment {; 57: fun attachmentToMap(attachment: Attachment): Map<String, Any?> { |
| `app/src/main/java/com/adit/sirs/data/mapper/StatusHistoryMapper.kt` | 10: object StatusHistoryMapper {; 12: fun toDomain(dto: StatusHistoryDto): StatusHistory { |
| `app/src/main/java/com/adit/sirs/data/mapper/UserMapper.kt` | 11: object UserMapper {; 13: fun toDomain(dto: UserDto): User {; 28: fun toDto(user: User): UserDto { |
| `app/src/main/java/com/adit/sirs/data/model/ActivityLogDto.kt` | 10: data class ActivityLogDto( |
| `app/src/main/java/com/adit/sirs/data/model/CategoryDto.kt` | 11: data class CategoryDto( |
| `app/src/main/java/com/adit/sirs/data/model/IncidentReportDto.kt` | 10: data class IncidentReportDto( |
| `app/src/main/java/com/adit/sirs/data/model/StatusHistoryDto.kt` | 10: data class StatusHistoryDto( |
| `app/src/main/java/com/adit/sirs/data/model/UserDto.kt` | 11: data class UserDto( |
| `app/src/main/java/com/adit/sirs/data/remote/CloudinaryUploadDataSource.kt` | 25: class CloudinaryUploadDataSource @Inject constructor() { |
| `app/src/main/java/com/adit/sirs/data/remote/FcmTokenDataSource.kt` | 17: class FcmTokenDataSource @Inject constructor( |
| `app/src/main/java/com/adit/sirs/data/remote/FirebaseAuthDataSource.kt` | 14: class FirebaseAuthDataSource @Inject constructor(; 38: fun signOut() {; 48: fun isLoggedIn(): Boolean = auth.currentUser != null |
| `app/src/main/java/com/adit/sirs/data/remote/FirestoreActivityLogDataSource.kt` | 20: class FirestoreActivityLogDataSource @Inject constructor(; 52: fun observeActivityLogs(): Flow<List<ActivityLogDto>> = callbackFlow { |
| `app/src/main/java/com/adit/sirs/data/remote/FirestoreCategoryDataSource.kt` | 20: class FirestoreCategoryDataSource @Inject constructor(; 82: fun observeActiveCategories(): Flow<List<CategoryDto>> = callbackFlow ; 100: fun observeAllCategories(): Flow<List<CategoryDto>> = callbackFlow { |
| `app/src/main/java/com/adit/sirs/data/remote/FirestoreReportDataSource.kt` | 21: class FirestoreReportDataSource @Inject constructor(; 79: fun observeUserReports(userId: String, status: String? = null): Flow<L; 108: fun observeAllReports(status: String? = null): Flow<List<IncidentRepor; 136: fun observeReport(reportId: String): Flow<IncidentReportDto?> = callba; ... |
| `app/src/main/java/com/adit/sirs/data/remote/FirestoreUserDataSource.kt` | 17: class FirestoreUserDataSource @Inject constructor( |
| `app/src/main/java/com/adit/sirs/data/repository/AdminRepositoryImpl.kt` | 22: class AdminRepositoryImpl @Inject constructor(; 30: override fun observeAllReports(status: String?): Flow<List<IncidentRep; 37: override suspend fun updateReportStatus(; 109: override fun observeActivityLogs(): Flow<List<ActivityLog>> {; ... |
| `app/src/main/java/com/adit/sirs/data/repository/AuthRepositoryImpl.kt` | 20: class AuthRepositoryImpl @Inject constructor(; 32: override suspend fun login(email: String, password: String): Result<Us; 58: override suspend fun register(name: String, email: String, password: S; 91: override suspend fun logout(): Result<Unit> {; ... |
| `app/src/main/java/com/adit/sirs/data/repository/CategoryRepositoryImpl.kt` | 19: class CategoryRepositoryImpl @Inject constructor(; 25: override fun observeActiveCategories(): Flow<List<Category>> {; 32: override fun observeAllCategories(): Flow<List<Category>> {; 39: override suspend fun createCategory(name: String, slug: String, descri; ... |
| `app/src/main/java/com/adit/sirs/data/repository/ReportRepositoryImpl.kt` | 25: class ReportRepositoryImpl @Inject constructor(; 33: override fun observeUserReports(userId: String, status: String?): Flow; 40: override fun observeReport(reportId: String): Flow<IncidentReport?> {; 47: override fun observeStatusHistories(reportId: String): Flow<List<Statu; ... |
| `app/src/main/java/com/adit/sirs/domain/model/ActivityLog.kt` | 9: data class ActivityLog( |
| `app/src/main/java/com/adit/sirs/domain/model/Attachment.kt` | 9: data class Attachment( |
| `app/src/main/java/com/adit/sirs/domain/model/Category.kt` | 9: data class Category( |
| `app/src/main/java/com/adit/sirs/domain/model/IncidentReport.kt` | 9: data class IncidentReport( |
| `app/src/main/java/com/adit/sirs/domain/model/IncidentStatus.kt` | 7: enum class IncidentStatus(val value: String, val displayName: String) ; 15: fun fromString(value: String): IncidentStatus { |
| `app/src/main/java/com/adit/sirs/domain/model/Severity.kt` | 7: enum class Severity(val value: String, val displayName: String) {; 15: fun fromString(value: String): Severity { |
| `app/src/main/java/com/adit/sirs/domain/model/StatusHistory.kt` | 9: data class StatusHistory( |
| `app/src/main/java/com/adit/sirs/domain/model/User.kt` | 9: data class User( |
| `app/src/main/java/com/adit/sirs/domain/model/UserRole.kt` | 7: enum class UserRole(val value: String) {; 13: fun fromString(value: String): UserRole { |
| `app/src/main/java/com/adit/sirs/domain/repository/AdminRepository.kt` | 13: fun observeAllReports(status: String? = null): Flow<List<IncidentRepor; 26: fun observeActivityLogs(): Flow<List<ActivityLog>> |
| `app/src/main/java/com/adit/sirs/domain/repository/AuthRepository.kt` | 21: fun isLoggedIn(): Boolean |
| `app/src/main/java/com/adit/sirs/domain/repository/CategoryRepository.kt` | 12: fun observeActiveCategories(): Flow<List<Category>>; 14: fun observeAllCategories(): Flow<List<Category>> |
| `app/src/main/java/com/adit/sirs/domain/repository/ReportRepository.kt` | 16: fun observeUserReports(userId: String, status: String? = null): Flow<L; 18: fun observeReport(reportId: String): Flow<IncidentReport?>; 20: fun observeStatusHistories(reportId: String): Flow<List<StatusHistory> |
| `app/src/main/java/com/adit/sirs/MainActivity.kt` | 21: class MainActivity : ComponentActivity() {; 28: override fun onCreate(savedInstanceState: Bundle?) {; 40: private fun requestNotificationPermission() { |
| `app/src/main/java/com/adit/sirs/presentation/admin/ActivityLogScreen.kt` | 24: fun ActivityLogScreen(; 72: private fun ActivityLogCard(log: ActivityLog) { |
| `app/src/main/java/com/adit/sirs/presentation/admin/AdminReportDetailScreen.kt` | 38: fun AdminReportDetailScreen( |
| `app/src/main/java/com/adit/sirs/presentation/admin/AdminReportListScreen.kt` | 34: fun AdminReportListScreen( |
| `app/src/main/java/com/adit/sirs/presentation/admin/AdminViewModel.kt` | 28: class AdminViewModel @Inject constructor(; 106: private fun loadCurrentUser() {; 119: fun loadAllReports(statusFilter: String? = null) {; 131: fun setSearchQuery(query: String) {; ... |
| `app/src/main/java/com/adit/sirs/presentation/admin/CategoryManagementScreen.kt` | 30: fun CategoryManagementScreen(; 200: private fun CategoryCard( |
| `app/src/main/java/com/adit/sirs/presentation/admin/UpdateStatusSheet.kt` | 31: fun UpdateStatusSheet( |
| `app/src/main/java/com/adit/sirs/presentation/auth/AuthViewModel.kt` | 26: class AuthViewModel @Inject constructor(; 59: private fun checkCurrentUser() {; 71: private fun seedCategories() {; 110: fun login(email: String, password: String) {; ... |
| `app/src/main/java/com/adit/sirs/presentation/auth/ForgotPasswordScreen.kt` | 23: fun ForgotPasswordScreen( |
| `app/src/main/java/com/adit/sirs/presentation/auth/LoginScreen.kt` | 27: fun LoginScreen( |
| `app/src/main/java/com/adit/sirs/presentation/auth/RegisterScreen.kt` | 29: fun RegisterScreen( |
| `app/src/main/java/com/adit/sirs/presentation/components/DetailComponents.kt` | 26: fun DetailRow(label: String, value: String) {; 51: fun StatusHistoryItem(history: StatusHistory) {; 109: fun AttachmentCard(; 153: private fun rememberAttachmentFormat(format: String, mimeType: String, |
| `app/src/main/java/com/adit/sirs/presentation/components/EmptyState.kt` | 20: fun EmptyState( |
| `app/src/main/java/com/adit/sirs/presentation/components/ErrorView.kt` | 19: fun ErrorView( |
| `app/src/main/java/com/adit/sirs/presentation/components/InfoChip.kt` | 21: fun InfoChip( |
| `app/src/main/java/com/adit/sirs/presentation/components/LoadingView.kt` | 18: fun LoadingView(modifier: Modifier = Modifier) { |
| `app/src/main/java/com/adit/sirs/presentation/components/ReportCardItem.kt` | 26: fun ReportCardItem( |
| `app/src/main/java/com/adit/sirs/presentation/components/SeverityBadge.kt` | 22: fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) { |
| `app/src/main/java/com/adit/sirs/presentation/components/SirsTextField.kt` | 16: fun SirsTextField( |
| `app/src/main/java/com/adit/sirs/presentation/components/StatusBadge.kt` | 22: fun StatusBadge(status: IncidentStatus, modifier: Modifier = Modifier) |
| `app/src/main/java/com/adit/sirs/presentation/dashboard/AdminDashboardScreen.kt` | 35: fun AdminDashboardScreen(; 261: private fun StatCard(; 299: private fun ActionCard( |
| `app/src/main/java/com/adit/sirs/presentation/dashboard/DashboardViewModel.kt` | 26: data class DashboardStats(; 39: class DashboardViewModel @Inject constructor(; 68: private fun loadCurrentUser() {; 86: private fun loadUserReports(userId: String) {; ... |
| `app/src/main/java/com/adit/sirs/presentation/dashboard/UserDashboardScreen.kt` | 34: fun UserDashboardScreen(; 211: private fun StatCard(; 249: private fun ActionCard( |
| `app/src/main/java/com/adit/sirs/presentation/navigation/AppNavGraph.kt` | 25: fun AppNavGraph( |
| `app/src/main/java/com/adit/sirs/presentation/navigation/Routes.kt` | 7: object Routes {; 25: fun reportDetail(reportId: String) = "report_detail/$reportId"; 27: fun editReport(reportId: String) = "edit_report/$reportId"; 29: fun adminReportDetail(reportId: String) = "admin_report_detail/$report |
| `app/src/main/java/com/adit/sirs/presentation/profile/ProfileScreen.kt` | 24: fun ProfileScreen(; 88: private fun ProfileRow(label: String, value: String) { |
| `app/src/main/java/com/adit/sirs/presentation/profile/ProfileViewModel.kt` | 22: class ProfileViewModel @Inject constructor(; 37: private fun loadProfile() { |
| `app/src/main/java/com/adit/sirs/presentation/reports/CreateReportScreen.kt` | 33: fun CreateReportScreen(; 273: private fun CategoryGuidanceCard( |
| `app/src/main/java/com/adit/sirs/presentation/reports/EditReportScreen.kt` | 33: fun EditReportScreen( |
| `app/src/main/java/com/adit/sirs/presentation/reports/ReportDetailScreen.kt` | 31: fun ReportDetailScreen(; 294: private fun CategoryGuidanceCard( |
| `app/src/main/java/com/adit/sirs/presentation/reports/ReportFormState.kt` | 10: data class ReportFormState( |
| `app/src/main/java/com/adit/sirs/presentation/reports/ReportListScreen.kt` | 27: fun ReportListScreen( |
| `app/src/main/java/com/adit/sirs/presentation/reports/ReportViewModel.kt` | 33: class ReportViewModel @Inject constructor(; 104: private fun loadCurrentUser() {; 118: private fun loadCategories() {; 127: fun loadUserReports(statusFilter: String? = null) {; ... |
| `app/src/main/java/com/adit/sirs/presentation/theme/Theme.kt` | 46: fun SIRSTheme( |
| `app/src/main/java/com/adit/sirs/SirsApplication.kt` | 17: class SirsApplication : Application() {; 19: override fun onCreate() { |
| `app/src/main/java/com/adit/sirs/SirsFirebaseMessagingService.kt` | 10: class SirsFirebaseMessagingService : FirebaseMessagingService() {; 13: override fun onNewToken(token: String) {; 19: override fun onMessageReceived(message: RemoteMessage) { |

## 8. Catatan Validasi Implementasi dari Source Code

- Penghapusan laporan sudah diselaraskan antara aplikasi dan `firestore.rules`: admin boleh menghapus laporan, sedangkan user hanya boleh menghapus laporan miliknya sendiri selama status masih `pending` (`firestore.rules:66` dan `firestore.rules:91`).
- Kategori laporan tidak lagi dibuat otomatis dari `AuthViewModel`. Data kategori dikelola melalui fitur Manajemen Kategori admin, sehingga sumber data kategori lebih konsisten dengan konsep admin-managed data.
- Secret operasi delete Cloudinary tidak ditaruh hardcoded; endpoint/secret dibaca dari Gradle property/environment di `app/build.gradle.kts`.

## 9. Cara Menjalankan

1. Buka project di Android Studio.
2. Pastikan `app/google-services.json` tersedia.
3. Jalankan Gradle Sync.
4. Build/run ke emulator/perangkat Android API minimal 26.
5. Untuk build CLI: `./gradlew assembleDebug`.

## 10. Ringkasan Presentasi Singkat

- Jelaskan bahwa SIRS memisahkan UI, state, domain, dan data source.
- Tunjukkan `AppNavGraph.kt` untuk alur navigasi role.
- Tunjukkan `ReportViewModel.kt` untuk flow laporan dari form sampai Firestore.
- Tunjukkan `FileValidation.kt` untuk keamanan upload.
- Tunjukkan `AdminViewModel.kt` dan `FirestoreReportDataSource.kt` untuk alur admin menangani laporan.
- Tunjukkan `firestore.rules` sebagai bukti hak akses database.

