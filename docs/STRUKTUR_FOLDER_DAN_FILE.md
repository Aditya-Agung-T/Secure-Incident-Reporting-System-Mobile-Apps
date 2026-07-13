# Struktur Folder dan Penjelasan File Project SIRS

Dokumen ini menjelaskan fungsi dari setiap direktori (folder) dan file spesifik yang ada di dalam *source code* aplikasi SIRS (*Sistem Informasi Respons Insiden Keamanan Siber*).

Aplikasi ini menggunakan Kotlin, Jetpack Compose, dan mengadopsi standar desain arsitektur modern Android, yakni **Clean Architecture** dan **MVVM (Model-View-ViewModel)**.

Struktur utama source code berada di dalam folder:  
`app/src/main/java/com/adit/sirs/`

---

## 1. Root Package (`com.adit.sirs`)
- `MainActivity.kt`: Titik awal berjalannya UI Android (Single Activity). Ini adalah kanvas utama tempat Jetpack Compose (NavGraph) dirender.
- `SirsApplication.kt`: Kelas level aplikasi yang berjalan pertama kali saat aplikasi dibuka. Digunakan untuk inisialisasi awal Hilt (Dependency Injection) dan Firebase.
- `SirsFirebaseMessagingService.kt`: Menangani Push Notification (FCM). Mengurus token perangkat dan menangkap notifikasi saat aplikasi berada di *background*.

---

## 2. Folder `core/` (Utilitas & Keamanan)
Folder ini berisi komponen yang bersifat *reusable* (dapat digunakan kembali di berbagai tempat).

* **`common/`**
  * `Result.kt`: Class generik untuk merepresentasikan *Success* atau *Error* dari sebuah proses asinkron (misal dari Firestore).
  * `UiState.kt`: Merepresentasikan kondisi dari UI: `Loading`, `Success`, atau `Error`.
  * `RateLimiter.kt`: Mekanisme pencegahan spam untuk fungsi yang tidak boleh dipanggil terus menerus (misalnya pencegahan *submit* form ganda / *Denial of Wallet*).
* **`constants/`**
  * `AppConstants.kt`: Menyimpan nilai konstan (seperti panjang minimal deskripsi 20 karakter, nama koleksi Firestore).
* **`error/`**
  * `AppError.kt`: Mendefinisikan tipe-tipe error khusus aplikasi.
  * `ErrorMapper.kt`: Menerjemahkan *exception* asing dari Firebase/Sistem menjadi bahasa Indonesia yang mudah dipahami pengguna biasa.
* **`firebase/`**
  * `AnalyticsHelper.kt`: Alat bantu untuk mengirim log event ke Firebase Analytics (berguna untuk merekam alur *funnel* aplikasi).
  * `FirebaseProvider.kt`: Kelas penyedia *instance* statis layanan-layanan Firebase.
* **`security/`**
  * `FileValidation.kt`: Membaca *Magic Bytes* (header binari file) untuk memastikan file yang di-upload murni PDF, JPG, atau PNG (mencegah upload malware berkedok ekstensi .jpg).
  * `MimeTypeValidator.kt`: Memeriksa konsistensi ekstensi file dengan tipe MIME yang dideklarasikan oleh Android *ContentResolver*.
  * `SlaCalculator.kt`: Menghitung batas waktu maksimal (*Deadline/SLA*) penanganan insiden berdasarkan tingkat keparahan (Severity).
* **`util/`**
  * `DateFormatter.kt`: Mengonversi timestamp sistem menjadi format penanggalan yang rapi dan seragam di seluruh UI.
  * `FileSizeFormatter.kt`: Mengonversi ukuran Bytes menjadi format KB/MB yang bisa dibaca manusia.

---

## 3. Folder `data/` (Infrastruktur Data)
Lapisan penghubung internet, database (Firestore), penyimpanan file (Cloudinary), serta *Dependency Injection*.

