# PRD dan SRS Android Secure Incident Reporting System — Firebase Spark + Cloudinary Free

**Target platform:** Android Native  
**Stack utama:** Kotlin, Jetpack Compose, Firebase Authentication, Cloud Firestore, Firestore Security Rules, Firebase Cloud Messaging, Firebase Crashlytics, Firebase Analytics, Firebase App Check, Cloudinary Free  
**Strategi backend:** Firebase Spark/free-plan friendly + Cloudinary Free untuk attachment  
**Status dokumen:** Draft implementasi untuk AI Agent  
**Basis domain:** Project SIRS Laravel existing, tetapi dibangun ulang sebagai aplikasi mobile native  
**Keputusan produk:** Laravel tidak digunakan sebagai backend produksi. Firebase digunakan untuk auth, database, realtime, rules, dan observability. Cloudinary Free digunakan untuk upload bukti gambar/PDF tanpa Cloudinary API Secret di Android.

---

# BAGIAN I — PRODUCT REQUIREMENTS DOCUMENT (PRD)

## 1. Ringkasan Eksekutif

Secure Incident Reporting System Android adalah aplikasi mobile native untuk pelaporan, pemantauan, dan penanganan insiden keamanan. Pengguna dapat membuat laporan insiden, memilih kategori, menentukan tingkat keparahan, mengisi lokasi kejadian, mengunggah bukti melalui Cloudinary Free, dan memantau status laporan secara realtime melalui Cloud Firestore. Administrator dapat melihat seluruh laporan, mengubah status penanganan, memberikan tanggapan, mengelola kategori, dan melihat activity log.

Dokumen ini memodifikasi strategi produk agar tetap hemat biaya: Firebase memakai layanan yang ramah Spark/free plan, sedangkan attachment tidak memakai Firebase Storage, tetapi memakai Cloudinary Free. Untuk menjaga implementasi tetap realistis tanpa server sendiri, upload Cloudinary pada MVP menggunakan **unsigned upload preset yang dibatasi ketat**. Pendekatan ini tidak sekuat signed upload via backend, tetapi paling cocok jika requirement utama adalah gratis, sederhana, dan tetap bisa upload bukti.

Tujuan utama produk adalah menghadirkan sistem pelaporan insiden yang aman, realtime, mobile-first, mudah diuji, dapat diaudit, dan dapat berjalan untuk demo/MVP dengan biaya nol selama pemakaian masih di bawah batas free tier masing-masing layanan.

---

## 2. Keputusan Arsitektur Utama

Keputusan final untuk build ulang:

```text
Android Kotlin + Jetpack Compose
        |
        | Firebase SDK
        v
Firebase Authentication
Cloud Firestore
Firestore Security Rules
Firebase Cloud Messaging
Crashlytics
Analytics
App Check
        |
        | Unsigned upload preset terbatas
        v
Cloudinary Free
```

Laravel existing tidak digunakan sebagai backend produksi untuk aplikasi Android. Namun domain dari Laravel existing tetap menjadi acuan:

- User dan role: `user`, `administrator`.
- Report/laporan insiden.
- Category/kategori laporan.
- Attachment/bukti laporan.
- Status report: `pending`, `investigating`, `resolved`, `rejected`.
- Severity: `low`, `medium`, `high`, `critical`.
- Activity log.
- Validasi upload maksimal 2 MB, format JPG/JPEG/PNG/PDF.
- User hanya boleh mengubah laporan jika status masih `pending`.

### 2.1 Keputusan Penting Terkait Free Plan

1. **Cloudinary digunakan kembali**, tetapi hanya untuk attachment bukti.
2. **Firebase Storage tidak digunakan** agar tidak menambah area biaya/kuota storage Firebase.
3. **Cloud Functions tidak menjadi requirement wajib untuk MVP Spark** karena strict Spark/free-plan sebaiknya tidak bergantung pada backend function.
4. Upload Cloudinary memakai **unsigned upload preset** yang dibatasi allowed format, max file size, folder, dan mode non-overwrite.
5. Cloudinary API Secret **tidak boleh** disimpan di Android.
6. Field attachment yang disimpan di Firestore hanya metadata dan URL aman dari Cloudinary.
7. Jika nanti keamanan upload harus lebih kuat, upgrade path adalah Cloud Functions signed upload pada Firebase Blaze atau backend kecil terpisah.

---

## 3. Visi Produk

Menyediakan aplikasi mobile pelaporan insiden keamanan yang aman, realtime, mudah digunakan, dan dapat diaudit, dengan backend Firebase serta attachment storage berbasis Cloudinary Free.

---

## 4. Tujuan Produk

### 4.1 Tujuan Utama

1. Membuat aplikasi Android native untuk pelaporan insiden.
2. Mengganti backend Laravel dengan Firebase secara penuh untuk mobile app.
3. Menyediakan login/register berbasis Firebase Authentication.
4. Menyimpan data laporan, kategori, user profile, status history, attachment metadata, dan activity log di Cloud Firestore.
5. Mengunggah bukti laporan ke Cloudinary Free tanpa menyimpan API Secret di Android.
6. Menyediakan role-based access control untuk user dan administrator melalui Firestore Security Rules.
7. Menyediakan realtime status update melalui snapshot listener Firestore.
8. Menjaga audit trail melalui activity log.
9. Mengurangi risiko abuse upload dengan validasi client, Cloudinary upload preset restriction, dan monitoring kuota.
10. Menyediakan struktur project Android yang rapi agar mudah dilanjutkan AI Agent lain.

