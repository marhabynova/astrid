<div align="center">

<img src="app/src/main/res/drawable/fed_icon.png" width="96px" alt="Astrid Icon" style="border-radius: 24px;">

# Astrid 🌱
### *Minimal, Warm & Intelligent Pantry Expiration Tracker for Android*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-blue?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-emerald?style=flat-square)](https://developer.android.com/jetpack/compose)
[![ML Kit](https://img.shields.io/badge/Google%20ML%20Kit-On--Device%20OCR-orange?style=flat-square)](https://developers.google.com/ml-kit)
[![Room DB](https://img.shields.io/badge/Room-Database%20Offline--First-purple?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

</div>

---

## ✨ Tentang Astrid

**Astrid** adalah aplikasi pelacak tanggal kedaluwarsa dan inventori dapur yang dirancang dengan filosofi **minimalisme fungsional (Nielsen Norman Group Heuristic #8 & Apple HIG)**. Astrid membebaskan pengguna dari antarmuka yang rumit dan rasa bersalah membuang makanan dengan pendekatan visual yang tenang, hangat, dan ramah pengguna.

---

## 🚀 Fitur Unggulan

* **📷 AI Camera OCR Date Recognition:** Cukup arahkan kamera ke kemasan (Susu, Kaleng, Roti, Mie), mesin regex cerdas membaca tanggal secara otomatis tanpa perlu diketik manual. Dilengkapi sanitasi typo karakter OCR umum (`O` &rarr; `0`, `l` &rarr; `1`, `S` &rarr; `5`).
* **🍳 "Use-This-Up" Recipe Suggester:** Memasangkan bahan-bahan dapur yang mendekati batas kedaluwarsa dengan ide resep masakan cepat saji di rumah (5–15 menit).
* **🌿 Food Waste & Savings Analytics:** Pantau berapa persen makanan yang berhasil kamu selamatkan, estimasi rupiah belanja yang dihemat, serta jejak karbon yang dicegah.
* **❄️ Storage Zone Classification:** Pemisahan otomatis bahan makanan berdasarkan lokasi penyimpanan: **Kulkas (Fridge)**, **Freezer (Pembeku)**, dan **Pantry (Rak Dapur)**.
* **🎨 Material 3 Expressive & Dark Mode:** Tampilan menyejukkan mata dengan kontras tinggi, typography jelas, dan dukungan penuh tema dinamis Android.
* **🔒 100% Offline-First & Privasi:** Semua data inventori tersimpan aman di database lokal Room HP kamu tanpa pengumpulan data pribadi.

---

## 🛠️ Arsitektur & Teknologi

* **Bahasa:** Kotlin (JVM 21)
* **UI Toolkit:** Jetpack Compose & Material You (Material 3)
* **Arsitektur:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
* **Database:** Room ORM dengan auto-migration
* **Vision & AI:** Google ML Kit Text Recognition & Barcode Scanning (ZXing)
* **Dependency Injection:** Dagger Hilt
* **Jaringan & API:** Retrofit & Open Food Facts API

---

## 🧪 Menjalankan Pengujian Otomatis

Proyek ini telah dilengkapi dengan rangkaian unit test komprehensif:

```bash
# Menjalankan seluruh pengujian unit test
./gradlew testFullDebugUnitTest

# Menjalankan pengujian untuk versi FOSS
./gradlew testFossDebugUnitTest -PbuildFoss
```

---

## 📄 Lisensi

Diterbitkan di bawah lisensi [MIT License](LICENSE).