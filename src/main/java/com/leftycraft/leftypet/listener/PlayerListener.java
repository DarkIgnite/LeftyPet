package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetData;
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
            if (!isDismissed && !data.isTraining()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getPetManager().summonPet(player);
                    }
                });
            }
        });

        // Refresh altar display if player owns an altar
        com.leftycraft.leftypet.model.PetAltar altar = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (altar != null) {
            plugin.getAltarManager().updateAltarHologram(altar);
        }
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
        if (pet != null && pet.isValid()) {
            if (event.getFrom().getWorld() != event.getTo().getWorld()) {
                plugin.getPetManager().summonPet(player);
            } else if (event.getFrom().distanceSquared(event.getTo()) > 256.0) {
                pet.getDisplayEntity().teleport(event.getTo().clone().add(0, 1.5, 0));
                pet.getNameTagDisplay().teleport(event.getTo().clone().add(0, 2.1, 0));
            }
        }
    }
}