### 4.2 Indikator Keberhasilan

| ID | Indikator | Target |
|---|---|---|
| KPI-01 | Auth berjalan | User dapat register, login, logout |
| KPI-02 | Role berjalan | User dan administrator diarahkan ke dashboard berbeda |
| KPI-03 | Report CRUD berjalan | User dapat membuat, melihat, mengubah, dan menghapus laporan pending |
| KPI-04 | Admin flow berjalan | Admin dapat melihat semua laporan dan update status |
| KPI-05 | Cloudinary Free berjalan | Bukti JPG/PNG/PDF max 2 MB dapat diupload dan URL tersimpan |
| KPI-06 | Firestore rules aman | User tidak dapat membaca/mengubah data user lain |
| KPI-07 | Activity log berjalan | Aksi penting tercatat |
| KPI-08 | Realtime update berjalan | User/admin melihat perubahan status melalui Firestore listener |
| KPI-09 | Kuota free-plan aman | Query, upload, dan file size dibatasi agar tidak boros kuota |
| KPI-10 | Build Android sukses | APK/debug build berhasil tanpa error |

---

## 5. Stakeholder

| Stakeholder | Kepentingan |
|---|---|
| User/Pelapor | Membuat laporan, upload bukti, memantau status |
| Administrator | Memverifikasi dan menangani laporan |
| Pengelola organisasi | Mendapat visibilitas insiden dan status penanganan |
| Tim developer Android | Membangun UI, state, Firebase integration, Cloudinary upload |
| Auditor/Reviewer | Memeriksa security rules, log, dan kontrol akses |
| Dosen/Penguji | Menilai pemenuhan requirement dan secure coding |

---

## 6. Persona Pengguna

### 6.1 User/Pelapor

User adalah mahasiswa, pegawai, atau anggota organisasi yang menemukan insiden keamanan.

Kebutuhan:

- Register dan login dengan aman.
- Membuat laporan insiden secara cepat.
- Mengunggah bukti berupa gambar atau PDF.
- Melihat status laporan.
- Mengubah laporan jika belum diproses.
- Mendapat update status secara realtime saat membuka aplikasi.

Batasan akses:

- Hanya boleh melihat laporan miliknya.
- Tidak boleh mengubah status laporan.
- Tidak boleh membaca activity log global.
- Tidak boleh mengelola kategori.

### 6.2 Administrator

Administrator adalah petugas yang meninjau dan menangani laporan.

Kebutuhan:

- Melihat semua laporan.
- Filter laporan berdasarkan status, severity, kategori, tanggal.
- Mengubah status laporan.
- Memberikan tanggapan admin.
- Mengelola kategori.
- Melihat activity log.
- Melihat laporan critical/high yang belum selesai.

Batasan akses:

- Tidak boleh mengubah email/password user langsung dari client.
- Aksi admin harus tercatat di activity log.
- Admin dibuat manual melalui Firebase Console, bukan dari register umum.

---

## 7. Ruang Lingkup Produk

### 7.1 In Scope

1. Android native app dengan Kotlin + Jetpack Compose.
2. Firebase Authentication email/password.
3. Cloud Firestore sebagai database utama.
4. Firestore Security Rules.
5. Realtime listener Firestore untuk status laporan.
6. Cloudinary Free untuk upload attachment.
7. FCM token management untuk kebutuhan notifikasi dasar.
8. Crashlytics dan Analytics.
9. App Check untuk mengurangi abuse terhadap resource Firebase.
10. Role user dan administrator.
11. CRUD laporan untuk user.
12. Status management untuk admin.
13. Category management untuk admin.
14. Activity log.
15. Dokumentasi struktur data dan flow.

### 7.2 Out of Scope untuk Versi Awal

1. iOS app.
2. Web admin baru.
3. Chat realtime antara user dan admin.
4. Multi-tenant organisasi kompleks.
5. Payment/subscription.
6. Integrasi email SMTP custom.
7. Migrasi otomatis penuh dari database Laravel ke Firestore.
8. OCR/AI analysis pada attachment.
9. End-to-end encryption custom untuk seluruh laporan.
10. Firebase Storage untuk attachment utama.
11. Cloudinary signed upload via Cloud Functions sebagai fitur wajib MVP.
12. Push notification server-side otomatis jika membutuhkan backend trigger berbayar.

---

## 8. Fitur Produk

### 8.1 Authentication

Fitur:

- Register email/password.
- Login email/password.
- Logout.
- Reset password via Firebase email reset.
- Session persistence memakai Firebase Auth.
- User profile tersimpan di Firestore collection `users`.

Catatan implementasi:

- Firebase Auth menyimpan identitas auth dasar.
- Role, name, isActive, createdAt disimpan di Firestore.
- Setelah register, role default harus `user`.
- Pembuatan administrator tidak boleh dari register umum.
- Admin dibuat manual melalui Firebase Console atau admin script lokal saat development.

### 8.2 User Dashboard

Menampilkan:

- Total laporan milik user.
- Jumlah laporan pending/investigating/resolved/rejected.
- Laporan terbaru.
- Shortcut buat laporan.
- Indikator laporan yang memiliki attachment.

### 8.3 Report Management untuk User

User dapat:

