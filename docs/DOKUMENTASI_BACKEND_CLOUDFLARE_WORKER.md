# Dokumentasi Backend: Cloudflare Worker untuk Autentikasi Upload & Delete Asset Cloudinary

Dokumen ini menjelaskan arsitektur backend SIRS untuk operasi keamanan file/gambar di Cloudinary. Backend diimplementasikan sebagai Cloudflare Worker yang memverifikasi identitas pengguna melalui Firebase ID Token (JWT) sebelum menerbitkan izin unggah (Upload Signature) maupun menghapus asset.

## 1. Arsitektur Umum

```
┌──────────────────────┐
│   Android App SIRS   │
│                      │
│  User klik Submit    │
│  atau Hapus Laporan  │
└──────────┬───────────┘
           │
           │  1. Ambil Firebase ID Token dari user yang login
           │  2. Kirim Request ke Cloudflare Worker:
           │     Header: Authorization: Bearer <Fireb...ken>
           │     - GET  /generate-signature (Untuk Upload)
           │     - POST / (Untuk Hapus) dengan body publicId
           │
           ▼
┌──────────────────────────────────────────────────────┐
│          Cloudflare Worker (Backend)                  │
│          sirs-cloudinary-delete                       │
│          https://sirs-cloudinary-delete               │
│              .adityatriwibowo19.workers.dev           │
│                                                      │
│  3. Ambil token dari header Authorization            │
│  4. Verifikasi JWT ke Google (JWKS public key)       │
│     - issuer: securetoken.google.com/androapp-f262d  │
│     - audience: androapp-f262d                       │
│  5. Jika token TIDAK valid -> 401 Unauthorized       │
│  6. Jika token valid:                                │
│     - Jika GET /generate-signature:                  │
│       Generate SHA-1 signature upload, lalu return   │
│       signature tersebut ke Android.                 │
│     - Jika POST / (Hapus):                           │
│       Baca publicId, generate SHA-1 signature hapus, │
│       dan kirim request destroy ke Cloudinary API.   │
│                                                      │
│  Environment Variables (non-secret):                 │
│    FIREBASE_PROJECT_ID = androapp-f262d               │
│    CLOUDINARY_CLOUD_NAME = da26s6yet                 │
│    CLOUDINARY_API_KEY = fsLUpWrPWiDAgeYibRX8E8_xZVs  │
│                                                      │
│  Secret (tersimpan aman di Cloudflare):               │
│    CLOUDINARY_API_SECRET = *** (tidak pernah terekspos)│
└──────────┬───────────────────────────────────────────┘
           │
           │  7. (A) Android mengupload file ke Cloudinary
           │     menggunakan Signature dari Worker. ATAU:
           │  7. (B) Worker mengirim request destroy 
           │     langsung ke Cloudinary.
           │
           ▼
┌──────────────────────┐
│   Cloudinary CDN     │
│                      │
│  8. Terima/Hapus File│
│  9. Response: ok/not │
└──────────────────────┘
```

## 2. Mengapa Arsitektur Ini Aman

### Sebelumnya (TIDAK AMAN)

Android menyimpan `CLOUDINARY_DELETE_SECRET` di `BuildConfig`. Siapa pun yang decompile APK bisa mendapatkan secret tersebut dan menghapus file Cloudinary sesuka hati tanpa login.

### Sekarang (AMAN)

| Aspek | Penjelasan |
|---|---|
| Tidak ada secret di APK | Android hanya menyimpan URL endpoint Worker. Tidak ada API secret Cloudinary di dalam APK. |
| Autentikasi wajib | Setiap request ke Worker harus menyertakan Firebase ID Token yang valid. Token ini hanya bisa didapat oleh user yang benar-benar login di aplikasi SIRS melalui Firebase Auth. |
| Verifikasi JWT di server | Worker memverifikasi token menggunakan public key resmi Google (JWKS). Token palsu/expired/dari project lain otomatis ditolak. |
| Secret tersimpan di server | `CLOUDINARY_API_SECRET` hanya tersimpan di environment Cloudflare Workers (via `wrangler secret`). Tidak pernah dikirim ke Android, tidak ada di source code, tidak ada di file konfigurasi yang bisa dibaca. |
| Signature digenerate di server | SHA-1 signature untuk Cloudinary API digenerate di Worker menggunakan secret yang hanya ada di server. Android tidak pernah tahu signature ini. |

## 3. Struktur File dan Folder

### 3.1 Sisi Backend (Cloudflare Worker)

