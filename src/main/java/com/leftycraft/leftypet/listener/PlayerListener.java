package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final LeftyPetPlugin plugin;

    public PlayerListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Pre-load pet data and auto-summon unless dismissed in current server session
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
            boolean isDismissed = plugin.getPetManager().isSessionDismissed(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    com.leftycraft.leftypet.model.PetKitchen kitchen = (plugin.getKitchenManager() != null) ?
                            plugin.getKitchenManager().getKitchenByOwner(player.getUniqueId()) : null;
                    boolean isKitchenAssigned = (kitchen != null && kitchen.isPetAssigned());
                    if (!isDismissed && !data.isTraining() && !isKitchenAssigned) {
                        plugin.getPetManager().summonPet(player);
                    }
                }
            });
        });

        // Refresh altar display if player owns an altar
        com.leftycraft.leftypet.model.PetAltar altar = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (altar != null) {
            altar.setCachedOwnerName(player.getName());
            plugin.getAltarManager().updateAltarHologram(altar);
        }

        // Update Bedrock/Java visibility for existing pets & altars
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                for (ActivePet pet : plugin.getPetManager().getActivePets().values()) {
                    pet.updateVisibilityFor(player);
                }
                for (com.leftycraft.leftypet.model.PetAltar a : plugin.getAltarManager().getAltars().values()) {
                    plugin.getAltarManager().updateAltarHeadVisibilityFor(a, player);
                }
                for (com.leftycraft.leftypet.model.PetKitchen k : plugin.getKitchenManager().getKitchensMap().values()) {
                    plugin.getKitchenManager().updateChefVisibilityFor(k, player);
                }
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPetManager().unloadPlayer(player.getUniqueId());

        com.leftycraft.leftypet.model.PetAltar altar = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (altar != null) {
            plugin.getAltarManager().saveAltars();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getAltarManager().updateAltarHologram(altar);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        if (data != null) {
            data.setEnergy(0.0); // Pet otomatis pingsan saat pemilik gugur
        }
        // Despawn pet on death to prevent weird ghost positioning
        plugin.getPetManager().despawnPet(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                if (!data.isTraining()) {
                    plugin.getPetManager().summonPet(player);
                    ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>Pet kamu pingsan karena kematianmu! Beri makan untuk memulihkannya.</gradient>");
                }
            }
        }, 15L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet != null) {
            plugin.getPetManager().summonPet(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet != null) {
            if (event.getFrom().getWorld() != event.getTo().getWorld() || event.getFrom().distanceSquared(event.getTo()) > 256.0) {
                plugin.getPetManager().summonPet(player);
            }
        }
    }
}
