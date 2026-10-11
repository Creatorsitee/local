# Localhost Fullstack Web Server & Hosting

Aplikasi Android untuk meng-host website Fullstack di localhost secara real-time dengan server HTTP/REST multi-method (GET, POST, PUT, DELETE, PATCH, OPTIONS), WebSocket & Server-Sent Events (SSE) untuk komunikasi dua arah real-time, dynamic database backend (JSON/SQLite CRUD), file manager & editor kode bawaan, integrasi Git clone/pull, live access logs, QR code LAN, serta tab pratinjau browser in-app dalam balutan antarmuka monokrom hitam-putih yang bersih, profesional, dan minimalis.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Pembaruan Kapabilitas Fullstack Real-Time**:
> - **Dukungan Fullstack Tanpa Pengecualian**: Tidak hanya berkas statis (HTML/CSS/JS), server menangani dynamic backend request dengan metode HTTP lengkap (GET, POST, PUT, DELETE, PATCH, OPTIONS), parsing body request JSON/Form, serta CORS universal.
> - **Komunikasi Real-Time (WebSockets & SSE)**: Server menyediakan handler WebSocket (`ws://localhost:port/ws`) dan Server-Sent Events (`/api/events`) aktif di thread socket lokal untuk pertukaran data dua arah secara instan (chat, game lokal, IoT, live dashboard).
> - **Database Backend Terintegrasi**: Mesin database lokal (JSON REST DB & SQLite) yang dapat diakses langsung oleh script front-end melalui endpoint `/api/*` untuk menyimpan dan membaca data persisten secara real-time.
> - **Pengelolaan Berkas & Git**: Git clone dan Git pull dari repository publik, manajemen berkas/folder mandiri, impor berkas penyimpanan perangkat, dan editor kode real-time.
> - **Pratinjau & Akses Jaringan**: Tab in-app browser WebView terintegrasi, tombol buka di browser eksternal, deteksi IP WiFi/LAN otomatis, serta QR code monokrom beresolusi tinggi untuk akses cepat dari perangkat lain dalam satu jaringan WiFi.
> - **Standar Kode & Visual**: Desain monokrom hitam-putih murni, tajam, profesional, tanpa data tiruan/dummy, tanpa komentar di semua berkas kode, dan tanpa penyebutan platform pihak ketiga.

---

## 1. Overview & Core Concept

- **Apa yang Dilakukan**: Mengubah perangkat Android menjadi server web fullstack mandiri. Server ini mengeksekusi penyajian berkas front-end, memproses panggilan API backend, mengelola database lokal secara dinamis, dan memfasilitasi komunikasi socket real-time.
- **Target Pengguna**: Pengembang web fullstack, mobile web engineer, mahasiswa, dan teknisi jaringan yang memerlukan lingkungan server lokal portabel yang lengkap di perangkat mobile.
- **Nilai Utama**: Kemampuan fullstack menyeluruh (front-end + backend API + database + WebSockets real-time) langsung dari ponsel pintar tanpa memerlukan instalasi Node.js/Python di terminal Linux, ringan, cepat, dan siap pakai.

---

## 2. User Experience & Visual Design

### Alur Antarmuka Utama

1. **Dashboard Server**:
   - Status server (Aktif / Berhenti) dengan indikator monokrom tegas.
   - Saklar daya server (Mulai / Hentikan Server).
   - Konfigurasi port kustom (default: `8080`) dengan validasi port real-time.
   - Status modul: Static Server (Aktif), REST API Engine (Aktif), WebSocket Server (Aktif).
   - Tampilan alamat akses: Localhost (`http://localhost:8080`) dan Alamat Jaringan LAN (`http://192.168.x.x:8080`).
   - Kartu QR Code monokrom beresolusi tinggi untuk pemindaian instan oleh gawai di jaringan WiFi yang sama.
   - Tombol pintas: Salin URL, Buka di Browser Eksternal, dan Masuk ke Tab Pratinjau.

2. **Backend API & Database Manager**:
   - Panel pengujian endpoint backend bawaan (`/api/data`, `/api/records`, `/api/status`).
   - Tampilan koleksi data JSON/SQLite yang tersimpan dan dapat dimodifikasi oleh web fullstack melalui request POST/PUT/DELETE.
   - Switch WebSocket real-time broadcast: Menguji pengiriman pesan real-time ke semua klien web yang terhubung.