```
cloudinary-delete-worker/
└── sirs-cloudinary-delete/
    ├── src/
    │   └── index.ts              <- Kode utama Worker
    ├── test/                     <- Folder test (vitest)
    ├── node_modules/             <- Dependencies (tidak di-commit)
    ├── package.json              <- Dependencies dan scripts npm
    ├── package-lock.json         <- Lock file npm
    ├── wrangler.jsonc            <- Konfigurasi deploy Cloudflare
    ├── tsconfig.json             <- Konfigurasi TypeScript
    ├── vitest.config.mts         <- Konfigurasi test runner
    ├── worker-configuration.d.ts <- Type definitions auto-generated
    └── AGENTS.md                 <- Panduan development Worker
```

Detail setiap file:

| File | Fungsi |
|---|---|
| `src/index.ts` | Kode utama Worker. Berisi handler HTTP, verifikasi JWT Firebase, generate signature Cloudinary, dan request delete ke Cloudinary API. |
| `wrangler.jsonc` | Konfigurasi Cloudflare Worker: nama worker, compatibility date, environment variables (FIREBASE_PROJECT_ID, CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY). |
| `package.json` | Dependency project. Runtime: `jose` (JWT verification). Dev: `wrangler`, `vitest`, `typescript`. |
| `tsconfig.json` | Target ES2024, module ES2022, strict mode, bundler module resolution. |

### 3.2 Sisi Android (Client)

File-file Android yang terhubung ke backend ini:

| File | Lokasi | Fungsi |
|---|---|---|
| `CloudinaryUploadDataSource.kt` | `app/src/main/java/com/adit/sirs/data/remote/` | Mengirim request delete ke Worker dengan Firebase ID Token di header `Authorization`. |
| `ReportRepositoryImpl.kt` | `app/src/main/java/com/adit/sirs/data/repository/` | Memanggil `CloudinaryUploadDataSource.deleteFile()` saat user/admin menghapus laporan. |
| `FirebaseProvider.kt` | `app/src/main/java/com/adit/sirs/core/firebase/` | Menyediakan instance `FirebaseAuth` yang diinjeksi ke `CloudinaryUploadDataSource`. |
| `RepositoryModule.kt` | `app/src/main/java/com/adit/sirs/data/di/` | Hilt module yang mengikat repository interface ke implementation. |
| `build.gradle.kts` | `app/` | Mendefinisikan `BuildConfig.CLOUDINARY_DELETE_ENDPOINT` (URL Worker). |
| `gradle.properties` | Root project | Menyimpan nilai `CLOUDINARY_DELETE_ENDPOINT` (URL Worker). |

### 3.3 Sisi Firebase

| File/Service | Lokasi | Fungsi |
|---|---|---|
| `google-services.json` | `app/` | Konfigurasi Firebase project (project id: `androapp-f262d`). |
| `firestore.rules` | Root project | Security rules Firestore yang mengizinkan delete laporan oleh admin atau owner (status pending). |
| Firebase Auth (cloud) | Google Cloud | Menerbitkan ID Token (JWT) saat user login. Token inilah yang diverifikasi oleh Worker. |
| Google JWKS endpoint | `googleapis.com` | Public key yang dipakai Worker untuk memverifikasi signature JWT. |

## 4. Alur Lengkap Hapus Laporan (End-to-End)

### Langkah 1: User Menekan Tombol Hapus di Aplikasi

File terkait:
- `ReportDetailScreen.kt` atau `AdminReportDetailScreen.kt` (tombol hapus di UI)
- `ReportViewModel.kt` atau `AdminViewModel.kt` (memanggil repository)

### Langkah 2: Repository Mengambil Data Laporan dari Firestore

File terkait:
- `ReportRepositoryImpl.kt:78-95`

```
ReportRepositoryImpl.deleteReport(reportId)
  -> reportDataSource.getReport(reportId)     // ambil data laporan
  -> attachment = report.attachment            // ambil info attachment
```

### Langkah 3: Jika Ada Attachment, Hapus File dari Cloudinary via Worker

File terkait:
- `ReportRepositoryImpl.kt:83-88`
- `CloudinaryUploadDataSource.kt:96-129`

```
cloudinaryDataSource.deleteFile(publicId, resourceType)
  -> auth.currentUser.getIdToken(false)        // ambil Firebase JWT
  -> OkHttp POST ke endpoint Worker
     Header: Authorization: Bearer <JWT>
     Body: { "publicId": "...", "resourceType": "image" }
```

### Langkah 4: Cloudflare Worker Menerima Request

File terkait:
- `cloudinary-delete-worker/sirs-cloudinary-delete/src/index.ts:29-49`