- Membuat laporan.
- Melihat daftar laporan miliknya.
- Melihat detail laporan.
- Mengubah laporan jika status `pending`.
- Menghapus laporan jika status `pending`.
- Melihat attachment yang pernah diupload.

Field form laporan:

- `title`
- `categoryId`
- `description`
- `location`
- `incidentDate`
- `severity`
- `attachment` optional

Validasi:

- `title` required, min 5, max 150.
- `description` required, min 20, max 4000.
- `location` required, min 3, max 255.
- `incidentDate` tidak boleh melewati hari ini.
- `severity` harus salah satu `low`, `medium`, `high`, `critical`.
- `categoryId` harus kategori aktif.
- Attachment optional, max 2 MB, hanya JPG/JPEG/PNG/PDF.
- Report hanya boleh diubah/dihapus jika status masih `pending`.

### 8.4 Admin Dashboard

Menampilkan:

- Total seluruh laporan.
- Statistik status.
- Statistik severity.
- Laporan terbaru.
- Laporan critical/high yang belum selesai.
- Shortcut activity log dan category management.

### 8.5 Admin Report Handling

Admin dapat:

- Melihat semua laporan.
- Filter dan search laporan.
- Melihat detail laporan.
- Mengubah status laporan.
- Memberikan admin response.
- Melihat status history.

Aturan:

- Update status hanya bisa dilakukan oleh role `administrator`.
- Status history harus tercatat setiap status berubah.
- Jika status pertama kali berubah dari `pending`, field `handledBy` dan `handledAt` diisi.
- Semua perubahan status harus menulis activity log.

### 8.6 Category Management

Admin dapat:

- Membuat kategori.
- Mengubah nama/deskripsi kategori.
- Mengaktifkan/nonaktifkan kategori.
- Soft delete kategori secara logical melalui `isActive = false` atau `deletedAt`.

User hanya dapat melihat kategori aktif.

### 8.7 Attachment Upload dengan Cloudinary Free

Attachment disimpan di Cloudinary Free. Firestore hanya menyimpan metadata attachment dan URL aman.

Attachment valid:

- JPG
- JPEG
- PNG
- PDF
- Maksimal 2 MB per file
- Maksimal 1 attachment per report untuk MVP

Flow upload MVP free-plan:

1. Android memilih file dari Storage Access Framework.
2. Android melakukan validasi awal ukuran dan MIME.
3. Android upload langsung ke Cloudinary memakai unsigned upload preset.
4. Cloudinary upload preset membatasi folder, format, max file size, overwrite false, dan unique filename.
5. Android menerima `secure_url`, `public_id`, `bytes`, `format`, `resource_type`.
6. Android menyimpan metadata attachment ke Firestore bersama report.
7. Activity log dibuat di Firestore sesuai aturan akses.

Konfigurasi Cloudinary Free yang wajib:

```text
CLOUDINARY_CLOUD_NAME=<cloud-name>
CLOUDINARY_UPLOAD_PRESET=sirs_unsigned_report_evidence
CLOUDINARY_UPLOAD_FOLDER=sirs/incident-reports
ALLOWED_FORMATS=jpg,jpeg,png,pdf
MAX_FILE_SIZE=2097152
UNIQUE_FILENAME=true
OVERWRITE=false
USE_FILENAME=false
```

Catatan keamanan:

- Cloudinary API Secret tidak boleh masuk Android source code.
- Cloudinary API Key dan cloud name boleh berada di client, tetapi jangan perlakukan sebagai secret.
- Unsigned upload preset dapat disalahgunakan jika bocor, jadi preset harus dibatasi ketat.
- Gunakan folder khusus untuk SIRS agar monitoring dan cleanup mudah.
- Jangan izinkan video pada MVP karena cepat menghabiskan kuota free.
- Jika abuse terjadi, rotate/nonaktifkan upload preset dan buat preset baru.

### 8.8 Notification

Pada MVP Spark + Cloudinary Free, notification dibagi dua:

| Event | Mode MVP | Catatan |
|---|---|---|
| Report created | Realtime Firestore listener admin | Tidak wajib push server-side |
| Status updated | Realtime Firestore listener user | User melihat perubahan saat app terbuka |
| Critical report created | Admin dashboard highlight | Push otomatis optional |
| Admin response updated | Realtime Firestore listener user | Push otomatis optional |

FCM tetap boleh disiapkan untuk token management, tetapi pengiriman notifikasi otomatis dari backend tidak menjadi syarat MVP jika tidak memakai Cloud Functions.

### 8.9 Activity Log

Aksi yang wajib dicatat:

- `user.registered`
- `report.created`
- `report.updated`
- `report.deleted`
- `attachment.uploaded`
- `report.status_updated`
- `report.response_updated`
- `category.created`
- `category.updated`
- `category.disabled`

Catatan:

- Karena MVP tidak mewajibkan Cloud Functions, sebagian log dibuat dari client dengan Firestore Rules ketat.
- Rules harus memastikan user hanya dapat menulis log untuk aksinya sendiri dan admin hanya dapat menulis log admin.
- Untuk production serius, log sensitif sebaiknya dipindahkan ke trusted backend/Cloud Functions.

---

# BAGIAN II — SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

## 9. Arsitektur Sistem

### 9.1 Komponen Utama

