# Panduan Deploy Backend dan Firestore Rules SIRS

Dokumen ini menjelaskan cara melakukan deploy backend yang digunakan oleh project SIRS dan cara deploy ulang Firestore Security Rules. Isi dokumen disusun berdasarkan struktur project aktual, bukan template umum.

Backend yang digunakan project ini terdiri dari dua bagian:

1. **Cloudflare Worker** untuk keamanan Cloudinary:
   - menerbitkan *signature* upload melalui `GET /generate-signature`;
   - menghapus asset Cloudinary melalui `POST /`;
   - memverifikasi Firebase ID Token/JWT sebelum operasi Cloudinary dijalankan.
2. **Firebase Firestore Rules** untuk mengatur akses database Cloud Firestore.

---

## 1. Ringkasan Lokasi File Penting

| Komponen | Path Project | Fungsi |
|---|---|---|
| Worker source | `cloudinary-backend-worker/sirs-cloudinary/src/index.ts` | Kode utama Cloudflare Worker untuk generate upload signature dan delete asset. |
| Worker config | `cloudinary-backend-worker/sirs-cloudinary/wrangler.jsonc` | Nama Worker, entry point, compatibility date, env var non-secret. |
| Worker package | `cloudinary-backend-worker/sirs-cloudinary/package.json` | Script deploy/dev/test dan dependency `jose`, `wrangler`, `vitest`. |
| Firebase deploy config | `firebase.json` | Menunjuk file rules Firestore: `firestore.rules`. |
| Firestore rules | `firestore.rules` | Aturan akses database Cloud Firestore. |
| Firebase Android config | `app/google-services.json` | Sumber project id Firebase: `androapp-f262d`. |
| Android endpoint config | `gradle.properties` | Menyimpan `CLOUDINARY_DELETE_ENDPOINT` yang dipakai Android. |
| Android BuildConfig | `app/build.gradle.kts` | Membentuk `BuildConfig.CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_UPLOAD_FOLDER`, dan `CLOUDINARY_DELETE_ENDPOINT`. |

---

## 2. Backend Cloudflare Worker

### 2.1 Identitas Worker

Berdasarkan file `cloudinary-backend-worker/sirs-cloudinary/wrangler.jsonc`:

| Properti | Nilai Aktual |
|---|---|
| Nama Worker | `sirs-cloudinary-delete` |
| Entry point | `src/index.ts` |
| Compatibility date | `2026-07-04` |
| Compatibility flags | `nodejs_compat` |
| Observability | `enabled: true` |
| Upload source maps | `true` |

Walaupun nama Worker masih mengandung kata `delete`, fungsinya sekarang sudah lebih luas: **autentikasi upload dan delete Cloudinary**. Nama Worker dipertahankan agar URL production tidak berubah dan Android tidak mengalami breaking change.

Endpoint production yang digunakan Android:

```text
https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev
```

---

## 3. Environment Variable dan Secret Worker

### 3.1 Environment Variable Non-Secret

Nilai berikut berada di `wrangler.jsonc` bagian `vars`:

| Variable | Nilai Aktual | Keterangan |
|---|---|---|
| `FIREBASE_PROJECT_ID` | `androapp-f262d` | Dipakai untuk memverifikasi issuer dan audience Firebase JWT. |
| `CLOUDINARY_CLOUD_NAME` | `da26s6yet` | Nama cloud Cloudinary. |
| `CLOUDINARY_API_KEY` | `929295481525158` | API key publik Cloudinary untuk request signed upload/delete. |

### 3.2 Secret yang Tidak Boleh Masuk Source Code

| Secret | Lokasi Penyimpanan yang Benar | Fungsi |
|---|---|---|
| `CLOUDINARY_API_SECRET` | Cloudflare Worker Secret (`wrangler secret put`) | Dipakai Worker untuk membuat SHA-1 signature Cloudinary. |

Jangan menulis `CLOUDINARY_API_SECRET` ke:

- `gradle.properties`;
- `app/build.gradle.kts`;
- source Android/Kotlin;
- file Markdown dokumentasi;
- Git repository.

---

## 4. Setup Pertama Kali Cloudflare Worker

Jalankan dari root project:

```bash
cd cloudinary-backend-worker/sirs-cloudinary
npm install
```

Pastikan Wrangler bisa berjalan:

```bash
npx wrangler --version
```

Login ke Cloudflare jika belum pernah login:

```bash
npx wrangler login
```

Simpan secret Cloudinary API Secret ke Cloudflare:

