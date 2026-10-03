package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.model.PetLeaderboardEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PetLeaderboardManager {

    private final LeftyPetPlugin plugin;
    private final List<PetLeaderboardEntry> cachedLeaderboard = new ArrayList<>();
    private final Map<UUID, String> nameCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0L;
    private static final long CACHE_TTL_MS = 60_000L; // 60 seconds

    public PetLeaderboardManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized List<PetLeaderboardEntry> getTopPets(int limit) {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate > CACHE_TTL_MS || cachedLeaderboard.isEmpty()) {
            refreshLeaderboard();
        }
        int max = Math.min(limit, cachedLeaderboard.size());
        return new ArrayList<>(cachedLeaderboard.subList(0, max));
    }

    public synchronized void refreshLeaderboard() {
        File dataFolder = plugin.getPetManager().getDataFolder();
        if (!dataFolder.exists()) return;

        Map<UUID, PetLeaderboardEntry> entries = new HashMap<>();

        // 1. Online cached pets first
        for (Map.Entry<UUID, PetData> online : plugin.getPetManager().getPetDataCache().entrySet()) {
            UUID uuid = online.getKey();
            PetData data = online.getValue();
            String name = resolvePlayerName(uuid);
            entries.put(uuid, new PetLeaderboardEntry(
                    uuid,
                    name,
                    data.getLevel(),
                    data.getName(),
                    data.getPetClass(),
                    data.getSkinKey()
            ));
        }

        // 2. Scan offline player data files
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                try {
                    UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));
                    if (entries.containsKey(uuid)) continue; // online data is freshest

                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                    int lvl = cfg.getInt("level", 1);
                    String petName = cfg.getString("name", "Spirit Companion");
                    String skinKey = cfg.getString("skin", "spirit");
                    PetClass petClass;
                    try {
                        petClass = PetClass.valueOf(cfg.getString("class", "FIGHTER"));
                    } catch (Exception e) {
                        petClass = PetClass.FIGHTER;
                    }
                    String playerName = resolvePlayerName(uuid);

                    entries.put(uuid, new PetLeaderboardEntry(
                            uuid,
                            playerName,
                            lvl,
                            petName,
                            petClass,
                            skinKey
                    ));
                } catch (Exception ignored) {}
            }
        }

        List<PetLeaderboardEntry> sorted = new ArrayList<>(entries.values());
        Collections.sort(sorted); // descending by level

        cachedLeaderboard.clear();
        cachedLeaderboard.addAll(sorted);
        lastCacheUpdate = System.currentTimeMillis();
    }

    private String resolvePlayerName(UUID uuid) {
        String cached = nameCache.get(uuid);
        if (cached != null) return cached;

        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        String name = op.getName();
        if (name == null || name.isBlank()) {
            name = "Pemain-" + uuid.toString().substring(0, 5);
        }
        nameCache.put(uuid, name);
        return name;
    }
}
