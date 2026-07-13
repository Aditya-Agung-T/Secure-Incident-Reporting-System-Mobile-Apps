# Lampiran 2.4 – Rancangan Antarmuka Pengguna (UI/UX)

Dokumen ini menyajikan rancangan antarmuka pengguna (wireframe) untuk setiap
layar (screen) utama aplikasi **SIRS (Secure Incident Reporting System)**.
Wireframe digambarkan secara ASCII agar mudah direproduksi di laporan, dan
setiap layar dilengkapi dengan:

- **Nama / judul layar**
- **Penjelasan singkat fungsi layar**
- **Komponen UI yang digunakan** (TextField, Button, LazyColumn, dll.)

Semua layar dibangun dengan Jetpack Compose (Material 3) dan diambil langsung
dari source code di `app/src/main/java/com/adit/sirs/presentation/`.

---

## 1. Login Screen
**File:** `presentation/auth/LoginScreen.kt`

**Fungsi:** Layar awal autentikasi. Pengguna memasukkan email & password,
lalu diarahkan ke dashboard sesuai peran (user biasa atau admin) melalui
callback `onLoginSuccess(isAdmin)`.

**Komponen UI:**
- `Text` (judul brand "SIRS" + subtitle)
- `SirsTextField` (Email, KeyboardType.Email)
- `SirsTextField` (Password, PasswordVisualTransformation, trailingIcon
  Visibility/VisibilityOff toggle)
- `TextButton` ("Lupa Password?")
- `Text` error (jika `UiState.Error`)
- `Button` "Masuk" (disabled saat Loading / field kosong, menampilkan
  `CircularProgressIndicator` saat loading)
- `TextButton` ("Belum punya akun? Daftar")

```
┌──────────────────────────────────┐
│                                  │
│           SIRS                   │
│  Secure Incident Reporting System │
│                                  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Email                       │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Password                  👁 │  │
│  └────────────────────────────┘  │
│                         Lupa Password?│
│                                  │
│  ┌────────────────────────────┐  │
│  │         Masuk              │  │
│  └────────────────────────────┘  │
│                                  │
│      Belum punya akun? Daftar    │
└──────────────────────────────────┘
```

---

## 2. Register Screen
**File:** `presentation/auth/RegisterScreen.kt`

**Fungsi:** Pendaftaran akun user baru. Memiliki validasi password sesuai
standar OWASP/NIST (≥12 karakter, huruf besar/kecil, angka, simbol) yang
ditampilkan sebagai checklist live, serta konfirmasi password.

**Komponen UI:**
- `TopAppBar` (title "Daftar Akun", navigationIcon ArrowBack)
- `SirsTextField` (Nama Lengkap)
- `SirsTextField` (Email)
- `SirsTextField` (Password + toggle visibility + `Text` error)
- `Text` checklist aturan password (✓ / •, warna dinamis)
- `SirsTextField` (Konfirmasi Password, error jika tidak sama)
- `Text` error (jika `UiState.Error`)
- `Button` "Daftar" (enabled bila semua valid + cocok)

```
┌──────────────────────────────────┐
│ ← Daftar Akun                     │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Nama Lengkap               │  │
│  └────────────────────────────┘  │
│  ┌────────────────────────────┐  │
│  │ Email                      │  │
│  └────────────────────────────┘  │
│  ┌────────────────────────────┐  │
│  │ Password                 👁 │  │
│  └────────────────────────────┘  │
│  Standar password OWASP/NIST:    │
│  ✓ Minimal 12 karakter           │
│  ✓ Memiliki huruf besar          │
│  • Memiliki karakter khusus      │
│  ┌────────────────────────────┐  │
│  │ Konfirmasi Password        │  │
│  └────────────────────────────┘  │
│  ┌────────────────────────────┐  │
│  │          Daftar            │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## 3. Forgot Password Screen
**File:** `presentation/auth/ForgotPasswordScreen.kt`

**Fungsi:** Mengirim tautan reset password ke email pengguna via Firebase
Auth (`sendPasswordResetEmail`). Menampilkan status sukses/gagal.

**Komponen UI:**
- `TopAppBar` (title "Reset Password", ArrowBack)
- `Text` penjelasan
- `SirsTextField` (Email)
- `Text` sukses ("Email reset terkirim...") / error
- `Button` "Kirim Email Reset" (disabled saat loading, `CircularProgressIndicator`)

```
┌──────────────────────────────────┐
│ ← Reset Password                  │
│                                  │
│  Masukkan email untuk menerima    │
│  tautan reset password.           │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Email                      │  │
│  └────────────────────────────┘  │
│  ✓ Email reset terkirim.         │
│    Periksa kotak masuk Anda.      │
│  ┌────────────────────────────┐  │
│  │      Kirim Email Reset     │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## 4. User Dashboard Screen
**File:** `presentation/dashboard/UserDashboardScreen.kt`

