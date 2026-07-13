# Fitur Keamanan Aplikasi

Dokumen ini merangkum fitur keamanan yang diterapkan pada aplikasi SIRS.

## 1. Autentikasi Pengguna

Aplikasi menggunakan Firebase Authentication untuk proses login, registrasi, reset password, dan pengelolaan session pengguna.

Password pengguna tidak disimpan di Firestore. Password dikelola oleh Firebase Authentication dan disimpan secara aman oleh layanan Firebase. Firestore hanya menyimpan data profil seperti UID, nama, email, role, dan status akun.

## 2. Session Management

Session pengguna dikelola oleh Firebase Authentication. Setelah login, Firebase menyediakan ID Token berbentuk JWT dan refresh token yang dikelola otomatis oleh Firebase SDK.

- ID Token memiliki masa berlaku terbatas.
- Refresh token digunakan oleh Firebase SDK untuk memperbarui session.
- Logout dilakukan melalui Firebase Authentication sehingga session lokal pengguna dihapus.

Aplikasi tidak membuat JWT manual di sisi Android karena secret key tidak aman jika disimpan di dalam APK.

## 3. Validasi Password

Form registrasi menampilkan aturan password agar pengguna mengetahui standar keamanan yang harus dipenuhi.

Aturan password:

- Minimal 12 karakter.
- Memiliki huruf besar.
- Memiliki huruf kecil.
- Memiliki angka.
- Memiliki karakter khusus atau simbol.

## 4. Hak Akses Pengguna dan Admin

Aplikasi membedakan hak akses pengguna biasa dan admin.

- Pengguna hanya dapat melihat dan mengelola laporan miliknya sendiri.
- Admin dapat melihat dan menangani seluruh laporan.
- Pembuatan akun admin tidak tersedia dari halaman registrasi publik.

## 5. Validasi Input

Input pada form laporan divalidasi sebelum data disimpan. Validasi dilakukan untuk memastikan field penting seperti judul, kategori, tingkat keparahan, deskripsi, lokasi, dan tanggal kejadian terisi dengan benar.

Validasi panjang input juga diterapkan pada judul, deskripsi, dan lokasi laporan. Di sisi Firestore, aturan keamanan turut membatasi ukuran field laporan agar data yang masuk tetap sesuai format aplikasi.

## 6. Validasi Upload File

Bukti pendukung laporan dibatasi hanya untuk format yang diperbolehkan.

Format yang didukung:

- JPG/JPEG
- PNG
- PDF

Validasi file meliputi:

- MIME type.
- Ekstensi file.
- Ukuran file.
- Signature file / magic bytes.

Validasi ini bertujuan agar file yang diupload benar-benar sesuai dengan format yang diizinkan, bukan hanya berdasarkan nama file.

## 7. Rate Limiting (Pencegahan Spam)

Untuk mencegah pengguna mengeksploitasi sumber daya Cloudinary dan Firestore (baik sengaja maupun tidak), aplikasi menerapkan pembatasan secara *client-side* melalui implementasi kelas `RateLimiter` (`app/src/main/java/com/adit/sirs/core/common/RateLimiter.kt`).

Aturan kuota yang berlaku:
1. **Laporan Baru**: Dibatasi maksimal **10 laporan** per **30 menit** untuk setiap pengguna.
2. **Unggah Bukti**: Dibatasi maksimal **10 kali unggahan** per **30 menit** untuk setiap pengguna.
3. **Jeda Eksekusi (*Cooldown*)**: Terdapat *cooldown* antarklik sebesar **10 detik** saat *submit* form.

Jika batas tercapai, proses jaringan akan dicegat oleh `ReportViewModel` sebelum memanggil Cloudflare Worker atau Firestore, dan *UI* otomatis menampilkan pesan kesalahan agar pengguna menunggu.

## 8. Activity Log

Aktivitas penting pada aplikasi dicatat agar perubahan data dapat ditelusuri. Contoh aktivitas yang dicatat:

- Registrasi pengguna.
- Pembuatan laporan.
- Perubahan laporan.
- Perubahan status laporan.
- Perubahan kategori.
- Upload bukti laporan.

## 9. Firestore Security Rules

Project menyertakan file `firestore.rules` untuk mengatur akses data di Cloud Firestore. Rules digunakan untuk membatasi akses berdasarkan status login, role pengguna, status aktif akun, dan kepemilikan data.

## 10. Keamanan Data Upload & Backend

Upload bukti laporan menggunakan **Cloudinary**. Aplikasi hanya menyimpan metadata file yang diperlukan di Firestore (URL file, public ID, dsb).

1. **Zero-Secret Client**: Aplikasi Android 100% bebas dari penyimpanan statis secret eksternal. `CLOUDINARY_API_KEY` maupun `CLOUDINARY_API_SECRET` tidak dimasukkan ke dalam `BuildConfig`. 
2. **Signed Upload (Anti-Spam)**: Alih-alih "Unsigned Upload" yang rentan disalahgunakan, Android menerapkan *Signed Upload*. Aplikasi meminta "Surat Izin" (Signature) kepada Cloudflare Worker dengan membawa Token Firebase. Cloudinary otomatis menolak request yang tidak memiliki signature rahasia, mencegah *Denial of Wallet* secara total.
3. **Serverless Backend via Cloudflare Workers**: Semua operasi destruktif (contoh: menghapus foto) diarahkan ke backend Cloudflare Worker. Android bertugas mengirim `publicId` dan menyisipkan **Firebase ID Token** (JWT) ke Header.
4. **JWT Verification**: Worker Cloudflare bertindak memverifikasi validitas token tersebut langsung ke *JWKS Server Google* untuk memastikan bahwa permintaan Hapus berasal dari pengguna sah SIRS. Secret Cloudinary (`CLOUDINARY_API_SECRET`) disimpan aman dan terenkripsi secara remote di server Cloudflare, tidak pernah dikembalikan ke klien Android.

## 11. Pencegahan Duplikasi Unggahan (Idempotensi Klien)

Pada proses *submit* laporan, UI menerapkan *caching upload state*. Apabila sistem gagal menyimpan referensi teks ke *Firestore* (misalnya karena aturan *Firestore Rules*), *file upload* yang sudah terkirim sukses ke Cloudinary akan disimpan statenya di sisi UI (`lastSuccessfulUpload`). Jika pengguna mencoba melakukan pengiriman ulang, sistem tidak akan mengirim duplikat gambar ke server, mencegah eksploitasi beban kapasitas Cloudinary secara tidak sengaja.
