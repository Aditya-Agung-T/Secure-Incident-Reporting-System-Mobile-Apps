# Pemetaan Fitur, File, dan Baris Kode (Code Location) SIRS

Dokumen ini adalah referensi cepat (Cheat Sheet) yang sangat akurat untuk mempermudah pencarian fungsi/fitur tertentu di dalam source code Android (Kotlin) pada saat pengembangan atau presentasi sidang.

Semua baris (line) yang dicantumkan di sini adalah fungsi utama (entry point) dari setiap fitur.

---

## 1. Autentikasi (Login & Register)

| Fitur | Lokasi File | Baris (Line) | Deskripsi / Alur Singkat |
|---|---|---|---|
| **Form Login (UI)** | `presentation/auth/LoginScreen.kt` | `Line 29` | UI untuk input Email dan Password. |
| **Logic Login (ViewModel)** | `presentation/auth/AuthViewModel.kt` | `Line 67` | `fun login()` -> Memvalidasi input dan memanggil UseCase/Repository. |
| **Login Firebase (Data)** | `data/repository/AuthRepositoryImpl.kt` | `Line 30` | `override suspend fun login()` -> Berkomunikasi dengan FirebaseAuth untuk verifikasi kredensial. |
| **Form Register (UI)** | `presentation/auth/RegisterScreen.kt` | `Line 31` | UI untuk membuat akun baru. |
| **Logic Register (ViewModel)** | `presentation/auth/AuthViewModel.kt` | `Line 85` | `fun register()` -> Memvalidasi email, kekuatan password, dan kesamaan password. |
| **Daftar ke Firestore** | `data/repository/AuthRepositoryImpl.kt` | `Line 54` | `override suspend fun register()` -> Membuat akun Auth, lalu menyimpan data profil ke koleksi `users` di Firestore. |

---

## 2. Pelaporan Insiden (Membuat & Mengedit)

| Fitur | Lokasi File | Baris (Line) | Deskripsi / Alur Singkat |
|---|---|---|---|
| **Form Buat Laporan (UI)** | `presentation/reports/CreateReportScreen.kt` | `Line 35` | Tampilan form (judul, deskripsi, lokasi, lampiran, dll). |
| **Proses Submit Laporan** | `presentation/reports/ReportViewModel.kt` | `Line 217` | `fun submitReport()` -> Mengecek Rate Limit (Anti-Spam), lalu memanggil Repository. |
| **Simpan ke Firestore** | `data/remote/FirestoreReportDataSource.kt` | `Line 48` | `fun createReport()` -> Menyimpan data laporan (termasuk status pending dan pembuatan kode seri unik). |
| **Pilih / Validasi Lampiran** | `core/security/FileValidation.kt` | `Line 28` | `fun validateFile()` -> Validasi ukuran, MIME type, dan pengecekan Magic Bytes. |
| **Upload Lampiran** | `data/remote/CloudinaryUploadDataSource.kt` | `Line 41` | `fun uploadFile()` -> Meminta *Signature* JWT ke Worker, lalu mengupload file ke Cloudinary. |
| **Edit Laporan (ViewModel)** | `presentation/reports/ReportViewModel.kt` | `Line 340` | `fun updateReport()` -> Memperbarui dokumen Firestore jika status laporan masih "Pending". |

---

## 3. Dashboard dan Daftar Laporan

| Fitur | Lokasi File | Baris (Line) | Deskripsi / Alur Singkat |
|---|---|---|---|
| **Dashboard Pengguna** | `presentation/dashboard/UserDashboardScreen.kt` | `Line 36` | Menampilkan statistik jumlah dan status laporan milik user sendiri. |
| **Kalkulasi Statistik (User)** | `presentation/dashboard/DashboardViewModel.kt` | `Line 85` | `fun loadUserReports()` -> Menarik laporan dari Firestore lalu menjumlahkannya ke dalam data class DashboardStats. |
| **Dashboard Admin** | `presentation/dashboard/AdminDashboardScreen.kt` | `Line 37` | Menampilkan seluruh rekapitulasi data keamanan siber. |
| **Kalkulasi Statistik (Admin)**| `presentation/dashboard/DashboardViewModel.kt` | `Line 100` | `fun loadAdminReports()` -> Menarik semua dokumen laporan tanpa filter UID. |
| **Daftar Laporan (User)** | `presentation/reports/ReportListScreen.kt` | `Line 29` | Menampilkan LazyColumn dari riwayat laporan pengguna beserta filternya. |
| **Load Daftar Laporan** | `presentation/reports/ReportViewModel.kt` | `Line 124` | `fun loadUserReports()` -> Menjalankan fungsi observer Firestore secara real-time. |

---

## 4. Keamanan dan Tindakan Khusus

| Fitur | Lokasi File | Baris (Line) | Deskripsi / Alur Singkat |
|---|---|---|---|
| **Menghitung Sisa Waktu (SLA)**| `core/security/SlaCalculator.kt` | `Line 14` | `fun formatRemaining()` -> Menghitung selisih waktu sekarang dengan deadline sesuai dengan *Severity*. |
| **Cek Keterlambatan (SLA)** | `core/security/SlaCalculator.kt` | `Line 38` | `fun isOverdue()` -> Menentukan apakah laporan tersebut telah melanggar/melewati batas SLA. |
| **Menghapus Laporan (Logic)** | `presentation/reports/ReportViewModel.kt` | `Line 417` | `fun deleteReport()` -> Dieksekusi saat user membatalkan/menghapus laporan pending miliknya. |
| **Menghapus Lampiran** | `data/remote/CloudinaryUploadDataSource.kt` | `Line 96` | `fun deleteFile()` -> Membawa *Firebase ID Token* user untuk memanggil rute POST Cloudflare Worker. |
| **Rate Limiter (Anti-Spam)** | `core/common/RateLimiter.kt` | `Line 34` | `fun canCreateReport()` & `fun canUploadFile()` -> Membatasi frekuensi aksi per pengguna dalam jeda waktu tertentu. |

---

## 5. Manajemen Kategori dan Aktivitas (Admin)

| Fitur | Lokasi File | Baris (Line) | Deskripsi / Alur Singkat |
|---|---|---|---|
| **Kelola Kategori (UI)** | `presentation/admin/CategoryManagementScreen.kt` | `Line 32` | Layar untuk menambah, mengedit, atau menghapus kategori laporan. |
| **Lihat Aktivitas Sistem** | `presentation/admin/ActivityLogScreen.kt` | `Line 26` | Layar untuk membaca log audit/aktivitas dari `ActivityLogService`. |
| **Menulis Log (Service)** | `data/remote/ActivityLogDataSource.kt` | `Line 20` | `fun logActivity()` -> Mencatat rekam jejak setiap aksi krusial (seperti perubahan status laporan). |

---

### Tips Penggunaan Saat Presentasi
Jika penguji meminta Anda membuktikan bagian tertentu (contoh: *"Coba tunjukkan di mana kode yang memvalidasi Magic Bytes file?"*), Anda dapat membuka file ini, melihat pada **Bab 2 (Pilih / Validasi Lampiran)**, dan Anda tahu pasti harus membuka **`FileValidation.kt` baris ke-28**.