**Fungsi:** Beranda untuk peran **user**. Menampilkan sapaan, ringkasan
statistik laporan (total/pending/investigasi/selesai/ditolak), aksi cepat,
dan daftar 5 laporan terbaru milik user.

**Komponen UI:**
- `Scaffold` + `TopAppBar` (title "Dashboard User", IconButton Person, Logout)
- `SmallFloatingActionButton` (tambah laporan, muncul bila ada laporan)
- `LazyColumn`
  - `Text` sapaan ("Selamat datang, {nama}")
  - `OutlinedCard` ringkasan Total Laporan
  - `Row` + `StatCard` (Pending, Investigasi, Selesai, Ditolak)
  - `ActionCard` (Buat Laporan, Laporan Saya)
  - `TextButton` "Lihat Semua"
  - `ReportCardItem` (5 terbaru) / `EmptyState` / `LoadingView` / `ErrorView`

```
┌──────────────────────────────────┐
│ Dashboard User                👤 🚪│
│ ──────────────────────────────── │
│ Selamat datang,                  │
│ Budi                             │
│ ┌──────────────────────────────┐ │
│ │ Total Laporan      3         │ │
│ └──────────────────────────────┘ │
│ ┌──────────┐  ┌───────────────┐  │
│ │2 Pending │  │1 Investigasi   │  │
│ └──────────┘  └───────────────┘  │
│ ┌──────────┐  ┌───────────────┐  │
│ │0 Selesai │  │0 Ditolak       │  │
│ └──────────┘  └───────────────┘  │
│ Aksi Cepat                       │
│ ┌──────────┐  ┌───────────────┐  │
│ │➕ Buat    │  │📄 Laporan Saya│  │
│ └──────────┘  └───────────────┘  │
│ Laporan Terbaru        Lihat Semua│
│ ┌──────────────────────────────┐ │
│ │ #INC-001  Phishing  ⚠ High   │ │
│ └──────────────────────────────┘ │
│                 ➕                │
└──────────────────────────────────┘
```

---

## 5. Admin Dashboard Screen
**File:** `presentation/dashboard/AdminDashboardScreen.kt`

**Fungsi:** Beranda untuk peran **admin**. Sama seperti User Dashboard,
ditambah kartu peringatan "Perlu Segera Ditangani" (jika ada laporan
Critical/High yang belum selesai) dan aksi cepat ke Kategori & Log Aktivitas.

**Komponen UI:**
- `Scaffold` + `TopAppBar` (title "Dashboard Admin", Person, Logout)
- `LazyColumn`
  - `StatCard` (sama seperti user)
  - `Card` peringatan urgent (`SeverityCritical` accent) bila
    `criticalUnresolved`/`highUnresolved` > 0
  - `ActionCard` (Laporan, Kategori, Log Aktivitas)
  - `ReportCardItem` dengan `showReporter = true` (5 terbaru)

