package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PetManager {

    private final LeftyPetPlugin plugin;
    private final Map<UUID, PetData> petDataCache = new ConcurrentHashMap<>();
    private final Map<UUID, ActivePet> activePets = new ConcurrentHashMap<>();
    private final Set<UUID> sessionDismissedPlayers = ConcurrentHashMap.newKeySet();
    private final File dataFolder;

    public boolean isSessionDismissed(UUID uuid) {
        return sessionDismissedPlayers.contains(uuid);
    }

    public void setSessionDismissed(UUID uuid, boolean dismissed) {
        if (dismissed) {
            sessionDismissedPlayers.add(uuid);
        } else {
            sessionDismissedPlayers.remove(uuid);
        }
    }

    public PetManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        startPetTickTask();
    }

    private void startPetTickTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (ActivePet pet : activePets.values()) {
                pet.tick();
            }
        }, 1L, 2L); // Run every 2 ticks

        // Online-only upgrade cooldown ticker (every 1 second = 20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PetData data = petDataCache.get(player.getUniqueId());
                if (data != null && data.getUpgradeCooldownRemainingSeconds() > 0) {
                    data.decrementUpgradeCooldown(1);
                }
            }
        }, 20L, 20L);
    }

    public PetData getPetData(UUID playerUuid) {
        return petDataCache.computeIfAbsent(playerUuid, this::loadPetDataFromFile);
    }

    public ActivePet getActivePet(UUID playerUuid) {
        return activePets.get(playerUuid);
    }

    public Map<UUID, ActivePet> getActivePets() {
        return activePets;
    }

    public boolean isPetSummoned(UUID playerUuid) {
        return activePets.containsKey(playerUuid);
    }

    public boolean summonPet(Player player) {
        UUID uuid = player.getUniqueId();
        PetData data = getPetData(uuid);

        if (data.isTraining()) {
            ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("altar-already-training"));
            return false;
        }

        despawnPet(uuid);

        ActivePet activePet = new ActivePet(plugin, player, data);
        activePets.put(uuid, activePet);
        data.setSummoned(true);
        sessionDismissedPlayers.remove(uuid);

        ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("pet-summoned"));
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.7f, 1.4f);
        com.leftycraft.leftypet.util.PetSoundUtil.playSummonSound(player, data.getSkinKey());
        return true;
    }

    public void despawnPet(UUID playerUuid) {
        ActivePet pet = activePets.remove(playerUuid);
        if (pet != null) {
            pet.despawn();
        }
        PetData data = petDataCache.get(playerUuid);
        if (data != null) {
            data.setSummoned(false);
        }
    }

    public boolean feedPet(Player player, ItemStack item) {
        ActivePet pet = activePets.get(player.getUniqueId());
        if (pet == null) return false;

        Material mat = item.getType();
        double restore = plugin.getConfigManager().getFoodRestore(mat);
        if (restore <= 0.0) return false;

        PetData data = pet.getData();
        if (data.getEnergy() >= 100.0) {
            ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") + "<yellow>ᴘᴇᴛ ᴋᴀᴍᴜ sᴜᴅᴀʜ sᴀɴɢᴀᴛ ᴋᴇɴʏᴀɴɢ!</yellow>");
            return true;
        }

        boolean wasFainted = data.isFainted();
        item.setAmount(item.getAmount() - 1);
        data.addEnergy(restore);
        pet.updateNameTag();

        String msg = plugin.getConfigManager().getMessage("pet-fed")
                .replace("{amount}", String.valueOf((int) restore))
                .replace("{current}", String.valueOf((int) data.getEnergy()));
        ColorUtil.sendMessage(player, msg);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.7f, 1.2f);

        if (wasFainted && !data.isFainted()) {
            ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#43e97b:#38f9d7>✨ <b>Pet kamu telah sadar dari pingsan!</b> Siap mendampingi petualanganmu kembali.</gradient>");
            if (pet.getDisplayEntity() != null && pet.getDisplayEntity().isValid()) {
                org.bukkit.Location loc = pet.getDisplayEntity().getLocation().add(0, 0.4, 0);
                loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 8, 0.25, 0.25, 0.25, 0.05);
                com.leftycraft.leftypet.util.PetSoundUtil.playHappySound(player, loc, data.getSkinKey());
            }
        }
        return true;
    }

    public PetData loadPetDataFromFile(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");

        // Determine the player's name for the default pet name
        String playerName = null;
        org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() != null) {
            playerName = offlinePlayer.getName();
        }

        PetData data = new PetData(uuid, playerName);

        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String savedName = cfg.getString("name", null);
            if (savedName != null) {
                data.setName(savedName);
            }
            data.setLevel(cfg.getInt("level", 1));
            data.setEnergy(cfg.getDouble("energy", 100.0));
            try {
                data.setPetClass(PetClass.valueOf(cfg.getString("class", "FIGHTER")));
            } catch (Exception e) {
                data.setPetClass(PetClass.FIGHTER);
            }
            data.setSkinKey(cfg.getString("skin", "spirit"));
            data.setTrailKey(cfg.getString("trail", "FLAME"));
            data.setTraining(cfg.getBoolean("is-training", false));
            String altarStr = cfg.getString("altar-id", null);
            if (altarStr != null) {
                try {
                    data.setCurrentAltarId(UUID.fromString(altarStr));
                } catch (Exception ignored) {}
            }
            data.setLastAltarClaimDate(cfg.getString("last-altar-claim-date", ""));
            data.setDailyAltarClaims(cfg.getInt("daily-altar-claims", 0));
            data.setAutoAttack(cfg.getBoolean("auto-attack", true));
            data.setUpgradeCooldownRemainingSeconds(cfg.getInt("upgrade-cooldown-seconds", 0));
        }
        return data;
    }

    public void savePetData(UUID uuid) {
        PetData data = petDataCache.get(uuid);
        if (data == null) return;

        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("name", data.getName());
        cfg.set("level", data.getLevel());
        cfg.set("energy", data.getEnergy());
        cfg.set("class", data.getPetClass().name());
        cfg.set("skin", data.getSkinKey());
        cfg.set("trail", data.getTrailKey());
        cfg.set("is-training", data.isTraining());
        cfg.set("altar-id", data.getCurrentAltarId() != null ? data.getCurrentAltarId().toString() : null);
        cfg.set("last-altar-claim-date", data.getLastAltarClaimDate());
        cfg.set("daily-altar-claims", data.getDailyAltarClaims());
        cfg.set("auto-attack", data.isAutoAttack());
        cfg.set("upgrade-cooldown-seconds", data.getUpgradeCooldownRemainingSeconds());

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save pet data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAllData() {
        for (UUID uuid : petDataCache.keySet()) {
            savePetData(uuid);
        }
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Map<UUID, PetData> getPetDataCache() {
        return petDataCache;
    }

    public void unloadPlayer(UUID uuid) {
        despawnPet(uuid);
        savePetData(uuid);
        petDataCache.remove(uuid);
    }
}
