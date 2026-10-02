package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final LeftyPetPlugin plugin;
    private Economy economy = null;

    public EconomyManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found. Pet upgrades will not charge money.");
            return;
        }
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                plugin.getLogger().info("Vault economy hooked successfully: " + economy.getName());
            } else {
                plugin.getLogger().warning("Vault found but no Economy provider registered.");
            }
        } catch (Throwable e) {
            plugin.getLogger().warning("Error hooking Vault: " + e.getMessage());
        }
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(Player player) {
        if (!hasEconomy()) return 0.0;
        return economy.getBalance(player);
    }

    public boolean hasEnough(Player player, double amount) {
        if (!hasEconomy() || amount <= 0.0) return true;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!hasEconomy() || amount <= 0.0) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        if (!hasEconomy() || amount <= 0.0) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (hasEconomy()) {
            try {
                return economy.format(amount);
            } catch (Throwable ignored) {}
        }
        return String.format("%,.0f", amount);
    }
}