```
Worker menerima POST request
  -> Baca header Authorization
  -> Ekstrak token dari "Bearer <token>"
  -> jwtVerify(token, JWKS, { issuer, audience })
  -> Jika gagal: return 401 Unauthorized
  -> Jika berhasil: lanjut ke langkah 5
```

### Langkah 5: Worker Generate Signature dan Hapus di Cloudinary

File terkait:
- `cloudinary-delete-worker/sirs-cloudinary-delete/src/index.ts:51-87`

```
Worker membaca body request (publicId, resourceType)
  -> Generate timestamp
  -> Generate SHA-1 signature:
     sha1("public_id=<publicId>&timestamp=<timestamp><CLOUDINARY_API_SECRET>")
  -> POST ke https://api.cloudinary.com/v1_1/da26s6yet/{resourceType}/destroy
     Body: public_id, timestamp, api_key, signature
  -> Return response Cloudinary ke Android
```

### Langkah 6: Repository Menghapus Dokumen Laporan dari Firestore

File terkait:
- `ReportRepositoryImpl.kt:90`
- `FirestoreReportDataSource.kt` (fungsi deleteReport)
- `firestore.rules` (validasi hak akses delete)

```
reportDataSource.deleteReport(reportId)
  -> Firestore batch delete: hapus dokumen laporan + statusHistories
  -> Firestore rules memvalidasi:
     - Admin: boleh hapus semua laporan
     - User: boleh hapus laporan miliknya sendiri jika status masih "pending"
```

## 5. Environment Variables dan Secrets

### 5.1 Di Cloudflare Worker (Server-Side)

| Variable | Tipe | Lokasi konfigurasi | Nilai |
|---|---|---|---|
| `FIREBASE_PROJECT_ID` | Environment variable | `wrangler.jsonc:18` | `androapp-f262d` |
| `CLOUDINARY_CLOUD_NAME` | Environment variable | `wrangler.jsonc:19` | `da26s6yet` |
| `CLOUDINARY_API_KEY` | Environment variable | `wrangler.jsonc:20` | `929295481525158` |
| `CLOUDINARY_API_SECRET` | **Secret** (terenkripsi) | Cloudflare dashboard / `wrangler secret put` | Tidak pernah terekspos di file mana pun |

Cara menyimpan secret:

```bash
cd cloudinary-backend-worker/sirs-cloudinary
npx wrangler secret put CLOUDINARY_API_SECRET
# Paste nilai secret, lalu tekan Enter
```

### 5.2 Di Android (Client-Side)

| Variable | Lokasi | Nilai |
|---|---|---|
| `CLOUDINARY_DELETE_ENDPOINT` | `gradle.properties` -> `BuildConfig` | `https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev` |
| `CLOUDINARY_CLOUD_NAME` | `app/build.gradle.kts:23` -> `BuildConfig` | `da26s6yet` |
| `CLOUDINARY_UPLOAD_FOLDER` | `app/build.gradle.kts:25` -> `BuildConfig` | `sirs/incident-reports` |

Catatan: Tidak ada secret apa pun di sisi Android. Semua nilai di atas bersifat publik/non-sensitif.

## 6. Dependency Backend

| Package | Versi | Fungsi |
|---|---|---|
| `jose` | `^6.2.3` | Verifikasi JWT Firebase ID Token menggunakan JWKS (JSON Web Key Set) dari Google. |
| `wrangler` | `^4.107.0` | CLI tool untuk develop, test, dan deploy Cloudflare Worker. |
| `typescript` | `^5.5.2` | Compiler TypeScript. |
| `vitest` | `~3.2.0` | Test runner. |
| `@cloudflare/vitest-pool-workers` | `^0.12.4` | Pool workers untuk testing di environment Cloudflare. |

## 7. Perintah Development dan Deploy

| Perintah | Fungsi |
|---|---|
| `cd cloudinary-delete-worker/sirs-cloudinary-delete` | Masuk ke folder Worker. |
| `npm install` | Install semua dependency. |
| `npx wrangler dev` | Jalankan Worker secara lokal untuk development/testing. |
| `npx wrangler deploy` | Deploy Worker ke Cloudflare (production). |
| `npx wrangler secret put CLOUDINARY_API_SECRET` | Simpan secret Cloudinary API secara aman di Cloudflare. |
| `npx wrangler types` | Generate TypeScript type definitions dari wrangler config. |
| `npm test` | Jalankan unit test dengan vitest. |

## 8. HTTP API Specification

### Endpoint

```
GET https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev/generate-signature
POST https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev
```

### Request Headers

| Header | Nilai | Wajib |
|---|---|---|
| `Authorization` | `Bearer <Firebase_ID_Token>` | Ya |
| `Content-Type` | `application/json` | Ya |

