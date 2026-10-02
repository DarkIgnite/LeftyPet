package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.AltarCancelMenu;
import com.leftycraft.leftypet.gui.AltarMenu;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.util.ColorUtil;
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

import java.util.UUID;

public class AltarListener implements Listener {

    private final LeftyPetPlugin plugin;

    public AltarListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getAltarManager().isAltarItem(event.getItemInHand())) return;

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        // Check if player already owns an altar in the world
        PetAltar existing = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (existing != null) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371><b>[LeftyPet]</b> Kamu sudah memiliki 1 Altar aktif! Bongkar altar lamamu terlebih dahulu.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Check 3x3x4 footprint clearance
        if (!plugin.getAltarManager().getStructureManager().canPlaceStructure(loc)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371><b>[LeftyPet]</b> Area 3x3x4 terhalang! Pastikan area di sekitar altar rata dan bersih dari rintangan.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Register and build 3x3 Level 1 structure
        UUID altarId = UUID.randomUUID();
        plugin.getAltarManager().registerAltar(altarId, player.getUniqueId(), loc, 1);
        plugin.getAltarManager().getStructureManager().buildStructure(loc, 1);

        player.sendMessage(ColorUtil.component("<gradient:#43e97b:#38f9d7><b>[LeftyPet]</b> Pet Training Altar (3x3) berhasil dibangun! Klik kanan lodestone di tengah untuk membuka menu Altar.</gradient>"));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        PetAltar altar = plugin.getAltarManager().getAltarAt(loc);

        // If not clicked directly on Lodestone, check if part of structure
        if (altar == null) {
            // Find if clicked block is part of an active altar structure
            for (Location cLoc : plugin.getAltarManager().getAltarsMap().keySet()) {
                if (plugin.getAltarManager().getStructureManager().isPartOfStructure(cLoc, loc)) {
                    altar = plugin.getAltarManager().getAltarAt(cLoc);
                    break;
                }
            }
        }

        if (altar == null) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371><b>[LeftyPet]</b> Ini bukan altar milikmu!</gradient>"));
            return;
        }

        // If finished -> Claim
        if (altar.isFinished()) {
            plugin.getAltarManager().claimTraining(player, altar);
            return;
        }

        // If in-progress -> Open Cancel Confirmation GUI (Revisi 3)
        if (altar.isTraining()) {
            AltarCancelMenu.open(player, altar, plugin);
            return;
        }

        // If idle -> Open Altar Management GUI (Revisi 5)
        AltarMenu.open(player, altar, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        PetAltar altar = plugin.getAltarManager().getAltarAt(loc);
        if (altar == null) {
            for (Location cLoc : plugin.getAltarManager().getAltarsMap().keySet()) {
                if (plugin.getAltarManager().getStructureManager().isPartOfStructure(cLoc, loc)) {
                    altar = plugin.getAltarManager().getAltarAt(cLoc);
                    break;
                }
            }
        }

        if (altar != null) {
            Player player = event.getPlayer();
            event.setCancelled(true);

            if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
                player.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371><b>[LeftyPet]</b> Kamu tidak bisa merusak altar milik pemain lain!</gradient>"));
                return;
            }

            // Dismantle cleanly
            plugin.getAltarManager().dismantleAltar(player, altar);
        }
    }
}
