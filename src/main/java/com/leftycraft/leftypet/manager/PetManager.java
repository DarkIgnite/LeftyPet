package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
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
    private final File dataFolder;

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
    }

    public PetData getPetData(UUID playerUuid) {
        return petDataCache.computeIfAbsent(playerUuid, this::loadPetDataFromFile);
    }

    public ActivePet getActivePet(UUID playerUuid) {
        return activePets.get(playerUuid);
    }

    public boolean isPetSummoned(UUID playerUuid) {
        return activePets.containsKey(playerUuid);
    }

    public boolean summonPet(Player player) {
        UUID uuid = player.getUniqueId();
        PetData data = getPetData(uuid);

        if (data.isTraining()) {
            player.sendMessage(plugin.getConfigManager().getMessage("altar-already-training"));
            return false;
        }

        despawnPet(uuid);

        ActivePet activePet = new ActivePet(plugin, player, data);
        activePets.put(uuid, activePet);
        data.setSummoned(true);

        player.sendMessage(plugin.getConfigManager().getMessage("pet-summoned"));
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.7f, 1.4f);
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
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§ePet kamu sudah sangat kenyang!");
            return true;
        }

        item.setAmount(item.getAmount() - 1);
        data.addEnergy(restore);
        pet.updateNameTag();

        String msg = plugin.getConfigManager().getMessage("pet-fed")
                .replace("{amount}", String.valueOf((int) restore))
                .replace("{current}", String.valueOf((int) data.getEnergy()));
        player.sendMessage(msg);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.7f, 1.2f);
        return true;
    }

    public PetData loadPetDataFromFile(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        PetData data = new PetData(uuid);

        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            data.setName(cfg.getString("name", "&bSpirit Companion"));
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

    public void unloadPlayer(UUID uuid) {
        despawnPet(uuid);
        savePetData(uuid);
        petDataCache.remove(uuid);
    }
}
