# 🐾 LeftyPet

> **Ultra-lightweight modern Pet plugin for Paper & Leaves 1.21.8**  
> Developed by **DarkIgnite**

---

## ✨ Features

- **Modern Display Entities (`ItemDisplay` & `TextDisplay`):**
  - Menggunakan Display Entity modern Minecraft 1.21 yang di-scale raksasa/cute tanpa beban physics tick.
  - Vektor *Lerp* halus & client-side interpolation (0% pathfinding lag di Leaves).
  - Anti-ghost entity: otomatis bersih saat player logout, death, atau change world.

- **Mount System (Dinaiki):**
  - Pet dapat dinaiki saat mencapai **Level 3**.
  - Menggunakan API Paper modern `PlayerInputEvent` (WASD + Spacebar) tanpa NMS & tanpa packet injection.
  - Memiliki fitur **Double Jump / Boost Dash** di udara saat mencapai **Level 5**.

- **Combat AI:**
  - Menyerang mob hostile di sekitar atau yang sedang menyerang pemilik.
  - Serangan proyektil sihir dengan partikel khusus dan sound effect.
  - Aman: tidak melukai player lain, villager, maupun pet peliharaan teman.

- **AFK Training Altar (Sistem Upgrade):**
  - Player dapat menempatkan **Pet Training Altar** (`/pet altar`).
  - Klik kanan altar dengan pet untuk memulai AFK upgrade berdurasi bertingkat:
    - **Lv 1 ➜ Lv 2:** 30 Detik
    - **Lv 2 ➜ Lv 3:** 90 Detik (1m 30s)
    - **Lv 3 ➜ Lv 4:** 180 Detik (3m)
    - **Lv 4 ➜ Lv 5:** 300 Detik (5m), dan seterusnya.
  - **0% Server Tick Lag (Timestamp Based):** Data upgrade disimpan berdasarkan waktu `System.currentTimeMillis()`. Player bisa logout atau server restart tanpa merusak progress.
  - Dilengkapi hologram countdown real-time dan animasi rotasi melayang di atas altar.

- **Pet Classes:**
  - ⚔ **Fighter:** +50% Attack Damage, Critical Hits.
  - 💖 **Support:** Pasif Regeneration aura saat HP pemilik sekarat (< 50%).
  - 🌾 **Looter:** Auto-pickup item drop di sekitar + 25% bonus Mob EXP.
  - 🐎 **Traveler:** +40% Kecepatan Mount, bebas fall damage saat menaiki pet.

- **Energy & Hunger System:**
  - Energi pet (0–100%) berkurang perlahan saat beraktivitas.
  - Beri makan pet dengan daging, apel, wortel emas, atau roti.
  - Pet tidak pernah mati permanen: hanya pingsan saat energi habis dan dapat dibangunkan kembali dengan makanan.

- **Cosmetics & GUI:**
  - Menu interaktif `/pet` (GUI chest).
  - Pilihan Skin Kepala Custom (Spirit, Slime King, Flame Dragon, Baby Panda, Cyber Robot, Shadow Demon, Golden Angel).
  - Pilihan Particle Trail (Flame, Soul Fire, Heart, Magic Glyph, Emerald, End Rod).
  - Ganti nama pet via `/pet rename <nama>`.

---

## 📜 Commands & Permissions

### Player Commands (`leftypet.use` - default: true)
| Command | Deskripsi |
| :--- | :--- |
| `/pet` | Membuka Dashboard GUI Pet |
| `/pet summon` | Memanggil pet ke samping bahu |
| `/pet dismiss` | Menyembunyikan pet ke alam spiritual |
| `/pet mount` | Menaiki pet (unlocked Lv 3+) |
| `/pet rename <nama>` | Mengganti nama pet kamu |
| `/pet class <class>` | Memilih spesialisasi kelas |
| `/pet altar` | Mendapatkan blok Pet Training Altar |

### Admin Commands (`leftypet.admin` - default: op)
| Command | Deskripsi |
| :--- | :--- |
| `/leftypet reload` | Reload file konfigurasi |
| `/leftypet setlevel <player> <level>` | Atur level pet pemain |
| `/leftypet setenergy <player> <amount>` | Atur energi pet pemain |
| `/leftypet givealtar <player>` | Berikan Altar ke pemain |

---

## 🛠 Compilation

Dibuat untuk **Java 21** dan **Paper / Leaves 1.21.8**:
```bash
mvn clean package
```
Output file `.jar` akan tersedia di dalam direktori `target/LeftyPet-1.0.0.jar`.