```
┌──────────────────────────────────┐
│ Dashboard Admin               👤 🚪│
│ Selamat datang,                  │
│ Administrator                    │
│ ┌──────────────────────────────┐ │
│ │ Total Laporan      12        │ │
│ └──────────────────────────────┘ │
│ ┌──── Perlu Segera Ditangani ──┐ │
│ │⚠ 1 Critical belum selesai    │ │
│ └──────────────────────────────┘ │
│ Aksi Cepat                       │
│ ┌─────┐ ┌────────┐ ┌─────────┐   │
│ │📄   │ │🏷      │ │🕑       │   │
│ │Lapor│ │Kategori│ │Log Akt. │   │
│ └─────┘ └────────┘ └─────────┘   │
│ Laporan Terbaru        Lihat Semua│
│ ┌──────────────────────────────┐ │
│ │ #INC-009  DDoS   👤 andi     │ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

---

## 6. Report List Screen (Laporan Saya)
**File:** `presentation/reports/ReportListScreen.kt`

**Fungsi:** Daftar semua laporan milik user, dengan filter berdasarkan
status dan severity (FilterChip horizontal-scroll).

**Komponen UI:**
- `TopAppBar` (title "Laporan Saya", ArrowBack)
- `SmallFloatingActionButton` (buat laporan)
- `Row` + `FilterChip` (Semua Status + tiap `IncidentStatus`)
- `Row` + `FilterChip` (Semua Severity + tiap `Severity`)
- `HorizontalDivider`
- `LazyColumn` `ReportCardItem` (showReporter=false) / `EmptyState`

```
┌──────────────────────────────────┐
│ ← Laporan Saya                   │
│ ──────────────────────────────── │
│ [Semua Status][Pending][Invest..]│
│ [Semua Sev][Critical][High][Med] │
│ ──────────────────────────────── │
│ 3 laporan ditemukan              │
│ ┌──────────────────────────────┐ │
│ │ #INC-001 Phishing  ⚠ High    │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ #INC-002 Malware   ✔ Selesai │ │
│ └──────────────────────────────┘ │
│                 ➕                │
└──────────────────────────────────┘
```

---

## 7. Report Detail Screen (User)
**File:** `presentation/reports/ReportDetailScreen.kt`

**Fungsi:** Menampilkan detail satu laporan untuk user: kode, status,
severity, kategori, info laporan, SLA penanganan (countdown live), panduan
kategori, deskripsi, lampiran, respon admin, dan riwayat status. Tombol edit/
hapus muncul bila status masih Pending.

**Komponen UI:**
- `TopAppBar` (title "Detail Laporan", ArrowBack, Edit & Delete bila pending)
- `LazyColumn` dengan `Card`/`OutlinedCard`:
  - Header (reportCode + `StatusBadge`, title, `SeverityBadge`, InfoChip)
  - Info Laporan (`DetailRow`)
  - SLA Penanganan (`SlaCalculator`, `InfoChip`)
  - `CategoryGuidanceCard` (mitigasi & bukti)
  - Deskripsi Insiden
  - `AttachmentCard` (buka file)
  - Admin Response card
  - Status History (`StatusHistoryItem`)
- `AlertDialog` konfirmasi hapus

```
┌──────────────────────────────────┐
│ ← Detail Laporan           ✎ 🗑  │
│ ┌──────────────────────────────┐ │
│ │ #INC-001        [Pending]    │ │
│ │ Phishing Email               │ │
│ │ ⚠ High   🏷 Email Phishing   │ │
│ └──────────────────────────────┘ │
│ Informasi Laporan                │
│ Lokasi      : ...                │
│ Tgl Insiden : 12 Jul 2026        │
│ ┌──────────────────────────────┐ │
│ │ SLA Penanganant [DALAM BATAS]│ │
│ │ 23j 12m lagi                 │ │
│ │ Deadline: 13 Jul 2026        │ │
│ └──────────────────────────────┘ │
│ Deskripsi Insiden                │
│ "Telah menerima email ..."       │
│ 📎 bukti.png            [Buka]   │
│ Riwayat Status                  │
│ • Dilaporkan - 12 Jul 10:00     │
└──────────────────────────────────┘
```

---

## 8. Create Report Screen
**File:** `presentation/reports/CreateReportScreen.kt`

**Fungsi:** Form pembuatan laporan insiden baru. Field: judul, kategori
(dropdown), deskripsi, lokasi, tanggal insiden (DatePicker), severity
(dropdown), lampiran opsional (upload Cloudinary), lalu submit.

**Komponen UI:**
- `TopAppBar` (title "Buat Laporan", ArrowBack)
- `SirsTextField` (Judul, Deskripsi, Lokasi)
- `ExposedDropdownMenuBox` (Kategori, Severity)
- `SirsTextField` readOnly + `DatePickerDialog` (Tanggal Insiden)
- `CategoryGuidanceCard` (panduan kategori terpilih)
- `Card` lampiran (`OutlinedButton` picker / `Row` nama+ukuran+`IconButton` hapus)
- `Button` "Kirim Laporan" (disabled saat loading, indikator "Mengunggah..."/"Menyimpan...")

```
┌──────────────────────────────────┐
│ ← Buat Laporan                   │
│ ┌────────────────────────────┐  │
│ │ Judul *                    │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Kategori *               ▼ │  │
│ └────────────────────────────┘  │
│ Panduan tindakan awal:          │
│ • Jangan klik tautan mencurigakan│
│ 📎 Bukti yang disarankan        │
│ ┌────────────────────────────┐  │
│ │ Deskripsi *                │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Lokasi *                   │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Tanggal Insiden *       📅 │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Severity *               ▼ │  │
│ └────────────────────────────┘  │
│ Lampiran (opsional)  JPG/PNG/PDF │
│ [ 📎 Pilih File ]               │
│ ┌────────────────────────────┐  │
│ │       Kirim Laporan        │  │
│ └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## 9. Edit Report Screen
**File:** `presentation/reports/EditReportScreen.kt`