### Request Body

```json
{
  "publicId": "sirs/incident-reports/abc123",
  "resourceType": "image"
}
```

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `publicId` | string | Ya | Public ID asset di Cloudinary yang akan dihapus. |
| `resourceType` | string | Tidak | Tipe resource Cloudinary: `image` (default) atau `raw` (untuk PDF). |

### Response (GET /generate-signature)
Sukses (200):
```json
{
  "signature": "1234abcd5678efgh",
  "timestamp": "1710000000",
  "api_key": "929295481525158",
  "folder": "incident-reports",
  "cloud_name": "da26s6yet"
}
```

### Response (POST / (Hapus))
Sukses (200):
```json
{
  "result": "ok"
}
```

Unauthorized (401):
```json
{
  "error": "Unauthorized: Invalid token (\"exp\" claim timestamp check failed)"
}
```

Bad Request (400):
```json
{
  "error": "Bad Request: publicId required"
}
```

Server Error (500):
```json
{
  "error": "pesan error internal"
}
```

## 9. Keamanan dan Catatan Penting

### Yang Sudah Diamankan

1. **Secret tidak ada di APK**: `CLOUDINARY_API_SECRET` hanya tersimpan di Cloudflare Workers environment (terenkripsi).
2. **Autentikasi JWT**: Setiap request wajib menyertakan Firebase ID Token yang valid.
3. **Verifikasi server-side**: Worker memverifikasi token menggunakan public key resmi Google, bukan secret statis.
4. **Signature server-side**: SHA-1 signature untuk Cloudinary API di-generate di server, bukan di client.
5. **CORS headers**: Worker menyertakan CORS headers untuk keamanan cross-origin.

### Potensi Peningkatan di Masa Depan

1. **Ownership check (IDOR prevention)**: Saat ini Worker hanya memverifikasi bahwa user adalah pengguna SIRS yang sah, tapi belum mengecek apakah file yang dihapus benar-benar milik user tersebut. Untuk level enterprise, Worker bisa mengecek ke Firestore apakah `publicId` terkait dengan laporan milik UID yang bersangkutan.
2. **Rate limiting**: Menambahkan pembatasan jumlah request delete per user per waktu di sisi Worker.
3. **Logging**: Menambahkan audit log di Worker untuk mencatat siapa menghapus file apa dan kapan.

## 10. Diagram Hubungan File

```
ANDROID APP
============
gradle.properties
  └─ CLOUDINARY_DELETE_ENDPOINT = https://sirs-cloudinary-delete...workers.dev
       │
app/build.gradle.kts
  └─ buildConfigField("CLOUDINARY_DELETE_ENDPOINT", ...)
       │
       ▼
CloudinaryUploadDataSource.kt
  ├─ Inject: FirebaseAuth (dari FirebaseProvider.kt via Hilt)
  ├─ uploadFile()  -> Minta signature via GET /generate-signature
  │                -> baru mengupload ke Cloudinary dengan Signature.
  └─ deleteFile()  -> ambil Firebase ID Token
                   -> POST ke Worker endpoint dengan Bearer token
       ▲
       │
ReportRepositoryImpl.kt
  └─ deleteReport() -> getReport() -> deleteFile() -> deleteReport(Firestore)
       ▲
       │
ReportViewModel.kt / AdminViewModel.kt
  └─ deleteReport() dipanggil dari UI
       ▲
       │
ReportDetailScreen.kt / AdminReportDetailScreen.kt
  └─ Tombol "Hapus Laporan"


CLOUDFLARE WORKER (BACKEND)
============================
wrangler.jsonc
  └─ vars: FIREBASE_PROJECT_ID, CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY
  └─ secret: CLOUDINARY_API_SECRET (via wrangler secret put)
       │
src/index.ts
  ├─ Import: jose (createRemoteJWKSet, jwtVerify)
  ├─ Verifikasi JWT dari Android -> Google JWKS
  ├─ Generate SHA-1 signature dengan CLOUDINARY_API_SECRET
  └─ POST destroy ke Cloudinary API
       │
       ▼
Cloudinary CDN
  └─ Hapus asset berdasarkan publicId


FIREBASE (CLOUD)
=================
Firebase Auth
  └─ Menerbitkan ID Token (JWT) saat user login
       │
Google JWKS Endpoint
  └─ https://www.googleapis.com/service_accounts/v1/jwk/
     securetoken@system.gserviceaccount.com
  └─ Public key untuk verifikasi JWT oleh Worker

Firestore Rules (firestore.rules)
  └─ Validasi hak delete dokumen laporan di database
```
