# 🐾 LeftyPet v2.0

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-2.0.0-emerald?style=for-the-badge)
![Performance](https://img.shields.io/badge/Performance-Optimized-brightgreen?style=for-the-badge)

**Ultra-lightweight modern Display Entity Pet plugin with Mounts, Combat, 3x3 AFK Training Altars, Duels, and Crossplay.**  
*Developed with ❤️ by **DarkIgnite** for **Leftycraft Network**.*

[Fitur Utama](#-fitur-utama) • [Optimasi v2.0](#-high-performance-engine-v20) • [Perintah & Izin](#-perintah--izin) • [Kompilasi](#-kompilasi--instalasi)

---

</div>

## ✨ Fitur Utama

### 🌟 Modern Display Entities & Crossplay
* **Tanpa Physics Tick Lag**: Menggunakan `ItemDisplay` & `TextDisplay` Minecraft 1.21+ yang di-scale dengan rapi tanpa membebani physics engine server.
* **Smooth Client-Side Interpolation**: Rotasi dan pergerakan halus di sisi client, meminimalisir packet overhead.
* **GeyserMC / Bedrock Fallback**: Kompatibel penuh untuk pemain Bedrock Edition via otomatisasi entitas `ArmorStand` tersembunyi.
* **Anti-Ghost Entity**: Entitas *non-persistent* otomatis bersih secara instan saat pemain logout, mati, berpindah world, atau server restart.

---

### 🏛 3x3 Multi-block AFK Training Altar
* **Struktur 3x3 Interaktif**: Letakkan blok altar untuk membangun fasilitas kuil 3x3 bertingkat lengkap dengan animasi konstruksi blok per blok.
* **Multi-Tier Progression**:
  * 🟡 **Altar Level 1**: Durasi standar (0% diskon).
  * 🟢 **Altar Level 2**: Diskon waktu upgrade **-10%**.
  * 🔵 **Altar Level 3**: Diskon waktu upgrade **-20%** + portal particles.
  * 🟣 **Altar Level 4 (Celestial Exclusive)**: Diskon waktu upgrade **-50%** + efek partikel kosmik naga!
* **Offline-Friendly & Cooldown**:
  * Durasi upgrade berjalan real-time saat pemain online.
  * Sistem cooldown upgrade dinamis berbasis tier level (mencegah eksploitasi upgrade instan).
  * Perlindungan anti-griefing & proteksi pembongkaran saat altar sedang aktif atau dalam masa cooldown.
  * Hologram perspektif terpadu (*Unified Billboard*) yang otomatis menghadap posisi pemain terdekat.

---

### 🐎 Mount System (Dapat Dinaiki)
* **Kontrol Kendali WASD**: Mengendarai pet dengan mulus via Paper API `PlayerInputEvent` (WASD + Spacebar) tanpa NMS usang atau packet injection yang rapuh.
* **Double Jump & Air Dash**: Lakukan lompatan ganda di udara saat pet mencapai level tinggi untuk mobilitas ekstra.

---

### ⚔ Combat AI & Duels
* **Combat Assistant**: Pet otomatis mendeteksi dan menembakkan sihir proyektil ke monster hostile di sekitar pemilik.
* **Pet Duels (`/pet duel <player>`)**: Tantang pemain lain dalam duel adu pet secara adil dengan notifikasi interaktif di chat!
* **Safe Targeting**: AI cerdas tidak akan melukai sesama pemain, villager, maupun pet teman.

---

### 🎭 Sistem Kelas & Spesialisasi
1. ⚔ **Fighter**: Bonus Attack Damage +50% dan peluang serangan kritikal.
2. 💖 **Support**: Pasif aura regenerasi ketika darah pemilik berada di bawah 50%.
3. 🌾 **Looter**: Auto-pickup drop item di sekitar serta bonus +25% Mob EXP.
4. 🐎 **Traveler**: +40% kecepatan mount dan imunitas terhadap fall damage saat berkendara.

---

### 🍖 Energy & Feeding
* Energi pet (0–100%) berkurang bertahap selama beraktivitas.
* Beri makan pet dengan berbagai makanan Minecraft (daging, apel emas, wortel emas, roti).
* Pet **tidak pernah mati permanen**: hanya pingsan saat energi habis dan dapat dibangunkan kembali dengan makanan favoritnya.

---

## ⚡ High-Performance Engine (v2.0)

Pembaruan versi 2.0 dirancang khusus untuk server berskala besar dengan beban tick mendekati **0% MSPT**:

* 🛡 **Zero Synchronous Chunk Loading**: Menggunakan `World.isChunkLoaded()` murni, mencegah server me-load paksa chunk pemain offline dari disk.
* 💾 **Disk I/O NBT Caching**: Menyimpan cache `ownerName` di memory dan `altars.yml`, mengeliminasi pembacaan file NBT playerdata dari harddisk saat ticker berjalan.
* 👁 **Proximity Culling (Sleep Mode)**: Altar otomatis masuk mode tidur saat tidak ada pemain dalam radius 32 blok (melewatkan render partikel, rotasi kepala, dan dispatch paket metadata).
* 📝 **Display Dirty-Checking**: Adventure MiniMessage parser dan pembaruan paket metadata hanya dieksekusi saat data teks benar-benar berubah.
* ⏱ **Relaxed Scheduler Ticking**: Mengoptimalkan interval ticker ke frekuensi 1,0 detik (20 ticks), memangkas siklus CPU hingga 80%+.

---

## 📜 Perintah & Izin

### 🎮 Player Commands (`leftypet.use` — Default: Semua Pemain)

| Perintah | Alias | Deskripsi |
| :--- | :--- | :--- |
| `/pet` | `/lp`, `/pets` | Membuka Dashboard GUI Pet |
| `/pet summon` | — | Memanggil pet ke samping bahu |
| `/pet dismiss` | — | Menyembunyikan pet ke alam spiritual |
| `/pet mount` | — | Menaiki pet (terbuka di Lv. 3+) |
| `/pet rename <nama>` | — | Mengganti nama pet kamu |
| `/pet class <class>` | — | Memilih atau mengubah spesialisasi kelas |
| `/pet altar` | — | Mengklaim blok kuil AFK Training Altar (max 3x/hari) |
| `/pet duel <player>` | — | Menantang pet pemain lain dalam duel |
| `/pet roadmap` | — | Melihat roadmap perkembangan level & bonus pet |

### 🛠 Admin Commands (`leftypet.admin` — Default: OP)

| Perintah | Deskripsi |
| :--- | :--- |
| `/leftypet reload` | Reload konfigurasi `config.yml` |
| `/leftypet setlevel <player> <level>` | Mengatur level pet pemain tertentu |
| `/leftypet setenergy <player> <amount>` | Mengatur daya energi pet pemain |
| `/leftypet givealtar <player> [level]` | Memberikan item altar tingkat 1–4 ke pemain |
| `/leftypet removealtar <player>` | Membongkar paksa altar milik pemain |
| `/leftypet tpaltar <player>` | Teleportasi langsung ke lokasi altar milik pemain |

---

## 🛠 Kompilasi & Instalasi

### Prasyarat
* **Java 21** atau versi lebih baru
* **Maven 3.8+**
* Server **Paper / Leaves / Purpur 1.21+**

### Langkah Kompilasi:
```bash
git clone https://github.com/DarkIgnite/LeftyPet.git
cd LeftyPet
mvn clean package
```

File `.jar` siap pakai akan berada di:  
📁 `target/LeftyPet-2.0.0.jar`

---

<div align="center">
  <sub>Dilisensikan dan dikembangkan untuk ekosistem Leftycraft Network.</sub>
</div>