* **`di/`**
  * `RepositoryModule.kt`: Pengaturan Hilt untuk mendeklarasikan implementasi *Repository* mana yang harus disuntikkan ke ViewModel.
* **`mapper/`**
  * File-file di sini (`CategoryMapper.kt`, `ReportMapper.kt`, `UserMapper.kt`, dll) bertugas mengubah objek data Firestore (DTO) ke objek murni *Domain* agar bebas dari atribut asing (seperti ID bawaan Firestore).
* **`model/`** (Data Transfer Object / DTO)
  * File berakhiran Dto (`UserDto.kt`, `IncidentReportDto.kt`, dll) adalah representasi persis dari data JSON di Firestore database.
* **`remote/`** (Data Source)
  * `CloudinaryUploadDataSource.kt`: Menangani komunikasi *Signed Upload* ke Cloudinary, serta memanggil Worker API eksternal (dengan JWT otentikasi) untuk meminta *Signature* upload dan otorisasi *delete* foto.
  * `FcmTokenDataSource.kt`: Menyimpan atau memperbarui Push Notification Token milik pengguna.
  * `FirebaseAuthDataSource.kt`: Menghubungkan form login dan register ke layanan otentikasi Firebase.
  * `FirestoreReportDataSource.kt`: Melakukan *Query*, simpan, dan update laporan ke *Collections* `incidentReports` di Firestore. Di sini juga ada fungsi *generate report code* (misal: SIRS-2026-XXX).
  * `FirestoreActivityLogDataSource.kt`, `FirestoreCategoryDataSource.kt`, `FirestoreUserDataSource.kt`: Masing-masing mengurus *CRUD* ke *Collections* Activity Log, Kategori Insiden, dan Profil User.
* **`repository/`**
  * `AuthRepositoryImpl.kt`, `ReportRepositoryImpl.kt`, `AdminRepositoryImpl.kt`, `CategoryRepositoryImpl.kt`: Kelas konkret dari *Interface* Repository, menggabungkan dan menyaring logika *Data Source* remote untuk diserahkan ke *ViewModel*.

---

## 4. Folder `domain/` (Bisnis Logic Inti)
Berisi *model* bersih dan kontrak-kontrak yang menjadi jembatan abstrak antara Data dan Presentation.

* **`model/`**
  * File seperti `User.kt`, `IncidentReport.kt`, `Attachment.kt`, dll. Model bisnis utama aplikasi yang digunakan oleh Jetpack Compose UI. Bersih dari implementasi Android/Firebase spesifik.
  * File Enum seperti `Severity.kt`, `IncidentStatus.kt`, `UserRole.kt` untuk menetapkan tipe status yang aman (Type-Safe).
* **`repository/`**
  * File *Interface* (`AuthRepository.kt`, `ReportRepository.kt`, dll) yang mengatur **apa** yang bisa dilakukan oleh data (misalnya: `suspend fun getReports()`), tapi tidak mengatur **bagaimana** data itu diambil.

---

## 5. Folder `presentation/` (Layar & Jetpack Compose UI)
Mencakup semua tampilan UI dan State Holder (ViewModel).

* **`admin/`**
  * Tampilan Admin: `AdminDashboardScreen.kt`, `AdminReportListScreen.kt`, `AdminReportDetailScreen.kt`, `ActivityLogScreen.kt`, `CategoryManagementScreen.kt`.
  * `AdminViewModel.kt`: Menyediakan *state* dan fungsi kueri yang mengatur apa saja yang tampil di layar Admin, termasuk mengupdate status.
  * `UpdateStatusSheet.kt`: Tampilan *Bottom Sheet* yang muncul saat admin mengubah status dan memberikan catatan respon.
* **`auth/`**
  * `LoginScreen.kt`, `RegisterScreen.kt`, `ForgotPasswordScreen.kt`: Seluruh *screen* untuk masuk atau mendaftar aplikasi.
  * `AuthViewModel.kt`: Mengurus state form *username/password* dan fungsi login/register.
