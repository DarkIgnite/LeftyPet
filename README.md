<div align="center">

# LeftyPet

Plugin pet berbasis Display Entity untuk Paper 1.21+ — lengkap dengan kelas, combat AI, training altar, dapur tycoon, duel, dan dukungan crossplay Bedrock.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Paper](https://img.shields.io/badge/Paper-1.21%2B-blue?style=flat-square)
![Version](https://img.shields.io/badge/version-2.0.0-success?style=flat-square)

Dikembangkan oleh **DarkIgnite** untuk **Leftycraft Network**.

[Fitur](#fitur) · [Instalasi](#instalasi) · [Perintah](#perintah) · [Permission](#permission) · [Konfigurasi](#konfigurasi) · [Build](#build-dari-source)

</div>

---

## Fitur

### Pet berbasis Display Entity
- Pet dirender dengan `ItemDisplay` + `TextDisplay`, bukan mob, sehingga tidak ikut AI/physics tick server.
- Gerakan dan rotasi diinterpolasi di sisi client agar halus.
- **Crossplay Bedrock (GeyserMC):** pemain Bedrock melihat fallback `ArmorStand` tak terlihat yang membawa kepala pet.
- Semua entity pet bersifat *non-persistent* — otomatis hilang saat logout, pindah world, atau restart, jadi tidak ada ghost entity.

### Kelas pet
| Kelas | Spesialisasi |
| :--- | :--- |
| **Fighter** | +50% attack damage, −30% attack cooldown |
| **Support** | Aura regenerasi saat HP pemilik di bawah 50% |
| **Looter** | Auto-pickup item di sekitar, +25% mob EXP |
| **Traveler** | Aura Speed II untuk pemilik, +40% kecepatan gerak pet |

Ganti kelas kapan saja dengan `/pet class <kelas>`.

### Combat & Duel
- Pet otomatis menyerang mob hostile di sekitar pemilik (bisa dimatikan dengan `/pet attack`).
- Targeting aman: tidak menyerang pemain, villager, atau pet lain.
- **Duel pet** antar pemain, opsional dengan taruhan uang (Vault).

### Energi & makanan
- Energi pet (0–100) berkurang saat menyerang dan bekerja.
- Isi ulang dengan makanan: daging, roti, apel, golden carrot, golden apple, dll. (bisa diatur di config).
- Pet tidak pernah mati permanen — hanya pingsan saat energi habis, lalu bangun lagi setelah diberi makan.

### Training Altar (AFK upgrade)
Struktur multi-block 3×3 tempat pet di-upgrade levelnya secara AFK. Altar dibangun dengan animasi blok per blok dan dilengkapi hologram yang menghadap pemain terdekat.

| Level altar | Potongan waktu upgrade |
| :---: | :---: |
| 1 | — |
| 2 | −10% |
| 3 | −20% |
| 4 | −30% |
| 5 | −40% |
| 6 *(Celestial)* | −50% |

- Durasi upgrade berjalan saat pemain online, dengan cooldown per tier untuk mencegah eksploitasi.
- Altar dilindungi dari griefing dan tidak bisa dibongkar saat sedang aktif atau cooldown.
- Klaim altar lewat `/pet altar` (maksimal 3× per hari). Level 6 membutuhkan permission `leftypet.celestial`.
- **Instant Upgrader** (item soulbound dari admin) untuk menyelesaikan upgrade seketika.

### Dapur MBG (Kitchen Tycoon)
Tempatkan pet di dapur untuk menghasilkan uang secara pasif:

1. **Memasak** di furnace (30 detik)
2. **Mengemas** di meja (30 detik)
3. **Menyerahkan** pesanan di jendela (5 detik)

- Setiap pesanan selesai menghasilkan uang acak yang disimpan di kasir; batas maksimalnya naik sesuai level pet.
- Setiap pesanan memakan 10% energi pet — pet beristirahat saat lapar.
- Pengumuman global saat pemain mengklaim kasir (dengan cooldown).
- Mendukung multiplier dari plugin **DungeonEventBoost** bila terpasang.

### Lainnya
- **Dashboard GUI** untuk semua fitur pet (`/pet`).
- **Roadmap** progresi level beserta milestone bonusnya.
- **Leaderboard** top 10 level pet di server.
- **Skin & trail kosmetik**, termasuk skin eksklusif Member++ dan Celestial.
- Integrasi proteksi region dengan **WorldGuard** dan **RedProtect**.

---

## Instalasi

**Kebutuhan:**
- Server **Paper 1.21+** (atau fork-nya: Purpur, Leaf, dll.)
- **Java 21**
- *(Opsional)* Vault + plugin ekonomi, WorldGuard, RedProtect, MythicMobs

**Langkah:**
1. Taruh `LeftyPet-2.0.0.jar` di folder `plugins/`.
2. Restart server (jangan gunakan `/reload`).
3. Sesuaikan `plugins/LeftyPet/config.yml`, lalu jalankan `/leftypet reload`.

---

## Perintah

### Pemain
Alias: `/lp`, `/pets`

| Perintah | Deskripsi |
| :--- | :--- |
| `/pet` | Buka dashboard GUI pet |
| `/pet summon` | Panggil pet |
| `/pet dismiss` | Simpan pet |
| `/pet rename <nama>` | Ganti nama pet (maks. 15 karakter) |
| `/pet class <kelas>` | Pilih kelas: `FIGHTER`, `SUPPORT`, `LOOTER`, `TRAVELER` |
| `/pet attack` | Aktifkan/nonaktifkan auto-attack |
| `/pet altar` | Klaim Training Altar (maks. 3× per hari) |
| `/pet kitchen` | Buka menu Dapur MBG (alias: `dapur`) |
| `/pet roadmap` | Lihat roadmap progresi level |
| `/pet top` | Lihat top 10 level pet |
| `/pet duel <pemain> [taruhan]` | Tantang pemain lain berduel |
| `/pet duel accept\|decline` | Terima/tolak tantangan duel |
| `/pet help` | Tampilkan daftar perintah |

### Admin
Alias: `/lpa`, `/petadmin`

| Perintah | Deskripsi |
| :--- | :--- |
| `/leftypet reload` | Muat ulang konfigurasi |
| `/leftypet setlevel <pemain> <level>` | Atur level pet |
| `/leftypet setenergy <pemain> <jumlah>` | Atur energi pet |
| `/leftypet givealtar <pemain> [level]` | Beri item altar (level 1–6) |
| `/leftypet removealtar <pemain>` | Hapus altar milik pemain |
| `/leftypet tpaltar <pemain>` | Teleport ke altar milik pemain |
| `/leftypet givekitchen <pemain>` | Beri item Dapur MBG |
| `/leftypet removekitchen <pemain>` | Hapus dapur milik pemain |
| `/leftypet tpkitchen <pemain>` | Teleport ke dapur milik pemain |
| `/leftypet giveinstantupgrader <pemain> [jumlah]` | Beri Instant Upgrader (soulbound) |
| `/leftypet cleankitchen` | Bersihkan semua ghost entity dapur |

---

## Permission

| Permission | Default | Deskripsi |
| :--- | :---: | :--- |
| `leftypet.use` | semua | Akses fitur pet |
| `leftypet.admin` | OP | Akses perintah admin |
| `leftypet.memberplusplus` | — | Buka skin eksklusif Member++ (alias: `leftypet.memberpp`) |
| `leftypet.celestial` | — | Izinkan upgrade altar ke Level 6 |

---

## Konfigurasi

Bagian utama di `config.yml`:

| Bagian | Isi |
| :--- | :--- |
| `settings` | Skala pet, durasi interpolasi, level maksimum |
| `upgrade-durations` | Durasi upgrade per level (detik); level di atas 10 bertambah 30 menit per level |
| `energy` | Energi maksimum, konsumsi per serangan, nilai tiap makanan |
| `economy` | Biaya upgrade via Vault (`base-cost + level × cost-per-level`) |
| `kitchen` | Rentang penghasilan dapur, bonus per level, cooldown pengumuman |
| `classes` | Nama, ikon, deskripsi, dan multiplier tiap kelas |
| `skins` / `trails` | Daftar skin kepala (Base64) dan efek trail |
| `messages` | Semua pesan plugin (format MiniMessage) |

---

## Performa

LeftyPet dirancang agar ringan di server ramai:

- **Menghindari sync chunk load** — ticker memeriksa `World#isChunkLoaded` lebih dulu, dan pet tidak di-respawn ke chunk yang belum ter-load.
- **Respawn throttling** — respawn pet dibatasi agar tidak memicu badai event spawn.
- **Proximity culling** — altar masuk mode tidur bila tidak ada pemain dalam radius 32 blok.
- **Dirty-checking** — teks hologram dan metadata hanya dikirim ulang saat nilainya berubah.
- **Penyimpanan async** — data dapur disimpan di luar main thread.

---

## Build dari source

```bash
git clone https://github.com/DarkIgnite/LeftyPet.git
cd LeftyPet
mvn clean package
```

Hasil build: `target/LeftyPet-2.0.0.jar`

**Struktur proyek:**
```
src/main/java/com/leftycraft/leftypet/
├── command/   # /pet dan /leftypet
├── config/    # Pemuat config.yml
├── entity/    # ActivePet (entity & perilaku pet)
├── gui/       # Menu inventory
├── hook/      # Integrasi WorldGuard & RedProtect
├── listener/  # Event listener
├── manager/   # Logika pet, altar, dapur, duel, leaderboard, ekonomi
├── model/     # Model data
└── util/      # Utilitas (warna, head, schematic, Bedrock)
```

---

<div align="center">
<sub>© Leftycraft Network · Dikembangkan oleh DarkIgnite</sub>
</div>