```bash
npx wrangler secret put CLOUDINARY_API_SECRET
```

Saat terminal meminta input, paste nilai Cloudinary API Secret dari dashboard Cloudinary. Nilai ini tidak akan muncul di source code.

---

## 5. Deploy Cloudflare Worker

### 5.1 Deploy Production

```bash
cd cloudinary-backend-worker/sirs-cloudinary
npm run deploy
```

Script tersebut menjalankan perintah dari `package.json`:

```bash
wrangler deploy
```

Alternatif langsung:

```bash
npx wrangler deploy
```

### 5.2 Output yang Diharapkan

Setelah deploy sukses, Worker tersedia pada domain berikut:

```text
https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev
```

Jika nama Worker di `wrangler.jsonc` diubah, URL production juga bisa berubah. Bila URL berubah, update juga nilai berikut di root `gradle.properties`:

```properties
CLOUDINARY_DELETE_ENDPOINT=https://domain-worker-baru.workers.dev
```

Lalu rebuild aplikasi Android agar `BuildConfig.CLOUDINARY_DELETE_ENDPOINT` ikut berubah.

---

## 6. API Backend Worker

Semua endpoint Worker membutuhkan header:

```http
Authorization: Bearer <Firebase_ID_Token>
```

Token tersebut diambil dari user yang sedang login melalui Firebase Auth di Android.

### 6.1 Generate Signature Upload

Endpoint:

```http
GET /generate-signature
```

URL lengkap:

```text
GET https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev/generate-signature
```

Fungsi:

- Android meminta izin upload sebelum mengirim file ke Cloudinary.
- Worker memverifikasi Firebase JWT.
- Worker membuat signature Cloudinary menggunakan `CLOUDINARY_API_SECRET`.
- Worker mengembalikan signature ke Android.

Response sukses:

```json
{
  "signature": "<sha1_signature>",
  "timestamp": "<unix_timestamp>",
  "api_key": "929295481525158",
  "folder": "incident-reports",
  "cloud_name": "da26s6yet"
}
```

Setelah mendapat response ini, Android mengupload file ke Cloudinary dengan parameter:

- `file`;
- `folder`;
- `api_key`;
- `timestamp`;
- `signature`.

Sistem ini menggantikan mekanisme unsigned upload preset, sehingga Cloudinary akan menolak upload dari pihak luar yang tidak memiliki signature valid.

### 6.2 Delete Asset Cloudinary

Endpoint:

```http
POST /
```

URL lengkap:

```text
POST https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev
```

Request body:

```json
{
  "publicId": "incident-reports/example-file-id",
  "resourceType": "image"
}
```

Keterangan field:

| Field | Wajib | Keterangan |
|---|---|---|
| `publicId` | Ya | Public ID asset Cloudinary yang akan dihapus. |
| `resourceType` | Tidak | Default `image`. Gunakan `raw` untuk PDF bila diperlukan. |

Response sukses mengikuti response Cloudinary, contoh:

```json
{
  "result": "ok"
}
```

---

## 7. Verifikasi Manual Setelah Deploy Worker

### 7.1 Cek Endpoint Tanpa Token

Perintah:

```bash
curl -i https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev/generate-signature
```

Hasil yang benar:

```text
HTTP/1.1 401 Unauthorized
```

Ini membuktikan endpoint tidak terbuka untuk publik tanpa login Firebase.

### 7.2 Cek Endpoint dengan Firebase Token

Jika memiliki Firebase ID Token valid dari user login, jalankan:

```bash
curl -i \
  -H "Authorization: Bearer <Firebase_ID_Token>" \
  https://sirs-cloudinary-delete.adityatriwibowo19.workers.dev/generate-signature
```

Hasil yang benar:

```text
HTTP/1.1 200 OK
```

dan body JSON berisi `signature`, `timestamp`, `api_key`, `folder`, dan `cloud_name`.

### 7.3 Verifikasi dari Aplikasi Android

1. Build ulang aplikasi Android setelah endpoint berubah.
2. Login sebagai user aktif.
3. Buat laporan dengan attachment JPG/PNG/PDF valid.
4. Pastikan proses tidak menampilkan error `Not Found`, `Unauthorized`, atau `gagal mendapat otorisasi upload`.
5. Pastikan metadata attachment tersimpan di Firestore setelah laporan berhasil dibuat.

---

## 8. Firestore Rules

### 8.1 Identitas Project Firebase

Project Firebase yang digunakan berdasarkan `app/google-services.json`:

```text
project_id: androapp-f262d
package_name: com.adit.sirs
```

