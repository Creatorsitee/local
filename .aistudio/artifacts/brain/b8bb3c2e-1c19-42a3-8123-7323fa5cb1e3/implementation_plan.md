# Rencana Pengembangan Editor Kode Multi-Tab & Manajemen Berkas

## Ringkasan Fitur
Berdasarkan kebutuhan yang Anda pilih, editor kode akan ditingkatkan untuk mendukung alur kerja pengembangan web yang efisien di layar ponsel:
1. **Sistem Multi-Tab Berkas**: Membuka dan mengedit beberapa berkas sekaligus dengan bilah tab horizontal yang dapat digeser, beralih berkas instan tanpa kehilangan teks yang sedang diedit, serta tombol tutup tab.
2. **Manajemen Berkas Langsung dari Editor**: Menu aksi lengkap di editor untuk mengubah nama berkas (*rename*), membuat salinan/duplikasi (*duplicate*), menghapus berkas, dan menyimpan cepat.
3. **Peningkatan Antarmuka Mobile**: Bilah simbol pemrograman cepat, tombol navigasi tab indentasi, penghitung baris dan karakter, serta tombol pratinjau cepat langsung ke halaman web.

---

## Rincian Perubahan & Arsitektur

### 1. Manajemen State Multi-Tab di `ServerViewModel`
- Mengganti state berkas tunggal menjadi daftar berkas terbuka (`openFiles: StateFlow<List<File>>`).
- Menyimpan konten berkas per berkas (`fileContents: Map<String, String>`) agar perubahan teks tidak hilang saat beralih tab.
- Indikator berkas aktif (`activeFile: StateFlow<File?>`).
- Fungsi `openFileInTab(file: File)`: Membuka berkas baru atau fokus ke tab yang sudah ada.
- Fungsi `closeTab(file: File)`: Menutup tab tertentu dan berpindah ke tab aktif berikutnya.
- Fungsi `renameFile(oldFile: File, newName: String)`: Mengubah nama berkas fisik di disk, memperbarui daftar tab, dan menyinkronkan kembali direktori web.
- Fungsi `duplicateFile(file: File)`: Membuat salinan berkas baru dengan nama `copy_...`.

### 2. Layar Editor Kode Baru (`FileManagerScreen.kt` & Editor Component)
- **Bilah Tab Horizontal**:
  - Menampilkan setiap berkas yang sedang dibuka dengan ekstensi dan indikator tab aktif.
  - Tombol silang `×` kecil pada tiap tab untuk menutup berkas.
  - Tombol `+` untuk memilih dan membuka berkas lain dari direktori.
- **Bilah Aksi & Menu Berkas**:
  - Tombol "Ubah Nama": Dialog cepat untuk mengganti nama berkas yang sedang aktif.
  - Tombol "Duplikasi": Membuat salinan berkas secara instan.
  - Tombol "Hapus": Konfirmasi penghapusan berkas langsung dari editor.
  - Tombol "Simpan": Menyimpan perubahan ke disk.
  - Tombol "Pratinjau": Membuka pratinjau web langsung.
- **Bilah Simbol Coding & Navigasi**:
  - Tombol pintasan simbol penting (`<`, `>`, `/`, `"`, `'`, `{`, `}`, `(`, `)`, `;`, `=`, `!`, `-`, `_`, `\t`).
  - Fitur *Line Wrap* untuk memudahkan membaca kode panjang di layar ponsel tanpa geser horizontal yang rumit.

### 3. Validasi & Standar
- Mempertahankan skema monokrom kontras tinggi (hitam dan putih murni) tanpa perubahan warna yang bertabrakan.
- Menjaga seluruh berkas bersih tanpa komentar kode.
- Menjalankan uji kompilasi dan unit test otomatis untuk memastikan kestabilan aplikasi.