**Fungsi:** Form edit laporan (hanya untuk status Pending). Field identik
dengan Create, namun nilai awal di-prefill dari `loadReportDetail` dan
tombol menyimpah memanggil `updateReport`.

**Komponen UI:** Sama dengan Create Report Screen, kecuali:
- `TopAppBar` title "Edit Laporan"
- `Button` label "Simpan Perubahan"
- Data di-prefill otomatis via `LaunchedEffect(reportDetail)`

```
┌──────────────────────────────────┐
│ ← Edit Laporan                   │
│ ┌────────────────────────────┐  │
│ │ Judul *  (terisi otomatis) │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Kategori *              ▼  │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Deskripsi *                │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Lokasi *                   │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Tanggal Insiden *      📅  │  │
│ └────────────────────────────┘  │
│ ┌────────────────────────────┐  │
│ │ Severity *              ▼  │  │
│ └────────────────────────────┘  │
│ [ 📎 Pilih File / nama.pdf ]    │
│ ┌────────────────────────────┐  │
│ │     Simpan Perubahan      │  │
│ └────────────────────────────┘  │
└──────────────────────────────────┘
```

---

## 10. Admin Report List Screen (Semua Laporan)
**File:** `presentation/admin/AdminReportListScreen.kt`

**Fungsi:** Daftar seluruh laporan untuk admin, dengan pencarian (judul/kode/
pelapor/lokasi), filter status, severity, dan rentang tanggal. Admin juga
bisa membuat laporan baru dari sini.

**Komponen UI:**
- `TopAppBar` (title "Semua Laporan", ArrowBack)
- `FloatingActionButton` (tambah laporan)
- `OutlinedTextField` search (leading Search, trailing Clear)
- `Row` `FilterChip` status & severity (horizontal scroll)
- `Row` `OutlinedButton` rentang tanggal (DatePickerDialog) + Clear
- `LazyColumn` `ReportCardItem` (showReporter=true)

```
┌──────────────────────────────────┐
│ ← Semua Laporan                  │
│ 🔍 Cari judul, kode, pelapor...  │
│ [Semua Status][Pending][Invest..]│
│ [Semua Sev][Critical][High][Med] │
│ [📅 Tgl Awal] [📅 Tgl Akhir]  ✕  │
│ ──────────────────────────────── │
│ 12 laporan ditemukan             │
│ ┌──────────────────────────────┐ │
│ │ #INC-009 DDoS  👤 andi ⚠ High│ │
│ └──────────────────────────────┘ │
│                 ➕                │
└──────────────────────────────────┘
```

---

## 11. Admin Report Detail Screen
**File:** `presentation/admin/AdminReportDetailScreen.kt`

**Fungsi:** Detail laporan untuk admin dengan kontrol penuh: info pelapor,
detail insiden, SLA, deskripsi, lampiran, respon, riwayat status, serta tombol
edit status/severity (bottom sheet) dan hapus (dialog).

**Komponen UI:**
- `TopAppBar` (title "Detail Laporan", ArrowBack, Edit, Delete)
- `LazyColumn` kartu: header, Info Pelapor (`DetailRow`), Detail Insiden,
  Target Penanganan (SLA), Deskripsi, `AttachmentCard`, Respon Admin,
  Riwayat Aktivitas
- `UpdateStatusSheet` (bottom sheet update status/severity/respon)
- `AlertDialog` konfirmasi hapus

```
┌──────────────────────────────────┐
│ ← Detail Laporan             ✎ 🗑│
│ ┌──────────────────────────────┐ │
│ │ #INC-009       [Investigasi] │ │
│ │ Serangan DDoS  ⚠ High       │ │
│ └──────────────────────────────┘ │
│ Informasi Pelapor                │
│ Nama Lengkap : Andi              │
│ Alamat Email : andi@x.com        │
│ Detail Insiden                  │
│ Lokasi/IP/URL : 10.0.0.5         │
│ Waktu Kejadian: 11 Jul 2026      │
│ Target Penanganan (SLA) [TERLAMBAT]│
│ 02j 10m telat  Batas: 12 Jul    │
│ Deskripsi Insiden               │
│ "Traffic melonjak ..."          │
│ Riwayat Aktivitas               │
│ • Dilaporkan - 11 Jul 09:00     │
│ • Investigasi - 11 Jul 11:00    │
└──────────────────────────────────┘
        (bottom sheet update status)
```

---