File `firebase.json` menunjuk rules berikut:

```json
{
  "firestore": {
    "rules": "firestore.rules"
  }
}
```

Artinya, saat deploy rules, Firebase CLI akan membaca file root project:

```text
firestore.rules
```

---

## 9. Ringkasan Firestore Security Rules Aktual

Berdasarkan file `firestore.rules`:

### 9.1 Helper Function

| Function | Fungsi |
|---|---|
| `signedIn()` | Mengecek apakah request memiliki `request.auth`. |
| `isOwner(uid)` | Mengecek apakah UID request sama dengan UID dokumen. |
| `currentUserDoc()` | Mengambil dokumen user login dari koleksi `users`. |
| `isAdmin()` | Mengecek user login, role `administrator`, dan `isActive == true`. |
| `isActiveUser()` | Mengecek user login dan `isActive == true`. |
| `validUserCreate(uid)` | Memastikan user hanya membuat profil dirinya sendiri dengan role `user`. |
| `validReportCreate()` | Memvalidasi create laporan: owner, status `pending`, field teks, panjang teks, lokasi, dan severity. |
| `ownsExistingReport()` | Mengecek kepemilikan laporan existing. |
| `canListReports()` | Mengizinkan admin atau active user melakukan query list laporan. |
| `onlyPendingOwnerEdit()` | User hanya boleh edit laporan miliknya jika status masih `pending`. |
| `canDeleteExistingReport()` | Admin boleh delete semua, user hanya boleh delete laporan miliknya yang masih `pending`. |

### 9.2 Koleksi `users/{uid}`

| Operasi | Izin |
|---|---|
| Create | User boleh membuat dokumen miliknya sendiri jika valid, admin juga boleh. |
| Read | Owner atau admin. |
| Update owner | Owner boleh update data tertentu, tetapi tidak boleh mengubah `uid`, `email`, `role`, dan `isActive`. |
| Update/delete admin | Admin boleh update dan delete. |

### 9.3 Koleksi `categories/{categoryId}`

| Operasi | Izin |
|---|---|
| Read | Semua user aktif. |
| Create/update/delete | Admin saja. |

### 9.4 Koleksi `incidentReports/{reportId}`

| Operasi | Izin |
|---|---|
| Create | Harus lolos `validReportCreate()`. |
| Get | Admin atau owner laporan. |
| List | Admin atau active user. Query di Android harus tetap difilter sesuai user untuk user biasa. |
| Update | Admin atau owner jika laporan masih `pending`. |
| Delete | Admin atau owner jika laporan masih `pending`. |

### 9.5 Subkoleksi `incidentReports/{reportId}/statusHistories/{historyId}`

| Operasi | Izin |
|---|---|
| Read | Admin atau owner laporan. |
| Create/update | Admin saja. |
| Delete | Admin atau owner laporan jika status laporan masih `pending`. |

### 9.6 Koleksi `activityLogs/{logId}`

| Operasi | Izin |
|---|---|
| Read | Admin saja. |
| Create | User aktif jika `actorId == request.auth.uid` dan `actorRole` adalah `user` atau `administrator`. |
| Update/delete | Ditolak (`false`). |

### 9.7 Koleksi `deviceTokens/{tokenId}`

| Operasi | Izin |
|---|---|
| Read | Admin saja. |
| Create/update | User aktif hanya untuk token miliknya sendiri. |
| Delete | User aktif hanya untuk token miliknya sendiri. |

### 9.8 Default Deny

Bagian akhir rules:

```firestore
match /{document=**} {
  allow read, write: if false;
}
```

Artinya semua koleksi/dokumen yang tidak disebutkan eksplisit otomatis ditolak.

---

## 10. Deploy Firestore Rules

### 10.1 Install/Login Firebase CLI

Jika Firebase CLI belum tersedia, gunakan `npx` agar tidak perlu install global:

```bash
npx firebase-tools --version
```

Login jika belum login:

```bash
npx firebase-tools login
```

### 10.2 Deploy Rules ke Project Aktual

Jalankan dari root project SIRS:

```bash
npx firebase-tools deploy --only firestore:rules --project androapp-f262d --non-interactive
```

Alternatif jika sudah memiliki Firebase CLI global:

```bash
firebase deploy --only firestore:rules --project androapp-f262d
```

### 10.3 Output yang Diharapkan

Deploy sukses biasanya menampilkan pesan bahwa rules telah di-*release* ke Cloud Firestore untuk project `androapp-f262d`.

