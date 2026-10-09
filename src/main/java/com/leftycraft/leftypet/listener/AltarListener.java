package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.AltarCancelMenu;
import com.leftycraft.leftypet.gui.AltarMenu;
import com.leftycraft.leftypet.manager.AltarManager;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Method;
import java.util.UUID;

public class AltarListener implements Listener {

    private final LeftyPetPlugin plugin;

    public AltarListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        hookAdvancedEnchantments();
    }

    /**
     * Dynamically hook into AdvancedEnchantments internal protection event
     * so that Trench, Blast, Veinminer, Explosive, etc. cannot modify Altar blocks.
     */
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
                            if (b != null && plugin.getAltarManager().isAltarAreaOrBuilding(b.getLocation())) {
                                cancellable.setCancelled(true);
                            }
                        } catch (Throwable ignored) {}
                    }
                }, plugin, false);
                plugin.getLogger().info("Successfully hooked into AdvancedEnchantments BlockModifyEvent protection!");
            }
        } catch (ClassNotFoundException ignored) {
            // AdvancedEnchantments is not installed, no action needed
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register AdvancedEnchantments hook: " + t.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Prevent placing any normal block inside an active/building altar's 3x3x4 bounding box
        if (!plugin.getAltarManager().isAltarItem(event.getItemInHand())) {
            if (plugin.getAltarManager().isAltarAreaOrBuilding(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
            return;
        }

        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        // 1. Check land protection (RedProtect & WorldGuard)
        com.leftycraft.leftypet.hook.ProtectionHookManager.ProtectionResult protRes =
                plugin.getProtectionHookManager().canPlaceAltar(player, loc);
        if (!protRes.allowed()) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ʟᴀʜᴀɴ ᴛᴇʀʟɪɴᴅᴜɴɢɪ! ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ʙɪsᴀ ᴍᴇɴᴀʀᴏ ᴀʟᴛᴀʀ ᴅɪ ʟᴀɴᴅ ᴏʀᴀɴɢ ʟᴀɪɴ (" +
                    protRes.pluginName() + ": " + protRes.claimInfo() + ").</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // 2. Check if player already owns an active altar
        PetAltar existing = plugin.getAltarManager().getAltarByOwner(player.getUniqueId());
        if (existing != null) {
            event.setCancelled(true);
            Location exLoc = existing.getLocation();
            String worldName = exLoc.getWorld() != null ? exLoc.getWorld().getName() : "world";
            String coords = String.format("X: %d, Y: %d, Z: %d (%s)", exLoc.getBlockX(), exLoc.getBlockY(), exLoc.getBlockZ(), worldName);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇᴍɪʟɪᴋɪ 1 ᴀʟᴛᴀʀ ᴀᴋᴛɪғ ᴅɪ: </gradient><yellow>" + coords + "</yellow><gray>! ʙᴏɴɢᴋᴀʀ ᴀʟᴛᴀʀ ʟᴀᴍᴀᴍᴜ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ.</gray>"));
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

        int placedLevel = plugin.getAltarManager().getAltarItemLevel(event.getItemInHand());
        if (placedLevel == 4 && !player.hasPermission("leftypet.celestial")) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ 4 ʜᴀɴʏᴀ ʙɪsᴀ ᴅɪʟᴇᴛᴀᴋᴋᴀɴ ᴅᴀɴ ᴅɪɢᴜɴᴀᴋᴀɴ ᴏʟᴇʜ ʀᴀɴᴋ </gradient><gradient:#d946ef:#8b5cf6><b>CELESTIAL</b></gradient>!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        UUID altarId = UUID.randomUUID();
        plugin.getAltarManager().registerAltar(altarId, player.getUniqueId(), player.getName(), loc, placedLevel);

        // Build with animation (Revisi 16)
        plugin.getAltarManager().getStructureManager().buildStructureAnimated(loc, placedLevel, () -> {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ [ʟᴠ." + placedLevel + "] (3x3) ʙᴇʀʜᴀsɪʟ ᴅɪʙᴀɴɢᴜɴ! ᴋʟɪᴋ ᴋᴀɴᴀɴ ʟᴏᴅᴇsᴛᴏɴᴇ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ ᴍᴇɴᴜ.</gradient>"));
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation();

        // Prevent interacting while building
        if (plugin.getAltarManager().isBuilding(loc)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ sᴇᴅᴀɴɢ ᴅᴀʟᴀᴍ ᴘʀᴏsᴇs ᴘᴇᴍʙᴀɴɢᴜɴᴀɴ! ʜᴀʀᴀᴘ ᴛᴜɴɢɢᴜ sᴇʙᴇɴᴛᴀʀ.</gradient>"));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        PetAltar altar = plugin.getAltarManager().getAltarOfBlock(loc);
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        // 1. Prevent breaking while building
        if (plugin.getAltarManager().isBuilding(loc)) {
            event.setCancelled(true);
            event.setDropItems(false);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ sᴇᴅᴀɴɢ ᴅᴀʟᴀᴍ ᴘʀᴏsᴇs ᴘᴇᴍʙᴀɴɢᴜɴᴀɴ! ᴛɪᴅᴀᴋ ᴅᴀᴘᴀᴛ ᴅɪʜᴀɴᴄᴜʀᴋᴀɴ.</gradient>"));
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        PetAltar altar = plugin.getAltarManager().getAltarOfBlock(loc);
        if (altar != null) {
            Player player = event.getPlayer();

            // CRITICAL: Immediately cancel, suppress all drops and exp at EventPriority.LOWEST.
            // This prevents AdvancedEnchantments (Telepathy, Trench, Veinminer), EcoEnchants, etc.
            // from ever processing drops or duplicating blocks.
            event.setCancelled(true);
            event.setDropItems(false);
            event.setExpToDrop(0);

            if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ʙɪsᴀ ᴍᴇʀᴜsᴀᴋ ᴀʟᴛᴀʀ ᴍɪʟɪᴋ ᴘᴇᴍᴀɪɴ ʟᴀɪɴ!</gradient>"));
                return;
            }

            PetData ownerData = plugin.getPetManager().getPetData(altar.getOwnerUuid());
            if (ownerData != null && ownerData.isUpgradeOnCooldown()) {
                int remSec = ownerData.getUpgradeCooldownRemainingSeconds();
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ sᴇᴅᴀɴɢ ᴅᴀʟᴀᴍ ᴍᴀsᴀ ᴄᴏᴏʟᴅᴏᴡɴ ᴜᴘɢʀᴀᴅᴇ! ᴛɪᴅᴀᴋ ᴅᴀᴘᴀᴛ ᴅɪʜᴀɴᴄᴜʀᴋᴀɴ. sɪsᴀ ᴡᴀᴋᴛᴜ: </gradient><yellow>" + AltarManager.formatDuration(remSec) + "</yellow>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

            plugin.getAltarManager().dismantleAltar(player, altar);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (plugin.getAltarManager().isAltarAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getItems().clear();
        }
    }

    /**
     * Prevents pistons / sticky pistons from pushing altar blocks or pushing blocks into altar area.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getAltarManager().isAltarAreaOrBuilding(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
            Location dest = b.getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ());
            if (plugin.getAltarManager().isAltarAreaOrBuilding(dest)) {
                event.setCancelled(true);
                return;
            }
        }
        Block front = event.getBlock().getRelative(event.getDirection());
        if (plugin.getAltarManager().isAltarAreaOrBuilding(front.getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents sticky pistons from pulling altar blocks or pulling blocks into altar area.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getAltarManager().isAltarAreaOrBuilding(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
            Location dest = b.getLocation().clone().add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ());
            if (plugin.getAltarManager().isAltarAreaOrBuilding(dest)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getAltarManager().isAltarAreaOrBuilding(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getAltarManager().isAltarAreaOrBuilding(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getAltarManager().isAltarAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (plugin.getAltarManager().isAltarAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (plugin.getAltarManager().isAltarAreaOrBuilding(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