| Komponen | Tanggung jawab |
|---|---|
| Android App | UI, local state, input validation awal, Firebase SDK integration, Cloudinary upload |
| Firebase Auth | Autentikasi email/password |
| Cloud Firestore | Database utama realtime/document-based |
| Firestore Rules | Authorization dan validasi akses data |
| Cloudinary Free | Penyimpanan attachment laporan |
| FCM | Token management dan optional push notification |
| Crashlytics | Crash reporting |
| Analytics | Event analytics non-sensitif |
| App Check | Perlindungan akses Firebase dari app tidak resmi |

### 9.2 Prinsip Arsitektur

1. Client Android tidak boleh menyimpan secret.
2. Semua authorization final untuk data Firestore berada di Firestore Rules.
3. Upload Cloudinary harus dibatasi melalui unsigned upload preset yang ketat.
4. Data denormalisasi boleh dilakukan untuk kebutuhan query Firestore.
5. Query harus mengikuti batasan Firestore dan membutuhkan composite index yang jelas.
6. Attachment binary disimpan di Cloudinary, bukan di Firestore.
7. Firestore hanya menyimpan metadata dan URL attachment.
8. App harus tetap bisa dipakai tanpa Cloud Functions untuk MVP.
9. Upgrade ke Cloud Functions signed upload disiapkan sebagai opsi production hardening.

---

## 10. Struktur Project Android

Struktur yang wajib diikuti AI Agent:

```text
app/src/main/java/com/sirs/android/
  SirsApplication.kt
  MainActivity.kt

  core/
    common/
      Result.kt
      UiState.kt
    constants/
      AppConstants.kt
    error/
      AppError.kt
      ErrorMapper.kt
    firebase/
      FirebaseProvider.kt
    security/
      FileValidation.kt
      MimeTypeValidator.kt
    util/
      DateFormatter.kt
      FileSizeFormatter.kt

  data/
    mapper/
    model/
      UserDto.kt
      IncidentReportDto.kt
      CategoryDto.kt
      ActivityLogDto.kt
      AttachmentDto.kt
    remote/
      FirebaseAuthDataSource.kt
      FirestoreReportDataSource.kt
      FirestoreCategoryDataSource.kt
      CloudinaryUploadDataSource.kt
      FcmTokenDataSource.kt
    repository/
      AuthRepositoryImpl.kt
      ReportRepositoryImpl.kt
      CategoryRepositoryImpl.kt
      AdminRepositoryImpl.kt

  domain/
    model/
      User.kt
      IncidentReport.kt
      Category.kt
      Attachment.kt
      ActivityLog.kt
      Severity.kt
      IncidentStatus.kt
      UserRole.kt
    repository/
      AuthRepository.kt
      ReportRepository.kt
      CategoryRepository.kt
      AdminRepository.kt
    usecase/
      auth/
      report/
      admin/
      category/

  presentation/
    navigation/
      AppNavGraph.kt
      Routes.kt
    theme/
      Color.kt
      Theme.kt
      Type.kt
    components/
      StatusBadge.kt
      SeverityBadge.kt
      ErrorView.kt
      LoadingView.kt
      EmptyState.kt
      SirsTextField.kt
    auth/
      LoginScreen.kt
      RegisterScreen.kt
      ForgotPasswordScreen.kt
      AuthViewModel.kt
    dashboard/
      UserDashboardScreen.kt
      AdminDashboardScreen.kt
      DashboardViewModel.kt
    reports/
      ReportListScreen.kt
      ReportDetailScreen.kt
      CreateReportScreen.kt
      EditReportScreen.kt
      ReportFormState.kt
      ReportViewModel.kt
    admin/
      AdminReportListScreen.kt
      AdminReportDetailScreen.kt
      UpdateStatusSheet.kt
      CategoryManagementScreen.kt
      ActivityLogScreen.kt
      AdminViewModel.kt
    profile/
      ProfileScreen.kt
      ProfileViewModel.kt
```

Aturan penting:

- `MainActivity` hanya setup Compose dan navigation.
- Tidak boleh ada query Firestore langsung di Composable.
- ViewModel memanggil usecase/repository.
- Repository memanggil data source.
- Mapper memisahkan DTO Firestore dari domain model.
- Cloudinary upload logic diletakkan di `CloudinaryUploadDataSource`, bukan di UI.

---

## 11. Firestore Data Model

### 11.1 Collection `users`

Path:

```text
users/{uid}
```

Fields:

| Field | Type | Required | Keterangan |
|---|---|---|---|
| `uid` | string | yes | Sama dengan Firebase Auth UID |
| `name` | string | yes | Nama user |
| `email` | string | yes | Email user |
| `role` | string | yes | `user` atau `administrator` |
| `isActive` | boolean | yes | Status aktif akun |
| `photoUrl` | string/null | no | Foto profil optional |
| `createdAt` | timestamp | yes | Server timestamp |
| `updatedAt` | timestamp | yes | Server timestamp |
| `lastLoginAt` | timestamp/null | no | Optional |

### 11.2 Collection `categories`

Path:

```text
categories/{categoryId}
```

Fields:

| Field | Type | Required |
|---|---|---|
| `name` | string | yes |
| `slug` | string | yes |
| `description` | string/null | no |
| `isActive` | boolean | yes |
| `createdBy` | string | yes |
| `createdAt` | timestamp | yes |
| `updatedAt` | timestamp | yes |
| `deletedAt` | timestamp/null | no |

### 11.3 Collection `incidentReports`

Path:

```text
incidentReports/{reportId}
```

Fields:

| Field | Type | Required | Keterangan |
|---|---|---|---|
| `reportCode` | string | yes | Format contoh `INC-2026-00001` |
| `userId` | string | yes | UID pelapor |
| `userName` | string | yes | Denormalized for admin list |
| `userEmail` | string | yes | Denormalized for admin list |
| `categoryId` | string | yes | ID kategori |
| `categoryName` | string | yes | Denormalized |
| `title` | string | yes | 5-150 chars |
| `description` | string | yes | 20-4000 chars |
| `location` | string | yes | 3-255 chars |
| `incidentDate` | timestamp | yes | Tidak boleh future date |
| `severity` | string | yes | low/medium/high/critical |
| `status` | string | yes | pending/investigating/resolved/rejected |
| `adminResponse` | string/null | no | Tanggapan admin |
| `handledBy` | string/null | no | UID admin |
| `handledByName` | string/null | no | Nama admin |
| `handledAt` | timestamp/null | no | Saat pertama ditangani |
| `attachment` | map/null | no | Metadata Cloudinary |
| `createdAt` | timestamp | yes | Server timestamp |
| `updatedAt` | timestamp | yes | Server timestamp |
| `deletedAt` | timestamp/null | no | Soft delete |

Attachment map:

```json
{
  "originalName": "evidence.pdf",
  "publicId": "sirs/incident-reports/abc/evidence",
  "secureUrl": "https://res.cloudinary.com/...",
  "resourceType": "raw",
  "format": "pdf",
  "mimeType": "application/pdf",
  "bytes": 102400,
  "uploadedAt": "serverTimestamp"
}
```

### 11.4 Subcollection `statusHistories`

Path:

```text
incidentReports/{reportId}/statusHistories/{historyId}
```

Fields:

| Field | Type | Required |
|---|---|---|
| `fromStatus` | string/null | yes |
| `toStatus` | string | yes |
| `note` | string/null | no |
| `changedBy` | string | yes |
| `changedByName` | string | yes |
| `createdAt` | timestamp | yes |

### 11.5 Collection `activityLogs`

Path:

```text
activityLogs/{logId}
```

Fields:

| Field | Type | Required |
|---|---|---|
| `actorId` | string/null | no |
| `actorName` | string/null | no |
| `actorRole` | string/null | no |
| `action` | string | yes |
| `entityType` | string/null | no |
| `entityId` | string/null | no |
| `description` | string | yes |
| `context` | map/null | no |
| `createdAt` | timestamp | yes |

### 11.6 Collection `deviceTokens`

Path:

```text
deviceTokens/{tokenId}
```

Fields:

| Field | Type | Required |
|---|---|---|
| `uid` | string | yes |
| `token` | string | yes |
| `platform` | string | yes, `android` |
| `deviceName` | string/null | no |
| `role` | string | yes |
| `isActive` | boolean | yes |
| `createdAt` | timestamp | yes |
| `updatedAt` | timestamp | yes |

---

## 12. Cloudinary Requirement

### 12.1 Mode MVP: Unsigned Upload Preset

Mode ini dipilih karena target utama adalah Firebase Spark + Cloudinary Free tanpa backend custom.

Environment/config Android yang boleh disimpan:

```text
CLOUDINARY_CLOUD_NAME=<cloud-name>
CLOUDINARY_UPLOAD_PRESET=sirs_unsigned_report_evidence
CLOUDINARY_FOLDER=sirs/incident-reports
```

Yang tidak boleh disimpan di Android:

```text
CLOUDINARY_API_SECRET
```

### 12.2 Upload Preset Security Setting

Upload preset harus diatur di Cloudinary Console:

| Setting | Nilai rekomendasi |
|---|---|
| Signing mode | Unsigned |
| Folder | `sirs/incident-reports` |
| Allowed formats | `jpg,jpeg,png,pdf` |
| Max file size | 2 MB |
| Overwrite | false |
| Unique filename | true |
| Use filename | false |
| Resource type | auto |
| Access mode | public/readable via secure URL untuk MVP |

### 12.3 Cloudinary Response yang Disimpan

Simpan hanya field berikut ke Firestore:

- `public_id`
- `secure_url`
- `resource_type`
- `format`
- `bytes`
- `original_filename` jika tersedia
- `mimeType` hasil validasi app
- `uploadedAt`

Jangan simpan:

- API Secret.
- Signature.
- Informasi credential Cloudinary sensitif.
- EXIF/location metadata jika tidak dibutuhkan.

### 12.4 Risiko dan Mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Upload preset bocor | Orang lain bisa upload ke Cloudinary project | Batasi format, size, folder, overwrite false, monitoring usage, rotate preset |
| File berbahaya | Asset tidak valid masuk Cloudinary | Validasi MIME/size di Android dan Cloudinary preset |
| Kuota free habis | Upload gagal | Batasi 1 file/report, max 2 MB, kompres gambar sebelum upload |
| Abuse traffic | Bandwidth Cloudinary naik | Jangan tampilkan image ukuran asli di list; gunakan thumbnail/transformation ringan |
| Data sensitif di URL | Bukti dapat diakses via secure URL | Untuk MVP dianggap acceptable; production butuh signed/private delivery |

---

## 13. Firestore Security Rules Requirement

Security rules harus memenuhi prinsip berikut:

1. User hanya dapat membaca `users/{ownUid}`.
2. Admin dapat membaca user list.
3. User dapat membaca kategori aktif.
4. Admin dapat mengelola kategori.
5. User dapat membaca report miliknya.
6. Admin dapat membaca semua report.
7. User tidak boleh update status report.
8. User hanya boleh update report miliknya saat status masih pending.
9. Activity logs hanya boleh dibaca admin.
10. Client hanya boleh menulis field yang sesuai role.

Contoh rule konseptual:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function userDoc() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid));
    }

    function isActive() {
      return signedIn() && userDoc().data.isActive == true;
    }

    function isAdmin() {
      return isActive() && userDoc().data.role == 'administrator';
    }

    function isOwner(userId) {
      return isActive() && request.auth.uid == userId;
    }

    match /users/{uid} {
      allow read: if isAdmin() || (signedIn() && request.auth.uid == uid);
      allow create: if signedIn() && request.auth.uid == uid;
      allow update: if signedIn() && request.auth.uid == uid
        && !('role' in request.resource.data.diff(resource.data).changedKeys())
        && !('isActive' in request.resource.data.diff(resource.data).changedKeys());
      allow delete: if false;
    }

    match /categories/{categoryId} {
      allow read: if isActive() && (resource.data.isActive == true || isAdmin());
      allow create, update: if isAdmin();
      allow delete: if false;
    }

    match /incidentReports/{reportId} {
      allow read: if isAdmin() || (isActive() && resource.data.userId == request.auth.uid);
      allow create: if isActive()
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.status == 'pending';
      allow update: if isAdmin()
        || (isActive()
          && resource.data.userId == request.auth.uid
          && resource.data.status == 'pending'
          && request.resource.data.status == 'pending');
      allow delete: if false;

      match /statusHistories/{historyId} {
        allow read: if isAdmin() ||
          (isActive() && get(/databases/$(database)/documents/incidentReports/$(reportId)).data.userId == request.auth.uid);
        allow create: if isAdmin();
        allow update, delete: if false;
      }
    }

    match /activityLogs/{logId} {
      allow read: if isAdmin();
      allow create: if isActive() && request.resource.data.actorId == request.auth.uid;
      allow update, delete: if false;
    }

    match /deviceTokens/{tokenId} {
      allow read: if false;
      allow create, update: if isActive() && request.resource.data.uid == request.auth.uid;
      allow delete: if isActive() && resource.data.uid == request.auth.uid;
    }
  }
}
```

Catatan: rule di atas adalah konsep awal. AI Agent wajib menyesuaikan validasi field, allowed keys, enum status/severity, dan ukuran string agar rules lebih ketat.

---

## 14. Android Functional Requirements

### FR-01 Register

User dapat membuat akun baru menggunakan email, password, dan nama.

Acceptance Criteria:

- Email valid.
- Password minimal mengikuti kebijakan aplikasi.
- Setelah register, document user dibuat dengan role `user`.
- User diarahkan ke dashboard user.

### FR-02 Login

User/admin dapat login menggunakan Firebase Auth.

Acceptance Criteria:

- Login gagal menampilkan pesan jelas.
- User inactive tidak boleh masuk dashboard.
- Role menentukan navigation target.

### FR-03 Create Report

User dapat membuat laporan.

Acceptance Criteria:

- Semua field required tervalidasi.
- Attachment invalid ditolak sebelum upload.
- Jika attachment ada, upload ke Cloudinary berhasil sebelum report disimpan.
- Report tersimpan di Firestore dengan status `pending`.
- Activity log tercatat.

### FR-04 View Own Reports

User dapat melihat daftar laporan miliknya.

Acceptance Criteria:

- User tidak melihat laporan user lain.
- Data dapat difilter berdasarkan status/severity.
- Empty state tampil jika belum ada laporan.

### FR-05 Edit Pending Report

User dapat mengubah laporan jika status masih `pending`.

Acceptance Criteria:

- Tombol edit tidak muncul untuk status selain pending.
- Firestore Rules tetap menolak jika status bukan pending.
- Activity log tercatat.

### FR-06 Delete Pending Report

User dapat soft delete laporan pending.

Acceptance Criteria:

- Konfirmasi muncul sebelum delete.
- Field `deletedAt` terisi.
- Report tidak tampil di list normal.
- Activity log tercatat.

### FR-07 Admin View All Reports

Admin dapat melihat semua laporan.

Acceptance Criteria:

- List menampilkan data pelapor, status, severity, tanggal.
- Filter tersedia.
- Non-admin ditolak oleh UI dan Firestore Rules.

### FR-08 Admin Update Status

Admin dapat mengubah status dan tanggapan.

Acceptance Criteria:

- Status history bertambah.
- Activity log tercatat.
- User melihat perubahan melalui realtime listener.
- `handledBy` dan `handledAt` terisi saat pertama kali diproses.

### FR-09 Category Management

Admin dapat mengelola kategori.

Acceptance Criteria:

- User hanya melihat kategori aktif.
- Category update tercatat di activity log.

### FR-10 Attachment Upload

User dapat upload attachment ke Cloudinary Free.

Acceptance Criteria:

- File picker memakai Storage Access Framework.
- File > 2 MB ditolak.
- MIME selain JPG/JPEG/PNG/PDF ditolak.
- Upload progress tampil.
- Metadata Cloudinary tersimpan di Firestore.
- URL attachment dapat dibuka dari detail report.

---

## 15. Non-Functional Requirements

### 15.1 Security

1. Cloudinary API Secret tidak boleh berada di Android app.
2. Firestore rules harus diuji.
3. App Check wajib diaktifkan untuk production.
4. User inactive harus ditolak.
5. Attachment harus divalidasi MIME dan size sebelum upload.
6. Input string harus dibatasi panjangnya.
7. Data sensitif tidak boleh dikirim ke Analytics.
8. Crashlytics tidak boleh mencatat isi laporan secara mentah.
9. Upload preset Cloudinary harus dibatasi ketat.
10. Admin tidak boleh dibuat dari register umum.

### 15.2 Performance

1. List report memakai pagination atau query limit.
2. Gunakan composite index untuk query filter.
3. Hindari membaca semua activity log tanpa limit.
4. Gunakan Coil untuk image loading.
5. Hindari recomposition berat di Compose.
6. Gunakan thumbnail/transformation ringan untuk preview gambar.
7. Hindari menampilkan file PDF langsung di list.

### 15.3 Reliability

1. Firestore offline persistence boleh diaktifkan.
2. Upload Cloudinary harus punya progress dan retry handling.
3. Network error harus menampilkan pesan jelas.
4. Jika upload berhasil tetapi Firestore write gagal, tampilkan opsi retry simpan report.
5. Simpan temporary state form agar user tidak kehilangan input saat upload gagal.

### 15.4 Usability

1. Form harus memiliki error inline.
2. Loading state wajib ada.
3. Empty state wajib ada.
4. Status dan severity harus mudah dibedakan.
5. Navigation harus sederhana dan konsisten.
6. Preview nama file, ukuran file, dan tipe file harus tampil sebelum upload.

---

## 16. Query dan Index Firestore

Query yang dibutuhkan:

| Use case | Collection | Query |
|---|---|---|
| User report list | `incidentReports` | `where userId == uid`, `where deletedAt == null`, `orderBy createdAt desc` |
| User report by status | `incidentReports` | `where userId == uid`, `where status == x`, `orderBy createdAt desc` |
| Admin all reports | `incidentReports` | `where deletedAt == null`, `orderBy createdAt desc` |
| Admin by status | `incidentReports` | `where status == x`, `where deletedAt == null`, `orderBy createdAt desc` |
| Admin critical unresolved | `incidentReports` | `where severity in [high, critical]`, `where status in [pending, investigating]` |
| Categories active | `categories` | `where isActive == true`, `orderBy name asc` |
| Activity log | `activityLogs` | `orderBy createdAt desc`, `limit 50` |

Composite index kemungkinan dibutuhkan untuk kombinasi `where` + `orderBy`.

---

## 17. Analytics Event

Event yang boleh dicatat:

| Event | Parameter aman |
|---|---|
| `login_success` | role |
| `report_created` | severity, has_attachment |
| `report_status_viewed` | status |
| `upload_started` | mime_type, size_bucket |
| `upload_success` | mime_type, size_bucket |
| `upload_failed` | mime_type, size_bucket, error_type |

Tidak boleh mencatat:

- title laporan
- description laporan
- lokasi detail
- email user
- isi admin response
- URL attachment
- public ID attachment jika dianggap sensitif

---

## 18. Testing Requirement

### 18.1 Android Tests

Unit test:

- validation report form
- validation file MIME dan size
- mapper DTO-domain
- repository error mapping
- ViewModel state transition

UI test:

- login form validation
- create report form validation
- attachment validation error
- report list empty state
- status badge rendering

Integration/manual:

- register-login-logout
- create report tanpa attachment
- create report dengan attachment valid
- upload file invalid ditolak
- admin update status
- realtime update diterima user

### 18.2 Firebase Tests

Wajib test:

- Firestore rules user tidak bisa baca report orang lain.
- Firestore rules user tidak bisa baca activity log.
- Non-admin tidak bisa update status.
- User tidak bisa update report saat status bukan pending.
- User tidak bisa membuat report dengan status selain pending.
- Admin bisa update status.

Tools:

- Firebase Emulator Suite
- Firestore Rules Unit Testing

### 18.3 Cloudinary Tests

Wajib test:

- Upload JPG valid berhasil.
- Upload PNG valid berhasil.
- Upload PDF valid berhasil.
- File > 2 MB ditolak.
- File selain allowed format ditolak.
- Upload preset tidak mengizinkan overwrite.
- Metadata response tersimpan benar di Firestore.

---

## 19. Implementation Roadmap untuk AI Agent

### Phase 1 — Android Project Bootstrap

1. Buat project Kotlin + Jetpack Compose.
2. Setup package name.
3. Setup Gradle dependency Firebase BOM, Compose, Hilt, Coil, OkHttp/Retrofit untuk Cloudinary upload.
4. Tambahkan `google-services.json` setelah Firebase project tersedia.
5. Buat struktur folder clean architecture.
6. Buat theme dasar.
7. Buat navigation graph awal.

### Phase 2 — Firebase Setup

1. Setup Firebase Auth email/password.
2. Buat Firestore collections awal.
3. Buat Firestore Security Rules.
4. Buat seed manual untuk kategori awal.
5. Buat admin manual melalui Firebase Console.
6. Setup Emulator Suite.

### Phase 3 — Cloudinary Free Setup

1. Buat akun Cloudinary Free.
2. Buat unsigned upload preset khusus SIRS.
3. Set allowed format: jpg, jpeg, png, pdf.
4. Set max file size 2 MB.
5. Set folder `sirs/incident-reports`.
6. Set overwrite false dan unique filename true.
7. Simpan `cloudName` dan `uploadPreset` di Android config.

### Phase 4 — Auth Android

1. Implement LoginScreen.
2. Implement RegisterScreen.
3. Implement AuthRepository.
4. Load user profile dari Firestore.
5. Role-based navigation.
6. Logout.

### Phase 5 — Report User Flow

1. Implement category list.
2. Implement report form state dan validation.
3. Implement create report Firestore write.
4. Implement report list realtime/paginated.
5. Implement report detail.
6. Implement edit/delete pending report.

### Phase 6 — Cloudinary Upload Flow

1. Implement file picker.
2. Implement file validation.
3. Implement Cloudinary upload data source.
4. Implement upload progress.
5. Simpan metadata attachment saat create/update report.
6. Tampilkan preview attachment di detail report.

### Phase 7 — Admin Flow

1. Admin dashboard.
2. Admin report list.
3. Admin report detail.
4. Update status bottom sheet/dialog.
5. Category management.
6. Activity log screen.

### Phase 8 — Observability dan Security Hardening

1. Aktifkan App Check.
2. Setup Crashlytics.
3. Setup Analytics event aman.
4. Test Firestore rules.
5. Test invalid file upload.
6. Test role bypass scenario.
7. Build debug APK.
8. Buat checklist final.

---

## 20. Definition of Done

Project Android Firebase Spark + Cloudinary Free dianggap selesai jika:

1. User register/login/logout berhasil.
2. User profile Firestore otomatis dibuat.
3. Role user/admin berjalan.
4. User dapat membuat laporan.
5. User dapat upload attachment valid ke Cloudinary Free.
6. User dapat melihat laporan miliknya.
7. User tidak dapat melihat laporan orang lain.
8. User hanya dapat edit/delete laporan pending.
9. Admin dapat melihat semua laporan.
10. Admin dapat update status dan response.
11. Status history tercatat.
12. Activity log tercatat.
13. Realtime listener berjalan untuk update status.
14. Firestore rules diuji dan aman.
15. Cloudinary API Secret tidak ada di Android source.
16. Upload preset Cloudinary dibatasi ketat.
17. Crashlytics aktif.
18. Build Android berhasil.
19. Dokumentasi setup Firebase dan Cloudinary tersedia.

---

## 21. Instruksi Penting untuk AI Agent

AI Agent yang mengerjakan project ini wajib mengikuti instruksi berikut:

1. Jangan memakai Laravel sebagai backend utama.
2. Jangan memakai Firebase Storage untuk attachment utama.
3. Gunakan Cloudinary Free untuk attachment.
4. Jangan menyimpan Cloudinary API Secret di Android.
5. Jangan membuat semua logic di MainActivity.
6. Jangan bypass Firestore Security Rules dengan asumsi UI sudah aman.
7. Jangan membuat admin dari halaman register umum.
8. Jangan menulis data sensitif ke Analytics atau Crashlytics.
9. Jangan membuat fitur di luar scope sebelum core flow selesai.
10. Jangan mengklaim selesai sebelum build dan flow utama diuji.
11. Upload Cloudinary pada MVP memakai unsigned upload preset yang dibatasi ketat.
12. Gunakan Kotlin data class, repository pattern, ViewModel, dan Compose state yang rapi.

---

## 22. Prompt Singkat untuk AI Agent Berikutnya

```text
Bangun ulang SIRS sebagai Android native Kotlin + Jetpack Compose dengan Firebase Spark/free-plan friendly dan Cloudinary Free untuk attachment. Laravel existing hanya referensi domain, bukan backend. Gunakan Firebase Auth untuk login/register, Cloud Firestore untuk users/categories/incidentReports/statusHistories/activityLogs/deviceTokens, Firestore Security Rules untuk authorization, Cloudinary Free unsigned upload preset untuk attachment JPG/PNG/PDF max 2 MB, Crashlytics/Analytics/App Check untuk observability dan security. Jangan gunakan Firebase Storage untuk attachment utama. Jangan simpan Cloudinary API Secret di Android. Jangan jadikan UI sebagai satu-satunya kontrol akses. Implementasikan bertahap dan verifikasi build/test.
```

---

## 23. Referensi Domain dari Project Existing

Walaupun backend akan Firebase + Cloudinary Free, domain berikut diambil dari project SIRS existing:

| Domain | Referensi existing |
|---|---|
| Role user/admin | `app/Models/User.php` |
| Report status/severity | `app/Models/IncidentReport.php` |
| Report validation | `app/Http/Requests/StoreIncidentReportRequest.php` |
| Attachment validation | `app/Http/Requests/StoreIncidentReportRequest.php` |
| Activity log concept | `app/Services/ActivityLogService.php` |
| Status transition concept | `app/Services/IncidentReportService.php` |
| Role middleware concept | `app/Http/Middleware/EnsureUserHasRole.php` |
| Existing PRD/SRS web | `docs/Secure_Incident_Reporting_System_PRD_SRS.md` |

Dokumen ini menggantikan versi “Firebase Spark tanpa Cloudinary”. Jika ada konflik, dokumen Firebase Spark + Cloudinary Free ini yang harus diikuti.