* **`components/`**
  * Bagian-bagian kecil UI yang *Reusable*.
  * `DetailComponents.kt`, `EmptyState.kt`, `ErrorView.kt`, `LoadingView.kt`: Indikator standar untuk berbagai state UI.
  * `SirsTextField.kt`: Kolom input text utama yang seragam.
  * `ReportCardItem.kt`: Desain "Kartu" laporan yang terlihat di daftar.
  * `StatusBadge.kt` & `SeverityBadge.kt`: Label visual berwarna penanda *Pending/Selesai* atau *Low/Critical*.
* **`dashboard/`**
  * `UserDashboardScreen.kt` & `AdminDashboardScreen.kt`: Halaman awal (Beranda) untuk masing-masing tipe akun.
  * `DashboardViewModel.kt`: Logika perhitungan statistik (Total Laporan, Menunggu, Selesai).
* **`navigation/`**
  * `AppNavGraph.kt`: Pengatur *Pindah Layar* (Routing). Otak dari pergerakan UI aplikasi.
  * `Routes.kt`: Daftar konstanta string navigasi (misal `"login"`, `"report_detail/{id}"`).
* **`profile/`**
  * `ProfileScreen.kt` & `ProfileViewModel.kt`: Layar identitas pengguna yang menampilkan info akun dan eksekusi tombol "Logout".
* **`reports/`**
  * Tampilan Pengguna Umum: `ReportListScreen.kt`, `ReportDetailScreen.kt`, `CreateReportScreen.kt`, `EditReportScreen.kt`.
  * `ReportViewModel.kt`: Menangani form pembuatan laporan, validasi input (termasuk penyimpanan cache state upload gambar), dan pemanggilan ke Repository.
  * `ReportFormState.kt`: Membungkus *state* berbagai input (judul, deskripsi, lokasi) menjadi satu objek.

---

## 6. Folder `ui/theme/` (Desain dan Tema Aplikasi)
Folder ini mengatur tema visual global Jetpack Compose (Material Design 3) untuk seluruh aplikasi SIRS.

* `Color.kt`: Mendefinisikan palet warna identitas merek SIRS.
* `Type.kt`: Mendefinisikan aturan tipografi (jenis huruf, ketebalan, dan ukuran font).
* `Theme.kt`: Membungkus konfigurasi warna dan font menjadi `SIRSTheme`, sehingga tampilan aplikasi otomatis beradaptasi (responsif) dan mendukung *Dark Mode*.

## 7. Folder Luar (Root & Backend Konfigurasi)

1. **`build.gradle.kts` (Level Module `app`)**
   - Mendeklarasikan daftar seluruh dependensi eksternal (Library seperti Retrofit, Coil, Hilt, Firebase SDK, JWT) yang dipakai oleh aplikasi.
2. **`AndroidManifest.xml` (di `app/src/main/`)**
   - Pusat pendaftaran informasi sistem Android. Mencakup penentuan *icon*, nama aplikasi, serta perizinan (*Permissions* seperti internet).
3. **`firestore.rules` (di Root Project)**
   - Aturan Keamanan Database server (Firestore). Mengunci data dengan aturan: *user* hanya boleh melihat miliknya sendiri, *admin* memiliki kontrol penuh, *unauthorized user* dilarang melihat data apapun.
4. **`docs/` (Dokumentasi Project)**
   - Tempat seluruh dokumentasi teknis ini berada.
5. **`cloudinary-backend-worker/` (Serverless Backend)**
   - Proyek backend mini (*Cloudflare Workers*) menggunakan Node.js/TypeScript. Digunakan murni sebagai jembatan *Security* untuk memverifikasi Token *Firebase JWT* yang dikirimkan oleh Android saat ingin menghapus bukti foto di Cloudinary tanpa harus membocorkan *Secret Key* ke HP pengguna.