package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.model.PetData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AltarListener implements Listener {

    private final LeftyPetPlugin plugin;

    public AltarListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getAltarManager().isAltarItem(event.getItemInHand())) {
            Player player = event.getPlayer();
            player.sendMessage(plugin.getConfigManager().getMessage("altar-placed"));
            player.playSound(event.getBlock().getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.4f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LODESTONE) return;

        Player player = event.getPlayer();
        Location loc = block.getLocation();
        PetAltar altar = plugin.getAltarManager().getAltarAt(loc);

        if (altar != null) {
            event.setCancelled(true);
            if (altar.getOwnerUuid().equals(player.getUniqueId())) {
                if (altar.isFinished()) {
                    plugin.getAltarManager().claimTraining(player, altar);
                } else {
                    String msg = plugin.getConfigManager().getMessage("altar-in-progress")
                            .replace("{time}", altar.getFormattedRemainingTime());
                    player.sendMessage(msg);
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cAltar ini sedang digunakan oleh player lain!");
            }
            return;
        }

        // Check if player has pet summoned to start training
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet != null && pet.isValid()) {
            event.setCancelled(true);
            plugin.getAltarManager().startTraining(player, loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LODESTONE) return;

        Location loc = block.getLocation();
        PetAltar altar = plugin.getAltarManager().getAltarAt(loc);
        if (altar != null) {
            Player player = event.getPlayer();
            if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§cKamu tidak bisa menghancurkan altar milik player lain!");
                return;
            }

            // Cancel training
            PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());
            data.setTraining(false);
            data.setCurrentAltarId(null);

            plugin.getAltarManager().removeAltar(loc);

            // Drop Altar item back
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(loc.add(0.5, 0.5, 0.5), plugin.getAltarManager().createAltarItem());
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    "§eTraining dibatalkan karena Altar dihancurkan!");
        }
    }
}