Jika muncul error permission, pastikan akun Google yang login di Firebase CLI memiliki akses ke project `androapp-f262d`.

---

## 11. Verifikasi Firestore Rules Setelah Deploy

### 11.1 Verifikasi dari Firebase Console

1. Buka Firebase Console.
2. Pilih project `androapp-f262d`.
3. Masuk ke Firestore Database.
4. Buka tab Rules.
5. Pastikan isi rules sama dengan file `firestore.rules` di repository.

### 11.2 Verifikasi dari Aplikasi Android

Minimal skenario uji:

| Skenario | Hasil yang Diharapkan |
|---|---|
| User aktif login dan membuat laporan valid | Berhasil membuat dokumen `incidentReports`. |
| User biasa membuka daftar laporannya sendiri | Berhasil membaca laporan miliknya. |
| User biasa mencoba edit laporan yang sudah bukan `pending` | Ditolak. |
| Admin membuka semua laporan | Berhasil. |
| Admin mengubah status laporan | Berhasil membuat/mengubah data dan status history. |
| User non-login membaca data | Ditolak. |
| Koleksi tidak dikenal | Ditolak oleh default deny. |

---

## 12. Urutan Deploy yang Disarankan

Jika ada perubahan backend Cloudinary dan rules sekaligus, gunakan urutan berikut:

1. **Deploy Cloudflare Worker terlebih dahulu**
   ```bash
   cd cloudinary-backend-worker/sirs-cloudinary
   npm run deploy
   ```

2. **Deploy Firestore Rules**
   ```bash
   cd ../../
   npx firebase-tools deploy --only firestore:rules --project androapp-f262d --non-interactive
   ```

3. **Rebuild Android jika endpoint/config berubah**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Tes end-to-end dari aplikasi**
   - login;
   - submit laporan dengan attachment;
   - cek laporan tersimpan;
   - cek asset masuk ke Cloudinary;
   - delete laporan/asset jika skenario mengizinkan.

---

## 13. Troubleshooting

| Masalah | Penyebab Umum | Solusi |
|---|---|---|
| `gagal mendapat otorisasi upload: Not Found` | Android memanggil URL root, bukan `/generate-signature`. | Pastikan `CloudinaryUploadDataSource.kt` membentuk URL `baseUrl/generate-signature`, lalu rebuild aplikasi. |
| `401 Unauthorized` dari Worker | Firebase ID Token tidak ada, expired, atau bukan dari project `androapp-f262d`. | Login ulang user di aplikasi, pastikan Firebase project sama. |
| `Method Not Allowed` | Method selain GET/POST/OPTIONS. | Gunakan `GET /generate-signature` untuk upload signature dan `POST /` untuk delete. |
| Cloudinary menolak upload | Signature/timestamp/folder tidak sesuai dengan yang ditandatangani Worker. | Pastikan Android mengirim `folder` persis sama dengan response Worker. |
| Firestore `PERMISSION_DENIED` saat create laporan | Data tidak lolos `validReportCreate()` atau user tidak aktif. | Pastikan dokumen user memiliki `isActive: true`, field laporan valid, dan status awal `pending`. |
| Firebase deploy gagal karena tidak ada `.firebaserc` | Project default tidak tersimpan di repo. | Gunakan flag eksplisit `--project androapp-f262d`. |

---

## 14. Jawaban Singkat untuk Presentasi

**Q: Backend apa yang digunakan project ini?**

A: Project memakai Cloudflare Worker sebagai backend serverless untuk operasi Cloudinary yang sensitif, dan Firebase Firestore Rules sebagai backend security layer database.

**Q: Kenapa Cloudinary API Secret tidak ada di Android?**

A: Karena API Secret disimpan sebagai Cloudflare Worker Secret. Android hanya meminta signature upload/delete dengan Firebase JWT. Secret tidak pernah masuk APK.

**Q: Bagaimana deploy backend Cloudinary?**

A: Masuk ke `cloudinary-backend-worker/sirs-cloudinary`, pastikan secret sudah diset dengan `npx wrangler secret put CLOUDINARY_API_SECRET`, lalu jalankan `npm run deploy`.

**Q: Bagaimana deploy Firestore Rules?**

A: Dari root project jalankan `npx firebase-tools deploy --only firestore:rules --project androapp-f262d --non-interactive`.

**Q: Mengapa nama Worker masih `delete`, padahal sekarang juga upload?**

A: Worker awalnya dibuat untuk delete asset. Setelah security upgrade, Worker juga menerbitkan signature upload. Nama production tetap dipertahankan agar URL Android tidak berubah.
