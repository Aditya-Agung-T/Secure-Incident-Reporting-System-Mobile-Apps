# Dokumentasi Implementasi Firestore dan Cloudinary di Aplikasi SIRS

Dokumen ini menjelaskan secara rinci bagaimana arsitektur database (**Cloud Firestore**) dan penyimpanan media (**Cloudinary**) bekerja secara terintegrasi pada aplikasi SIRS, serta mekanisme keamanan yang mengelilinginya.

---

## BAGIAN 1: CLOUD FIRESTORE (Database Utama)

SIRS menggunakan **Cloud Firestore** dari Firebase sebagai database utama (NoSQL) yang menyimpan seluruh teks, profil, log, dan referensi data aplikasi.

### 1.1. Struktur Koleksi (Collections)
Database dipecah ke dalam beberapa koleksi utama yang diratakan (*flat architecture*) agar kueri efisien:

* **`users`**
  * Menyimpan profil pengguna dan admin (UID, nama, email, role: `user`/`administrator`, status aktif, login terakhir).
* **`categories`**
  * Menyimpan data referensi kategori masalah keamanan siber (slug, nama, deskripsi, tips mitigasi). Diatur oleh admin.
* **`incidentReports`**
  * Koleksi paling krusial. Menyimpan metadata laporan (report code, informasi pelapor, lokasi, severity, judul, deskripsi, status).
  * Di dalam dokumen laporan ini juga disimpan **URL** dan **Public ID** dari file bukti yang diunggah ke Cloudinary.
* **`incidentReports/{id}/statusHistories`** *(Sub-collection)*
  * Menyimpan riwayat jejak setiap kali laporan berubah status atau tingkat keparahan, termasuk catatan admin.
* **`activityLogs`**
  * Jejak audit (*Audit Trail*). Mencatat aktivitas penting seperti siapa mendaftar, siapa mengubah laporan, siapa merespons.
* **`deviceTokens`**
  * Menyimpan token FCM (*Firebase Cloud Messaging*) perangkat pengguna agar sistem bisa mengirimkan *Push Notification*.

### 1.2. Keamanan Database (Firestore Rules)
Akses data tidak dibiarkan terbuka. SIRS dilindungi oleh file `firestore.rules` dengan konsep dasar:
* **Pemilik Data (*Owner*)**: Pengguna biasa hanya bisa membaca (`get`, `list`) laporan miliknya sendiri (berdasarkan `userId`). Pengguna juga hanya bisa menghapus/mengedit laporannya sendiri *jika* status laporan masih `pending`.
* **Administrator**: Akun dengan *role* `administrator` memiliki hak baca-tulis penuh (*Full CRUD*) ke semua data, termasuk mengubah status laporan pengguna lain dan melihat *Activity Logs*.
* **Akses Publik Dilarang**: Tidak ada titik akhir (*endpoint*) yang bisa diakses oleh *guest* (orang yang tidak login). Seluruh kueri wajib menyertakan token Firebase Authentication yang sah.

### 1.3. Alur Data di Android (Realtime & Asinkron)
Semua baca-tulis ke Firestore dilakukan di folder `data/remote/`.
* Pengambilan data secara _realtime_ menggunakan `callbackFlow` dengan `addSnapshotListener`. Artinya, jika admin mengubah status laporan di *Dashboard Admin*, layar HP pengguna akan otomatis ter-update seketika (tanpa perlu *pull to refresh*).

---

## BAGIAN 2: CLOUDINARY (Penyimpanan Media)

Karena Firestore tidak ideal/murah untuk menyimpan file besar (seperti foto bukti insiden JPG/PNG atau dokumen PDF), SIRS menggunakan layanan **Cloudinary** sebagai CDN (*Content Delivery Network*) dan penyimpanan file pendukung.

