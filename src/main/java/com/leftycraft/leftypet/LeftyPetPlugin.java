package com.leftycraft.leftypet;

import com.leftycraft.leftypet.command.PetAdminCommand;
import com.leftycraft.leftypet.command.PetCommand;
import com.leftycraft.leftypet.config.ConfigManager;
import com.leftycraft.leftypet.listener.*;
import com.leftycraft.leftypet.manager.AltarManager;
import com.leftycraft.leftypet.manager.CombatManager;
import com.leftycraft.leftypet.manager.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeftyPetPlugin extends JavaPlugin {

    private static LeftyPetPlugin instance;

    private ConfigManager configManager;
    private PetManager petManager;
    private CombatManager combatManager;
    private AltarManager altarManager;
    private com.leftycraft.leftypet.manager.EconomyManager economyManager;
    private com.leftycraft.leftypet.manager.PetDuelManager petDuelManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Load Configurations
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 2. Initialize Managers
        economyManager = new com.leftycraft.leftypet.manager.EconomyManager(this);
        petManager = new PetManager(this);
        combatManager = new CombatManager(this);
        altarManager = new AltarManager(this);
        petDuelManager = new com.leftycraft.leftypet.manager.PetDuelManager(this);

        // 3. Register Listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new PetInteractListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new AltarListener(this), this);

        // 4. Register Commands
        PetCommand petCommand = new PetCommand(this);
        if (getCommand("pet") != null) {
            getCommand("pet").setExecutor(petCommand);
            getCommand("pet").setTabCompleter(petCommand);
        }

        PetAdminCommand adminCommand = new PetAdminCommand(this);
        if (getCommand("leftypet") != null) {
            getCommand("leftypet").setExecutor(adminCommand);
            getCommand("leftypet").setTabCompleter(adminCommand);
        }

        getLogger().info("=========================================");
        getLogger().info("  LeftyPet v" + getDescription().getVersion() + " by DarkIgnite enabled!");
        getLogger().info("  Leaves / Paper 1.21.8 Ready.");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down LeftyPet...");

        // Save & Cleanup all entities to prevent ghost entities
        if (petManager != null) {
            for (var player : Bukkit.getOnlinePlayers()) {
                petManager.despawnPet(player.getUniqueId());
            }
            petManager.saveAllData();
        }

        if (altarManager != null) {
            altarManager.removeAllEntities();
            altarManager.saveAltars();
        }

        if (petDuelManager != null) {
            petDuelManager.cleanupAll();
        }

        getLogger().info("LeftyPet disabled successfully.");
    }

    public static LeftyPetPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PetManager getPetManager() {
        return petManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public AltarManager getAltarManager() {
        return altarManager;
    }

    public com.leftycraft.leftypet.manager.EconomyManager getEconomyManager() {
        return economyManager;
    }

    public com.leftycraft.leftypet.manager.PetDuelManager getPetDuelManager() {
        return petDuelManager;
    }
}
