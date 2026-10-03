package com.leftycraft.leftypet.config;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetSkin;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final LeftyPetPlugin plugin;

    private float petScale = 1.35f;
    private int interpolationDuration = 3;
    private int maxLevel = 10;

    private final Map<Integer, Integer> upgradeDurations = new HashMap<>();
    private final Map<Material, Double> foodRestoreMap = new EnumMap<>(Material.class);
    private final Map<PetClass, Double> classDamageMultiplier = new EnumMap<>(PetClass.class);
    private final Map<PetClass, Double> classSpeedMultiplier = new EnumMap<>(PetClass.class);
    private final Map<String, PetSkin> skins = new LinkedHashMap<>();
    private final Map<String, String> trails = new LinkedHashMap<>();

    private double energyDrainPerAttack = 0.3;
    private boolean economyEnabled = true;
    private double upgradeBaseCost = 1000.0;
    private double upgradeCostPerLevel = 500.0;

    public ConfigManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        petScale = (float) config.getDouble("settings.pet-scale", 1.35);
        interpolationDuration = config.getInt("settings.interpolation-duration", 3);
        maxLevel = config.getInt("settings.max-level", 10);

        // Economy
        economyEnabled = config.getBoolean("economy.enabled", true);
        upgradeBaseCost = config.getDouble("economy.base-cost", 1000.0);
        upgradeCostPerLevel = config.getDouble("economy.cost-per-level", 500.0);

        // Upgrade durations
        upgradeDurations.clear();
        ConfigurationSection durSection = config.getConfigurationSection("upgrade-durations");
        if (durSection != null) {
            for (String key : durSection.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);
                    int seconds = durSection.getInt(key);
                    upgradeDurations.put(lvl, seconds);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Energy settings
        energyDrainPerAttack = config.getDouble("energy.drain-per-attack", 0.3);

        foodRestoreMap.clear();
        ConfigurationSection foodSec = config.getConfigurationSection("energy.food-restore");
        if (foodSec != null) {
            for (String key : foodSec.getKeys(false)) {
                try {
                    Material mat = Material.matchMaterial(key);
                    if (mat != null) {
                        foodRestoreMap.put(mat, foodSec.getDouble(key));
                    }
                } catch (Exception ignored) {}
            }
        }

        // Classes multipliers
        for (PetClass pc : PetClass.values()) {
            double dmg = config.getDouble("classes." + pc.name() + ".damage-multiplier", 1.0);
            double spd = config.getDouble("classes." + pc.name() + ".speed-multiplier", 1.0);
            classDamageMultiplier.put(pc, dmg);
            classSpeedMultiplier.put(pc, spd);
        }

        // Skins
        skins.clear();
        ConfigurationSection skinSec = config.getConfigurationSection("skins");
        if (skinSec != null) {
            for (String key : skinSec.getKeys(false)) {
                String dName = skinSec.getString(key + ".display-name", key);
                String tex = skinSec.getString(key + ".texture", "");
                String perm = skinSec.getString(key + ".permission", null);
                skins.put(key, new PetSkin(key, dName, tex, perm));
            }
        }

        // Trails
        trails.clear();
        ConfigurationSection trailSec = config.getConfigurationSection("trails");
        if (trailSec != null) {
            for (String key : trailSec.getKeys(false)) {
                trails.put(key, trailSec.getString(key + ".display-name", key));
            }
        }
    }

    public float getPetScale() {
        return petScale;
    }

    public int getInterpolationDuration() {
        return interpolationDuration;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getUpgradeDuration(int currentLevel) {
        if (upgradeDurations.containsKey(currentLevel)) {
            return upgradeDurations.get(currentLevel);
        }
        if (currentLevel > 10) {
            return 5400 + ((currentLevel - 10) * 1800);
        }
        return 30 * currentLevel;
    }

    public double getFoodRestore(Material material) {
        return foodRestoreMap.getOrDefault(material, 0.0);
    }

    public double getClassDamageMultiplier(PetClass petClass) {
        return classDamageMultiplier.getOrDefault(petClass, 1.0);
    }

    public double getClassSpeedMultiplier(PetClass petClass) {
        return classSpeedMultiplier.getOrDefault(petClass, 1.0);
    }

    public double getEnergyDrainPerAttack() {
        return energyDrainPerAttack;
    }

    public Map<String, PetSkin> getSkins() {
        return Collections.unmodifiableMap(skins);
    }

    public PetSkin getSkin(String key) {
        return skins.getOrDefault(key, skins.values().stream().findFirst().orElse(null));
    }

    public Map<String, String> getTrails() {
        return Collections.unmodifiableMap(trails);
    }

    public String getMessage(String path) {
        String prefix = plugin.getConfig().getString("messages.prefix", "<gradient:#00f2fe:#4facfe><b>[ʟᴇғᴛʏᴘᴇᴛ]</b></gradient> ");
        if ("prefix".equalsIgnoreCase(path)) {
            return prefix;
        }
        String msg = plugin.getConfig().getString("messages." + path, "");
        if (msg == null || msg.isEmpty()) {
            return prefix;
        }
        if (msg.contains("LeftyPet") || msg.contains("ʟᴇғᴛʏᴘᴇᴛ")) {
            return msg;
        }
        return prefix + msg;
    }

    public String getRawMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "");
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public double getUpgradeBaseCost() {
        return upgradeBaseCost;
    }

    public double getUpgradeCostPerLevel() {
        return upgradeCostPerLevel;
    }

    public double getUpgradeCost(int currentLevel) {
        if (!economyEnabled) return 0.0;
        return upgradeBaseCost + (currentLevel * upgradeCostPerLevel);
    }
}
