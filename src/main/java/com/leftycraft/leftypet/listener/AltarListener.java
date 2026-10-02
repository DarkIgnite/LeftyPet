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
import org.bukkit.event.entity.EntityDismountEvent;
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

        // Check if player already owns an active altar
        PetAltar existing = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (existing != null) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇᴍɪʟɪᴋɪ 1 ᴀʟᴛᴀʀ ᴀᴋᴛɪғ! ʙᴏɴɢᴋᴀʀ ᴀʟᴛᴀʀ ʟᴀᴍᴀᴍᴜ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Check 3x3x4 footprint clearance
        if (!plugin.getAltarManager().getStructureManager().canPlaceStructure(loc)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ᴀʀᴇᴀ 3x3x4 ᴛᴇʀʜᴀʟᴀɴɢ! ᴘᴀsᴛɪᴋᴀɴ ᴀʀᴇᴀ ᴅɪ sᴇᴋɪᴛᴀʀ ᴀʟᴛᴀʀ ʀᴀᴛᴀ ᴅᴀɴ ʙᴇʀsɪʜ ᴅᴀʀɪ ʀɪɴᴛᴀɴɢᴀɴ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        UUID altarId = UUID.randomUUID();
        plugin.getAltarManager().registerAltar(altarId, player.getUniqueId(), loc, 1);

        // Build with animation (Revisi 16)
        plugin.getAltarManager().getStructureManager().buildStructureAnimated(loc, 1, () -> {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3) ʙᴇʀʜᴀsɪʟ ᴅɪʙᴀɴɢᴜɴ! ᴋʟɪᴋ ᴋᴀɴᴀɴ ʟᴏᴅᴇsᴛᴏɴᴇ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ ᴍᴇɴᴜ.</gradient>"));
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

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

        if (altar == null) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴɪ ʙᴜᴋᴀɴ ᴀʟᴛᴀʀ ᴍɪʟɪᴋᴍᴜ!</gradient>"));
            return;
        }

        if (altar.isFinished()) {
            plugin.getAltarManager().claimTraining(player, altar);
            return;
        }

        if (altar.isTraining()) {
            AltarCancelMenu.open(player, altar, plugin);
            return;
        }

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
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ʙɪsᴀ ᴍᴇʀᴜsᴀᴋ ᴀʟᴛᴀʀ ᴍɪʟɪᴋ ᴘᴇᴍᴀɪɴ ʟᴀɪɴ!</gradient>"));
                return;
            }

            plugin.getAltarManager().dismantleAltar(player, altar);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getMountManager().stopMount(player);
        }
    }
}