## 12. Category Management Screen
**File:** `presentation/admin/CategoryManagementScreen.kt`

**Fungsi:** Kelola kategori insiden (tambah/edit/hapus/aktifkan). Kategori
berisi nama, deskripsi, panduan tindakan awal, dan bukti yang disarankan.

**Komponen UI:**
- `TopAppBar` (title "Kelola Kategori", ArrowBack)
- `FloatingActionButton` (tambah)
- `LazyColumn` `CategoryCard` (nama, deskripsi, jumlah panduan/bukti,
  status Aktif/Nonaktif, tombol Edit/Nonaktifkan/Hapus)
- `AlertDialog` form (SirsTextField: Nama, Deskripsi, Panduan, Bukti)
- `AlertDialog` konfirmasi hapus

```
┌──────────────────────────────────┐
│ ← Kelola Kategori                │
│ ┌──────────────────────────────┐ │
│ │ Phishing Email                │ │
│ │ 2 panduan · 1 bukti  [Aktif] │ │
│ │ [Edit][Nonaktifkan][Hapus]   │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ Malware                       │ │
│ │ 1 panduan · 2 bukti [Aktif]  │ │
│ │ [Edit][Nonaktifkan][Hapus]   │ │
│ └──────────────────────────────┘ │
│                 ➕                │
└──────────────────────────────────┘
   Dialog "Buat Kategori":
   Nama | Deskripsi | Panduan (1 baris/item)
   Bukti disarankan (1 baris/item) [Simpan][Batal]
```

---

## 13. Activity Log Screen
**File:** `presentation/admin/ActivityLogScreen.kt`

**Fungsi:** Menampilkan log aktivitas sistem/admin (siapa melakukan apa,
kapan).

**Komponen UI:**
- `TopAppBar` (title "Log Aktivitas", ArrowBack)
- `LazyColumn` `ActivityLogCard` (action, description, "Oleh: ...", timestamp)
- `EmptyState` bila kosong

```
┌──────────────────────────────────┐
│ ← Log Aktivitas                  │
│ ┌──────────────────────────────┐ │
│ │ Update Status                │ │
│ │ #INC-009 → Investigasi       │ │
│ │ Oleh: Admin  · 11 Jul 11:00  │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ Buat Kategori                │ │
│ │ Phishing Email ditambahkan   │ │
│ │ Oleh: Admin  · 10 Jul 09:30  │ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

---

## 14. Profile Screen
**File:** `presentation/profile/ProfileScreen.kt`

**Fungsi:** Menampilkan profil pengguna: nama, email, peran (role chip),
status akun, tanggal bergabung, dan login terakhir.

**Komponen UI:**
- `TopAppBar` (title "Profile", ArrowBack)
- `Icon` Person (80dp)
- `Text` nama, email
- `AssistChip` role (User/Admin)
- `Card` `ProfileRow` (Account Status, Member Since, Last Login)

```
┌──────────────────────────────────┐
│ ← Profile                        │
│                                  │
│             👤                   │
│           Budi Santoso           │
│        budi@example.com          │
│           [ User ]               │
│ ┌──────────────────────────────┐ │
│ │ Account Status   Active      │ │
│ │ Member Since     10 Jul 2026 │ │
│ │ Last Login       13 Jul 08:20│ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

---

## Ringkasan Komponen UI yang Digunakan

| Komponen Material 3 | Digunakan pada |
|---|---|
| `Scaffold` / `TopAppBar` | Semua layar |
| `Text` / `Icon` / `IconButton` | Global |
| `SirsTextField` (custom) | Login, Register, Forgot, Create, Edit, Category |
| `Button` / `OutlinedButton` / `TextButton` | Global (aksi utama) |
| `FloatingActionButton` / `SmallFloatingActionButton` | Dashboard, List, Admin List, Category |
| `LazyColumn` / `items` | Dashboard, List, Detail, Category, Log |
| `Card` / `OutlinedCard` | Statistik, kartu laporan, detail |
| `FilterChip` | List laporan (filter status/severity) |
| `ExposedDropdownMenuBox` | Create/Edit (kategori, severity) |
| `AlertDialog` | Hapus laporan, form kategori |
| `DatePickerDialog` | Create/Edit, filter tanggal admin |
| `AssistChip` | Profile (role) |
| `CircularProgressIndicator` | Loading pada tombol submit/login |
| `HorizontalDivider` | Pembatas list/filter |

---

*Dokumen ini dihasilkan dari inspect langsung ke source code Compose
(`presentation/...`) agar wireframe sesuai implementasi nyata di APK.*