3. **File Explorer & Editor Kode**:
   - Struktur direktori direktori root website (`filesDir/www`).
   - Aksi pembuatan berkas baru (HTML, CSS, JS, JSON) dan folder baru.
   - Tombol Git Clone: Klon repositori web publik langsung ke direktori root.
   - Tombol Git Pull: Tarik pembaruan repositori Git yang terhubung.
   - Tombol Impor Berkas: Ambil berkas dari penyimpanan perangkat.
   - Editor kode monokrom monospace dengan nomor baris dan tombol simpan instan.

4. **Log Akses & Event Real-Time**:
   - Streaming log HTTP & WebSocket secara live.
   - Rincian kolom: Waktu, Protokol (HTTP/WS), Method (GET/POST/PUT/DELETE), Path, Status Code (200, 201, 404, 500), Ukuran respons, dan IP Address klien.
   - Filter log: Semua, Hanya API/Backend, Hanya Berkas Statis, Galat.

5. **Tab Pratinjau (In-App Browser)**:
   - WebView modern terintegrasi dengan bilah alamat URL, reload, tombol kembali/maju.
   - Pengujian langsung aplikasi web fullstack dan koneksi API/WebSocket tanpa perlu keluar aplikasi.

### Identitas Visual Monokrom

- **Palet Warna**:
  - Latar Belakang: `#000000` (Dark) / `#FFFFFF` (Light) monokrom murni.
  - Kartu & Permukaan: `#111111` (Dark) / `#F4F4F4` (Light) dengan batas border tegas `#222222` / `#DDDDDD`.
  - Teks Utama: `#FFFFFF` (Dark) / `#000000` (Light).
  - Teks Sekunder: `#777777` (Dark) / `#666666` (Light).
  - Aksen Kontras: Putih solid pada Dark / Hitam solid pada Light.
- **Tipografi**:
  - Sans-serif modern dan tegas untuk teks navigasi dan kartu kontrol.
  - Monospace murni untuk alamat URL, port, payload JSON, log akses, dan editor kode.

---

## 3. Key Product Decisions & Trade-Offs

- **Arsitektur Socket HTTP/WebSocket Mandiri Multi-Thread**:
  - *Keputusan*: Membangun engine server berbasis `java.net.ServerSocket` dengan thread pool non-blocking yang membedakan permintaan HTTP statis, permintaan REST API dinamis, dan upgrade handshake WebSocket (`101 Switching Protocols`).
  - *Alasan*: Kompatibel penuh di semua arsitektur Android, zero-external runtime, sangat hemat daya, dan mengeksekusi semua method HTTP tanpa batasan.
- **REST API & JSON Database Backend Otomatis**:
  - *Keputusan*: Server menyediakan router `/api/*` terintegrasi yang mendukung CRUD penuh (GET, POST, PUT, DELETE) ke database berkas JSON lokal, ditambah endpoint `/api/execute` untuk logika dinamis.
  - *Alasan*: Memungkinkan website front-end (React, Vue, Vanilla JS) berinteraksi dengan backend nyata, menyimpan data ke database lokal, dan menerima respons JSON valid secara real-time.
- **Dukungan WebSocket Real-Time Handshake**:
  - *Keputusan*: Mengimplementasikan RFC 6455 WebSocket handshake (kalkulasi `Sec-WebSocket-Accept` via SHA-1 + Base64) dan penguraian frame pesan sederhana untuk broadcast dan echo real-time.
  - *Alasan*: Menjamin website fullstack dapat menggunakan `const ws = new WebSocket('ws://' + location.host + '/ws')` untuk fitur real-time seperti chat, game, dan live telemetry.
- **Git Sync via Archive Streaming**:
  - *Keputusan*: Mengunduh repositori Git via zipball stream HTTP/HTTPS dan mengekstrak langsung ke folder web root.
  - *Alasan*: Cepat, andal, dan bekerja tanpa binary CLI eksternal.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                     JETPACK COMPOSE UI                          │
