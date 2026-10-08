package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.KitchenMenu;
import com.leftycraft.leftypet.model.PetKitchen;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Method;
import java.util.UUID;

public class KitchenListener implements Listener {

    private final LeftyPetPlugin plugin;

    public KitchenListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        hookAdvancedEnchantments();
    }

    private void hookAdvancedEnchantments() {
        try {
            Class<?> bmeClass = Class.forName("net.advancedplugins.ae.impl.utils.protection.events.BlockModifyEvent");
            if (Event.class.isAssignableFrom(bmeClass)) {
                Class<? extends Event> eventClass = bmeClass.asSubclass(Event.class);
                org.bukkit.Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.LOWEST, (listener, evt) -> {
                    if (evt instanceof Cancellable cancellable) {
                        try {
                            Method getBlockMethod = evt.getClass().getMethod("getBlock");
                            Block b = (Block) getBlockMethod.invoke(evt);
                            if (b != null && plugin.getKitchenManager().isKitchenAreaOrBuilding(b.getLocation())) {
                                cancellable.setCancelled(true);
                            }
                        } catch (Throwable ignored) {}
                    }
                }, plugin, false);
            }
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getKitchenManager().isKitchenItem(event.getItemInHand())) {
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        PetKitchen existing = plugin.getKitchenManager().getKitchenByOwner(player.getUniqueId());
        if (existing != null) {
            event.setCancelled(true);
            Location exLoc = existing.getLocation();
            String worldName = exLoc.getWorld() != null ? exLoc.getWorld().getName() : "world";
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇᴍɪʟɪᴋɪ 1 ᴅᴀᴘᴜʀ ᴍʙɢ ᴀᴋᴛɪғ ᴅɪ: </gradient><yellow>X:" + exLoc.getBlockX() + " Y:" + exLoc.getBlockY() + " Z:" + exLoc.getBlockZ() + " (" + worldName + ")</yellow>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        if (!plugin.getKitchenManager().getStructureManager().canPlaceStructure(loc)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʀᴇᴀ 3x3x4 ᴛᴇʀʜᴀʟᴀɴɢ! ᴘᴀsᴛɪᴋᴀɴ ᴀʀᴇᴀ ᴅɪ sᴇᴋɪᴛᴀʀ ᴅᴀᴘᴜʀ ʀᴀᴛᴀ ᴅᴀɴ ʙᴇʀsɪʜ ᴅᴀʀɪ ʀɪɴᴛᴀɴɢᴀɴ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        int placedLevel = plugin.getKitchenManager().getKitchenItemLevel(event.getItemInHand());
        UUID kitchenId = UUID.randomUUID();
        plugin.getKitchenManager().registerKitchen(kitchenId, player.getUniqueId(), player.getName(), loc, placedLevel);

        plugin.getKitchenManager().getStructureManager().buildStructureAnimated(loc, placedLevel, () -> {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#43e97b:#38f9d7>ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ." + placedLevel + "] (3x3x4) ʙᴇʀʜᴀsɪʟ ᴅɪʙᴀɴɢᴜɴ! ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴋᴏᴍᴘᴏʀ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ ᴍᴇɴᴜ.</gradient>"));
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();
        if (plugin.getKitchenManager().isBuilding(loc)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴅᴀᴘᴜʀ sᴇᴅᴀɴɢ ᴅᴀʟᴀᴍ ᴘʀᴏsᴇs ᴘᴇᴍʙᴀɴɢᴜɴᴀɴ! ʜᴀʀᴀᴘ ᴛᴜɴɢɢᴜ sᴇʙᴇɴᴛᴀʀ.</gradient>"));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        PetKitchen kitchen = plugin.getKitchenManager().getKitchenOfBlock(loc);
        if (kitchen == null) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴɪ ʙᴜᴋᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ ᴍɪʟɪᴋᴍᴜ!</gradient>"));
            return;
        }

        KitchenMenu.open(player, kitchen, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (plugin.getKitchenManager().isBuilding(loc)) {
            event.setCancelled(true);
            event.setDropItems(false);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴅᴀᴘᴜʀ sᴇᴅᴀɴɢ ᴅᴀʟᴀᴍ ᴘʀᴏsᴇs ᴘᴇᴍʙᴀɴɢᴜɴᴀɴ! ᴛɪᴅᴀᴋ ᴅᴀᴘᴀᴛ ᴅɪʜᴀɴᴄᴜʀᴋᴀɴ.</gradient>"));
            return;
        }

        PetKitchen kitchen = plugin.getKitchenManager().getKitchenOfBlock(loc);
        if (kitchen != null) {
            Player player = event.getPlayer();

            // CRITICAL: Immediately cancel, suppress all drops and exp at EventPriority.LOWEST
            event.setCancelled(true);
            event.setDropItems(false);
            event.setExpToDrop(0);

            if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ʙɪsᴀ ᴍᴇʀᴜsᴀᴋ ᴅᴀᴘᴜʀ ᴍɪʟɪᴋ ᴘᴇᴍᴀɪɴ ʟᴀɪɴ!</gradient>"));
                return;
            }

            // Open menu directly so player can choose to claim or dismantle
            KitchenMenu.open(player, kitchen, plugin);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (plugin.getKitchenManager().isKitchenAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getItems().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
            Location dest = b.getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ());
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(dest)) {
                event.setCancelled(true);
                return;
            }
        }
        Block front = event.getBlock().getRelative(event.getDirection());
        if (plugin.getKitchenManager().isKitchenAreaOrBuilding(front.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
            Location dest = b.getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ());
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(dest)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getKitchenManager().isKitchenAreaOrBuilding(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getKitchenManager().isKitchenAreaOrBuilding(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getKitchenManager().isKitchenAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (plugin.getKitchenManager().isKitchenAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (plugin.getKitchenManager().isKitchenAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