### 2.1. Alur Unggah (Signed Upload Anti-Spam)
Untuk mencegah eksploitasi *Denial of Wallet* (spam upload dari orang asing menggunakan Postman/cURL), SIRS menerapkan arsitektur **Signed Upload** melalui dua langkah:
1. **Validasi Lokal Canggih**: Sebelum file dikirim, aplikasi membaca tipe MIME, ekstensi, dan **Magic Bytes** file tersebut (`FileValidation.kt`). Hal ini menjamin bahwa file `.jpg` memang berisi struktur biner foto, bukan skrip jahat (`.exe`/`.sh`) yang disamarkan.
2. **Generate Signature (Cloudflare Worker)**: Android meminta izin unggah ke *backend serverless* Cloudflare dengan mengirim Token Firebase. Jika token valid (user sah), Worker meracik Tanda Tangan Digital (*Signature SHA-1*) menggunakan rahasia `CLOUDINARY_API_SECRET` dan memberikannya ke Android.
3. **Pemisahan Kredensial (Zero-Secret)**: Android tidak menyimpan *Secret Key*. Android cukup membawa `signature` (surat izin) yang baru didapatkan tersebut untuk langsung mengunggah foto ke Cloudinary. Cloudinary menolak mentah-mentah siapapun yang mencoba mengunggah tanpa *Signature* yang sah.

### 2.2. Alur Hapus dan Serverless Backend
Menghapus gambar dari Cloudinary memerlukan operasi API tersertifikasi (membutuhkan API Key dan API Secret). Karena Android tidak boleh menyimpan *Secret Key*, SIRS menggunakan pendekatan keamanan tinggi:
1. Android meminta penghapusan dengan memanggil **Cloudflare Worker** (Backend Serverless) pada *endpoint* `sirs-cloudinary-delete.adityatriwibowo19.workers.dev`.
2. Di dalam bagian otorisasi (*Header*), Android menyisipkan **Firebase ID Token** (JWT) milik pengguna yang sedang *login*.
3. Cloudflare Worker akan memverifikasi token JWT tersebut langsung ke peladen JWKS Google. Jika token valid dan bukan manipulasi, Cloudflare Worker akan menggunakan *Secret Key* miliknya (yang terkunci di environment Worker) untuk memerintahkan Cloudinary menghapus file tersebut berdasarkan `public_id`.

---

## BAGIAN 3: ORKESTRASI FIRESTORE & CLOUDINARY

Bagaimana kedua platform ini saling berbicara saat aplikasi digunakan? 

### 3.1. Skenario Pembuatan Laporan
1. Pengguna mengisi judul, deskripsi, dan memilih foto dari Galeri.
2. Pengguna klik **Submit**.
3. Aplikasi melakukan validasi lokal.
4. Aplikasi mengunggah foto ke **Cloudinary**.
5. Cloudinary merespon dengan mengembalikan URL foto (contoh: `https://res.cloudinary.com/.../image.png`) dan Public ID (contoh: `incident-reports/img_123`).
6. Aplikasi membungkus data teks pelaporan dan *menyisipkan* URL serta Public ID tersebut ke dalam sebuah JSON (DTO).
7. Aplikasi mengunggah paket JSON tersebut ke **Firestore** (`incidentReports`).
8. Jika Firestore gagal/menolak request (misalnya sinyal hilang atau terbentur aturan keamanan), UI Android akan menerapkan teknik **Idempotensi (Caching State)**. URL gambar akan dikunci di *memory* HP, sehingga bila pengguna memencet ulang tombol "Submit", aplikasi tidak akan meng-upload ganda gambar yang sama ke Cloudinary, melainkan langsung lompat mencoba menyimpan teksnya saja ke Firestore.

### 3.2. Skenario Penghapusan Laporan
1. Pengguna (atau Admin) menekan tombol **Hapus Laporan**.
2. Aplikasi melihat dokumen laporan di Firestore dan mengambil `attachment.publicId` milik file gambar tersebut.
3. Aplikasi memanggil URL Backend **Cloudflare Worker** dan mengirim `publicId` beserta Token Firebase pengguna.
4. Worker berhasil menghapus foto di server Cloudinary.
5. Setelah foto dikonfirmasi terhapus, barulah aplikasi memerintahkan **Firestore** untuk menghapus teks pelaporan (`incidentReports/{id}`).
6. Jika Firestore ikut terhapus, maka proses selesai dengan bersih (tidak meninggalkan *zombie file* atau *orphan data*).