│  ┌──────────────┬──────────────┬─────────────┬────────────────┐ │
│  │  Dashboard   │  File Trees  │ Code Editor │   Live Logs    │ │
│  │  & QR Code   │  & Git Sync  │ (Monospace) │  & API Monitor │ │
│  └──────┬───────┴──────┬───────┴──────┬──────┴────────┬───────┘ │
│         │              │              │               │         │
│         ▼              ▼              ▼               ▼         │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    SERVER VIEWMODEL                        │ │
│  │  - ServerState (Running, Port, LAN IP, Active Clients)     │ │
│  │  - Live Access & WebSocket Logs StateFlow                  │ │
│  │  - Database Collections StateFlow                          │ │
│  │  - Current File & Tree StateFlow                           │ │
│  └─────────────────────────────┬──────────────────────────────┘ │
└────────────────────────────────┼────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FULLSTACK SERVER ENGINE                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │          Multi-Threaded ServerSocket Listener             │  │
│  │                 (LocalHttpServer.kt)                      │  │
│  └───────┬───────────────────┬───────────────────┬───────────┘  │
│          │                   │                   │              │
│          ▼                   ▼                   ▼              │
│  ┌───────────────┐   ┌───────────────┐   ┌───────────────┐      │
│  │ Static File   │   │  Dynamic REST │   │   WebSocket   │      │
│  │  Dispatcher   │   │  API Backend  │   │  Engine (WS)  │      │
│  │ (MIME, Range, │   │ (CRUD Routes, │   │  (RFC 6455,   │      │
│  │  Index auto)  │   │  JSON/SQLite) │   │  Live Events) │      │
│  └───────┬───────┘   └───────┬───────┘   └───────┬───────┘      │
│          │                   │                   │              │
│          ▼                   ▼                   ▼              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │           Storage Layer (filesDir/www & data/)            │  │
│  │   - index.html, style.css, script.js, db.json             │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Komponen Utama yang Akan Dibangun

1. **`FullstackHttpServer.kt`**: Multi-threaded server engine yang menangani:
   - File statis (HTML, CSS, JS, gambar, font, SVG, dll.) dengan MIME mapping.
   - REST API handler: Menangani `/api/data`, `/api/records`, `/api/echo`, `/api/info` dengan metode GET, POST, PUT, DELETE, PATCH, dan OPTIONS (CORS pre-flight).
   - WebSocket handler: Menangani handshake `Upgrade: websocket`, membaca dan membroadcast frame teks ke seluruh klien yang terhubung secara real-time.
   - Server-Sent Events (SSE): Endpoint `/api/events` untuk push notifikasi satu arah ke peramban.
2. **`DatabaseEngine.kt`**: Mesin penyimpanan data JSON/SQLite lokal yang mengelola koleksi data, mendukung operasi CRUD otomatis untuk endpoint `/api/*`.
3. **`NetworkUtils.kt`**: Deteksi IP WiFi/LAN perangkat (`192.168.x.x`), subnet check, dan ketersediaan port.
4. **`QrCodeGenerator.kt`**: Generator QR Code monokrom mandiri yang dirender presisi pada Compose Canvas.
5. **`GitService.kt`**: Mengunduh dan mengekstrak repositori Git publik via HTTP/HTTPS ke direktori web root (Git Clone & Git Pull).
6. **`FileManagerService.kt`**: Operasi berkas (buat, baca, tulis, hapus, impor, dan ekspor berkas situs).
7. **`ServerViewModel.kt`**: Pengelola state terpusat untuk kontrol server, live logs, database, dan editor berkas.
8. **`UI Screens`**:
   - `DashboardScreen.kt`: Status server, kontrol saklar, pemilihan port, status modul fullstack, kartu URL, dan QR Code.
   - `FileManagerScreen.kt`: Penjelajah berkas, aksi Git Clone & Git Pull, dialog pembuatan berkas, impor berkas.
   - `CodeEditorScreen.kt`: Editor kode monospace lengkap dengan nomor baris dan tombol simpan.
   - `DatabaseApiScreen.kt`: Inspektor backend REST API & database lokal, pemantauan WebSocket klien.
   - `AccessLogsScreen.kt`: Log streaming real-time dengan filter status code dan method HTTP.
   - `PreviewScreen.kt`: Tab in-app browser (WebView) dengan bilah navigasi dan tombol pintas browser eksternal.
9. **`Theme.kt` & `Color.kt`**: Tema monokrom hitam-putih murni yang elegan, tajam, dan responsif.

### Rencana Verifikasi

- Menguji start/stop server pada port kustom.
- Menguji penyajian halaman HTML, CSS, dan JS statis.
- Menguji request REST API (GET, POST, PUT, DELETE) dan memastikan respons JSON serta persistensi data di backend lokal.
- Menguji koneksi WebSocket dari front-end dan pertukaran pesan real-time.
- Menguji clone repositori Git publik dan memastikan berkas langsung siap disajikan.
- Menjalankan `compile_applet` untuk memastikan seluruh kode berhasil dikompilasi tanpa galat.
