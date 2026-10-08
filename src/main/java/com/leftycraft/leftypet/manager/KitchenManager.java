package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.model.PetKitchen;
import com.leftycraft.leftypet.model.PetSkin;
import com.leftycraft.leftypet.util.BedrockUtil;
import com.leftycraft.leftypet.util.ColorUtil;
import com.leftycraft.leftypet.util.HeadUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitchenManager {

    private final LeftyPetPlugin plugin;
    private final KitchenStructureManager structureManager;
    private final Map<Location, PetKitchen> kitchens = new HashMap<>();
    private final Map<Location, PetKitchen> blockToKitchen = new HashMap<>();
    private final NamespacedKey kitchenItemKey;
    private final File kitchenFile;

    public KitchenManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.structureManager = new KitchenStructureManager(plugin);
        this.kitchenItemKey = new NamespacedKey(plugin, "kitchen_building_item");
        this.kitchenFile = new File(plugin.getDataFolder(), "kitchens.yml");

        loadKitchens();
        startKitchenTicker();
    }

    public KitchenStructureManager getStructureManager() {
        return structureManager;
    }

    public Map<Location, PetKitchen> getKitchensMap() {
        return Collections.unmodifiableMap(kitchens);
    }

    public PetKitchen getKitchenAt(Location loc) {
        if (loc == null) return null;
        return kitchens.get(loc.getBlock().getLocation());
    }

    public PetKitchen getKitchenOfBlock(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Location bLoc = loc.getBlock().getLocation();
        PetKitchen direct = kitchens.get(bLoc);
        if (direct != null) return direct;
        return blockToKitchen.get(bLoc);
    }

    public boolean isKitchenAreaOrBuilding(Location loc) {
        return getKitchenOfBlock(loc) != null;
    }

    public PetKitchen getKitchenByOwner(UUID ownerUuid) {
        for (PetKitchen k : kitchens.values()) {
            if (k.getOwnerUuid().equals(ownerUuid)) {
                return k;
            }
        }
        return null;
    }

    public ItemStack createKitchenItem() {
        ItemStack item = new ItemStack(Material.SMOKER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component("<gradient:#4facfe:#00f2fe><b>ᴅᴀᴘᴜʀ ᴍʙɢ (ʙᴜɪʟᴅɪɴɢ)</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ & ᴋᴀᴛᴇʀɪɴɢ ᴏᴛᴏᴍᴀᴛɪs (14x7x13)!"));
            lore.add(ColorUtil.component("&7ᴘᴇᴛ ᴋᴀᴍᴜ ᴀᴋᴀɴ ʙᴇᴋᴇʀᴊᴀ sᴇʙᴀɢᴀɪ ᴋᴏᴋɪ:"));
            lore.add(ColorUtil.component("&e• 30s ᴍᴀsᴀᴋ ᴅɪ ғᴜʀɴᴀᴄᴇ"));
            lore.add(ColorUtil.component("&b• 30s ᴍᴇɴɢᴇᴍᴀs ᴅɪ ᴍᴇᴊᴀ"));
            lore.add(ColorUtil.component("&a• 5s sᴇʀᴀʜᴋᴀɴ ᴘᴇsᴀɴᴀɴ ᴅɪ ᴊᴇɴᴅᴇʟᴀ"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7• ʜᴀsɪʟ: &a+$100 &7ᴘᴇʀ ᴘᴇsᴀɴᴀɴ (ᴅɪsɪᴍᴘᴀɴ ᴅɪ ᴋᴀsɪʀ)"));
            lore.add(ColorUtil.component("&7• ᴋᴏɴsᴜᴍsɪ: &c-10% ᴇɴᴇʀɢɪ ᴘᴇᴛ &7ᴘᴇʀ ᴘᴇsᴀɴᴀɴ"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&a▶ ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴛᴀɴᴀʜ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴀɴɢᴜɴ ɢᴇᴅᴜɴɢ!"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(kitchenItemKey, PersistentDataType.BOOLEAN, true);
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isKitchenItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(kitchenItemKey, PersistentDataType.BOOLEAN);
    }

    public boolean claimKitchenItem(Player player) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<red>ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ ᴘᴇɴᴜʜ! ᴋᴏsᴏɴɢᴋᴀɴ sᴇᴛɪᴅᴀᴋɴʏᴀ 1 sʟᴏᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return false;
        }

        PetKitchen existing = getKitchenByOwner(player.getUniqueId());
        if (existing != null) {
            Location exLoc = existing.getLocation();
            String worldName = exLoc.getWorld() != null ? exLoc.getWorld().getName() : "world";
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<red>ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇᴍɪʟɪᴋɪ 1 ᴅᴀᴘᴜʀ ᴍʙɢ ᴀᴋᴛɪғ ᴅɪ: </red><yellow>X:" + exLoc.getBlockX() + " Y:" + exLoc.getBlockY() + " Z:" + exLoc.getBlockZ() + " (" + worldName + ")</yellow>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return false;
        }

        player.getInventory().addItem(createKitchenItem());
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ᴋᴀᴍᴜ ᴍᴇɴᴇʀɪᴍᴀ 1x ʙʟᴏᴋ ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ ᴍʙɢ! ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴛᴀɴᴀʜ ᴅᴇɴɢᴀɴ ʟᴀʜᴀɴ ʟᴜᴀs.</gradient>"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.4f);
        return true;
    }

    public PetKitchen placeKitchenBuilding(Player player, Location placedLoc) {
        StructureRotation rotation = structureManager.getRotationFromYaw(player.getLocation().getYaw());
        String facing = structureManager.getFacingFromRotation(rotation);

        UUID kitchenId = UUID.randomUUID();
        Location origin = placedLoc.getBlock().getLocation();

        // Build structure
        Set<Location> placedBlocks = structureManager.buildKitchen(origin, rotation);

        PetKitchen kitchen = new PetKitchen(kitchenId, player.getUniqueId(), player.getName(), origin, rotation, facing);
        kitchen.setAllBlockLocations(placedBlocks);

        kitchens.put(origin, kitchen);
        for (Location bLoc : placedBlocks) {
            blockToKitchen.put(bLoc, kitchen);
        }

        saveKitchens();
        updateCashierHologram(kitchen);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ ᴍʙɢ ʙᴇʀʜᴀsɪʟ ᴅɪʙᴀɴɢᴜɴ (ʜᴀᴅᴀᴘ " + facing + ")!</gradient>"));
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<yellow>ᴋʟɪᴋ ᴋᴀɴᴀɴ ʙʟᴏᴋ ᴅᴀᴘᴜʀ ᴜɴᴛᴜᴋ ᴍᴇɴᴜɢᴀsᴋᴀɴ ᴘᴇᴛ ᴋᴀᴍᴜ ᴍᴇɴᴊᴀᴅɪ ᴋᴏᴋɪ!</yellow>"));

        return kitchen;
    }

    public void assignPet(Player player, PetKitchen kitchen) {
        if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɪɴɪ ʙᴜᴋᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ ᴍɪʟɪᴋᴍᴜ!</red>"));
            return;
        }

        // Dismiss active following pet so it doesn't duplicate
        if (plugin.getPetManager().isPetSummoned(player.getUniqueId())) {
            plugin.getPetManager().despawnPet(player.getUniqueId());
            plugin.getPetManager().setSessionDismissed(player.getUniqueId(), true);
        }

        kitchen.setPetAssigned(true);
        kitchen.setCurrentStation(PetKitchen.KitchenStation.COOKING);
        kitchen.setCurrentProgressSeconds(0);
        saveKitchens();

        spawnOrUpdateChefDisplay(kitchen);
        updateCashierHologram(kitchen);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ᴘᴇᴛ ᴋᴀᴍᴜ ᴍᴜʟᴀɪ ʙᴇᴋᴇʀᴊᴀ sᴇʙᴀɢᴀɪ ᴋᴏᴋɪ ᴅɪ ᴅᴀᴘᴜʀ ᴍʙɢ!</gradient>"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
    }

    public void recallPet(Player player, PetKitchen kitchen) {
        if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɪɴɪ ʙᴜᴋᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ ᴍɪʟɪᴋᴍᴜ!</red>"));
            return;
        }

        kitchen.setPetAssigned(false);
        kitchen.removeEntities();
        saveKitchens();

        updateCashierHologram(kitchen);

        // Auto re-summon pet back beside player!
        plugin.getPetManager().setSessionDismissed(player.getUniqueId(), false);
        plugin.getPetManager().summonPet(player);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ᴘᴇᴛ ᴋᴀᴍᴜ ᴅɪᴛᴀʀɪᴋ ᴋᴇᴍʙᴀʟɪ ᴅᴀɴ sɪᴀᴘ ᴍᴇɴᴇᴍᴀɴɪᴍᴜ!</gradient>"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
    }

    public void feedPetInKitchen(Player player, PetKitchen kitchen) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir() || !isFoodItem(hand.getType())) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<yellow>ᴘᴇɢᴀɴɢ ᴍᴀᴋᴀɴᴀɴ (ᴅᴀɢɪɴɢ, ʀᴏᴛɪ, ᴡᴏʀᴛᴇʟ, ᴅʟʟ) ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴇʀɪ ᴍᴀᴋᴀɴ ᴘᴇᴛ ᴋᴏᴋɪ!</yellow>"));
            return;
        }

        PetData data = plugin.getPetManager().getPetData(kitchen.getOwnerUuid());
        double currentEnergy = data.getEnergy();
        if (currentEnergy >= 100.0) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<green>ᴇɴᴇʀɢɪ ᴘᴇᴛ sᴜᴅᴀʜ ᴘᴇɴᴜʜ (100%)!</green>"));
            return;
        }

        hand.subtract(1);
        double restored = Math.min(100.0, currentEnergy + 30.0);
        data.setEnergy(restored);
        plugin.getPetManager().savePetData(kitchen.getOwnerUuid());

        if (kitchen.getCurrentStation() == PetKitchen.KitchenStation.TIRED) {
            kitchen.setCurrentStation(PetKitchen.KitchenStation.COOKING);
            kitchen.setCurrentProgressSeconds(0);
        }
        saveKitchens();

        Location chefLoc = (kitchen.getChefDisplay() != null) ? kitchen.getChefDisplay().getLocation() : player.getLocation();
        chefLoc.getWorld().spawnParticle(Particle.HEART, chefLoc.clone().add(0, 0.5, 0), 10, 0.3, 0.3, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.8f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 0.8f, 1.2f);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ᴘᴇᴛ ᴋᴏᴋɪ ᴅɪʙᴇʀɪ ᴍᴀᴋᴀɴ! ᴇɴᴇʀɢɪ: </gradient><yellow>" + (int) restored + "%</yellow>"));

        spawnOrUpdateChefDisplay(kitchen);
        updateCashierHologram(kitchen);
    }

    private boolean isFoodItem(Material material) {
        String name = material.name();
        return material.isEdible() || name.contains("BEEF") || name.contains("PORK") || name.contains("MUTTON")
                || name.contains("CHICKEN") || name.contains("BREAD") || name.contains("CARROT")
                || name.contains("POTATO") || name.contains("APPLE") || name.contains("FISH") || name.contains("SALMON");
    }

    public void claimEarnings(Player player, PetKitchen kitchen) {
        double amount = kitchen.getStoredEarnings();
        if (amount <= 0) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ʙᴇʟᴜᴍ ᴀᴅᴀ ᴜᴀɴɢ ʜᴀsɪʟ ᴘᴇsᴀɴᴀɴ ʏᴀɴɢ sɪᴀᴘ ᴅɪᴀᴍʙɪʟ ᴅɪ ᴋᴀsɪʀ!</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        plugin.getEconomyManager().deposit(player, amount);
        kitchen.setStoredEarnings(0.0);
        saveKitchens();

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ʙᴇʀʜᴀsɪʟ ᴍᴇɴᴀʀɪᴋ ᴜᴀɴɢ ᴋᴀsɪʀ: </gradient><green><b>+$" + plugin.getEconomyManager().format(amount) + "</b></green>"));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);

        updateCashierHologram(kitchen);
    }

    public void dismantleKitchen(Player player, PetKitchen kitchen) {
        if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɪɴɪ ʙᴜᴋᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ ᴍɪʟɪᴋᴍᴜ!</red>"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ ᴘᴇɴᴜʜ! ᴋᴏsᴏɴɢᴋᴀɴ sʟᴏᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Auto claim earnings if any
        if (kitchen.getStoredEarnings() > 0) {
            claimEarnings(player, kitchen);
        }

        Location origin = kitchen.getLocation();
        kitchen.removeEntities();

        // Clear all blocks
        structureManager.removeKitchen(kitchen.getAllBlockLocations());

        // Unregister
        kitchens.remove(origin);
        for (Location bLoc : kitchen.getAllBlockLocations()) {
            blockToKitchen.remove(bLoc);
        }
        saveKitchens();

        // Auto re-summon pet
        plugin.getPetManager().setSessionDismissed(player.getUniqueId(), false);
        plugin.getPetManager().summonPet(player);

        player.getInventory().addItem(createKitchenItem());
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#4facfe:#00f2fe>ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ ᴍʙɢ ʙᴇʀʜᴀsɪʟ ᴅɪʙᴏɴɢᴋᴀʀ ᴅᴀɴ ᴅɪᴋᴇᴍʙᴀʟɪᴋᴀɴ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ!</gradient>"));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.7f, 1.2f);
    }

    private void startKitchenTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PetKitchen kitchen : kitchens.values()) {
                Player owner = Bukkit.getPlayer(kitchen.getOwnerUuid());
                boolean isOnline = (owner != null && owner.isOnline());

                // Tycoon loop progresses when owner is online & pet is assigned
                if (isOnline && kitchen.isPetAssigned()) {
                    tickKitchenTycoon(kitchen, owner);
                }

                // Periodic check for displays
                if (kitchen.isPetAssigned() && !kitchen.isGliding()) {
                    spawnOrUpdateChefDisplay(kitchen);
                }
                updateCashierHologram(kitchen);
            }
        }, 20L, 20L);
    }

    private void tickKitchenTycoon(PetKitchen kitchen, Player owner) {
        if (kitchen.isGliding()) return;

        PetData data = plugin.getPetManager().getPetData(kitchen.getOwnerUuid());
        double energy = data.getEnergy();

        if (energy <= 0.0) {
            kitchen.setCurrentStation(PetKitchen.KitchenStation.TIRED);
            kitchen.setCurrentProgressSeconds(0);
            return;
        }

        PetKitchen.KitchenStation currentStation = kitchen.getCurrentStation();
        int progress = kitchen.getCurrentProgressSeconds() + 1;
        kitchen.setCurrentProgressSeconds(progress);

        Location origin = kitchen.getLocation();
        StructureRotation rotation = kitchen.getRotation();

        switch (currentStation) {
            case COOKING -> {
                Location cookLoc = structureManager.getStationLocation(origin, PetKitchen.KitchenStation.COOKING, rotation);
                if (progress % 2 == 0 && cookLoc.getWorld() != null) {
                    cookLoc.getWorld().spawnParticle(Particle.SMOKE, cookLoc.clone().add(0, 0.3, 0), 4, 0.2, 0.2, 0.2, 0.02);
                    cookLoc.getWorld().spawnParticle(Particle.FLAME, cookLoc.clone().add(0, 0.1, 0), 2, 0.1, 0.1, 0.1, 0.01);
                    if (progress % 6 == 0) {
                        cookLoc.getWorld().playSound(cookLoc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.4f, 1.2f);
                    }
                }
                if (progress >= PetKitchen.KitchenStation.COOKING.getDurationSeconds()) {
                    kitchen.setCurrentProgressSeconds(0);
                    if (cookLoc.getWorld() != null) {
                        cookLoc.getWorld().playSound(cookLoc, Sound.BLOCK_BREWING_STAND_BREW, 0.7f, 1.2f);
                    }
                    glideChefToStation(kitchen, PetKitchen.KitchenStation.PACKING);
                }
            }
            case PACKING -> {
                Location packLoc = structureManager.getStationLocation(origin, PetKitchen.KitchenStation.PACKING, rotation);
                if (progress % 2 == 0 && packLoc.getWorld() != null) {
                    packLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, packLoc.clone().add(0, 0.5, 0), 3, 0.3, 0.2, 0.3, 0.02);
                    if (progress % 6 == 0) {
                        packLoc.getWorld().playSound(packLoc, Sound.ENTITY_VILLAGER_WORK_CLERIC, 0.5f, 1.3f);
                    }
                }
                if (progress >= PetKitchen.KitchenStation.PACKING.getDurationSeconds()) {
                    kitchen.setCurrentProgressSeconds(0);
                    if (packLoc.getWorld() != null) {
                        packLoc.getWorld().playSound(packLoc, Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.7f, 1.2f);
                    }
                    glideChefToStation(kitchen, PetKitchen.KitchenStation.DELIVERY);
                }
            }
            case DELIVERY -> {
                Location delLoc = structureManager.getStationLocation(origin, PetKitchen.KitchenStation.DELIVERY, rotation);
                if (delLoc.getWorld() != null) {
                    delLoc.getWorld().spawnParticle(Particle.WAX_ON, delLoc.clone().add(0, 0.3, 0), 3, 0.2, 0.2, 0.2, 0.02);
                }
                if (progress >= PetKitchen.KitchenStation.DELIVERY.getDurationSeconds()) {
                    // ORDER COMPLETED!
                    kitchen.addEarnings(100.0);
                    kitchen.incrementCompletedOrders();
                    kitchen.setCurrentProgressSeconds(0);

                    // Deduct 10% energy
                    double newEnergy = Math.max(0.0, data.getEnergy() - 10.0);
                    data.setEnergy(newEnergy);
                    plugin.getPetManager().savePetData(owner.getUniqueId());

                    // SFX & FX
                    if (delLoc.getWorld() != null) {
                        delLoc.getWorld().playSound(delLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.6f);
                        delLoc.getWorld().playSound(delLoc, Sound.ENTITY_VILLAGER_YES, 0.7f, 1.1f);
                        delLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, delLoc.clone().add(0, 0.6, 0), 10, 0.4, 0.4, 0.4, 0.05);
                    }

                    owner.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#4facfe:#00f2fe>1x ᴘᴇsᴀɴᴀɴ ᴍʙɢ sᴇʟᴇsᴀɪ! </gradient><yellow>+$100 ᴅɪsɪᴍᴘᴀɴ ᴅɪ ᴋᴀsɪʀ</yellow> <gray>(ᴇɴᴇʀɢɪ ᴘᴇᴛ: " + (int) newEnergy + "%)</gray>"));

                    if (newEnergy <= 0.0) {
                        kitchen.setCurrentStation(PetKitchen.KitchenStation.TIRED);
                        owner.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                                "<gradient:#ff5f6d:#ffc371>ᴘᴇᴛ ᴋᴏᴋɪ ᴋᴀᴍᴜ ᴋᴇʜᴀʙɪsᴀɴ ᴇɴᴇʀɢɪ! ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴘᴇᴛ ᴅɪ ᴅᴀᴘᴜʀ sᴀᴍʙɪʟ ʙᴀᴡᴀ ᴍᴀᴋᴀɴᴀɴ.</gradient>"));
                        owner.playSound(owner.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                        spawnOrUpdateChefDisplay(kitchen);
                    } else {
                        glideChefToStation(kitchen, PetKitchen.KitchenStation.COOKING);
                    }

                    saveKitchens();
                }
            }
            case TIRED -> {
                // Waits for food
            }
        }
    }

    private void glideChefToStation(PetKitchen kitchen, PetKitchen.KitchenStation nextStation) {
        if (kitchen.isGliding()) return;

        ItemDisplay chef = kitchen.getChefDisplay();
        if (chef == null || !chef.isValid()) {
            kitchen.setCurrentStation(nextStation);
            spawnOrUpdateChefDisplay(kitchen);
            return;
        }

        KitchenStructureManager.StationPose pose = structureManager.getStationPose(kitchen.getLocation(), nextStation, kitchen.getRotation());
        Location from = chef.getLocation();
        Location to = pose.location();
        if (to == null || to.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            kitchen.setCurrentStation(nextStation);
            spawnOrUpdateChefDisplay(kitchen);
            return;
        }

        kitchen.setGliding(true);
        kitchen.setCurrentStation(nextStation);

        int totalTicks = 25; // ~1.25 seconds smooth walk
        Vector diff = to.toVector().subtract(from.toVector());
        Vector step = diff.clone().multiply(1.0 / totalTicks);

        float travelYaw = (float) Math.toDegrees(Math.atan2(-diff.getX(), diff.getZ()));
        travelYaw = (travelYaw % 360 + 360) % 360;
        if (travelYaw > 180f) travelYaw -= 360f;

        float chefWalkYaw = travelYaw + 180f;
        chefWalkYaw = (chefWalkYaw % 360 + 360) % 360;
        if (chefWalkYaw > 180f) chefWalkYaw -= 360f;

        float finalTravelYaw = travelYaw;
        float finalChefWalkYaw = chefWalkYaw;

        new BukkitRunnable() {
            int currentTick = 0;
            Location currentLoc = from.clone();

            @Override
            public void run() {
                if (!kitchen.isPetAssigned() || chef == null || !chef.isValid()) {
                    kitchen.setGliding(false);
                    cancel();
                    return;
                }

                currentTick++;
                currentLoc.add(step);
                currentLoc.setYaw(finalChefWalkYaw);
                currentLoc.setPitch(0f);

                chef.teleport(currentLoc);
                if (kitchen.getBedrockStand() != null && kitchen.getBedrockStand().isValid()) {
                    Location standLoc = currentLoc.clone().subtract(0, 0.70, 0);
                    standLoc.setYaw(finalTravelYaw);
                    kitchen.getBedrockStand().teleport(standLoc);
                }
                if (kitchen.getHologramDisplay() != null && kitchen.getHologramDisplay().isValid()) {
                    kitchen.getHologramDisplay().teleport(currentLoc.clone().add(0, 0.75, 0));
                }

                if (currentTick % 3 == 0) {
                    currentLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, currentLoc.clone().subtract(0, 0.2, 0), 1, 0.05, 0.05, 0.05, 0.01);
                }

                if (currentTick >= totalTicks) {
                    kitchen.setGliding(false);
                    chef.teleport(to);
                    if (kitchen.getBedrockStand() != null && kitchen.getBedrockStand().isValid()) {
                        Location standLoc = to.clone().subtract(0, 0.70, 0);
                        standLoc.setYaw(pose.entityYaw());
                        kitchen.getBedrockStand().teleport(standLoc);
                    }
                    if (kitchen.getHologramDisplay() != null && kitchen.getHologramDisplay().isValid()) {
                        kitchen.getHologramDisplay().teleport(to.clone().add(0, 0.75, 0));
                    }
                    spawnOrUpdateChefDisplay(kitchen);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void spawnOrUpdateChefDisplay(PetKitchen kitchen) {
        KitchenStructureManager.StationPose pose = structureManager.getStationPose(kitchen.getLocation(), kitchen.getCurrentStation(), kitchen.getRotation());
        Location targetLoc = pose.location();
        if (targetLoc == null || targetLoc.getWorld() == null) return;

        PetData data = plugin.getPetManager().getPetData(kitchen.getOwnerUuid());
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);

        ItemDisplay chef = kitchen.getChefDisplay();
        if (chef == null || !chef.isValid()) {
            chef = targetLoc.getWorld().spawn(targetLoc, ItemDisplay.class, d -> {
                d.setPersistent(false);
                d.setBillboard(Display.Billboard.FIXED);
                d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                float scale = 1.15f;
                Transformation t = new Transformation(
                        new Vector3f(0f, 0f, 0f),
                        new AxisAngle4f(0f, 0f, 1f, 0f),
                        new Vector3f(scale, scale, scale),
                        new AxisAngle4f(0f, 0f, 1f, 0f)
                );
                d.setTransformation(t);
                d.setItemStack(head);
            });
            kitchen.setChefDisplay(chef);

            // Bedrock Stand
            Location standLoc = targetLoc.clone().subtract(0, 0.70, 0);
            standLoc.setYaw(pose.entityYaw());
            standLoc.setPitch(0f);
            ArmorStand stand = standLoc.getWorld().spawn(standLoc, ArmorStand.class, s -> {
                s.setPersistent(false);
                s.setVisible(false);
                s.setGravity(false);
                s.setMarker(true);
                s.setSmall(true);
                s.setCustomNameVisible(false);
                if (s.getEquipment() != null) {
                    s.getEquipment().setHelmet(head);
                }
            });
            kitchen.setBedrockStand(stand);

            // Enforce Java vs Bedrock visibility: hide ArmorStand from Java players!
            updateChefVisibility(kitchen);
        } else if (!kitchen.isGliding()) {
            chef.teleport(targetLoc);
            if (kitchen.getBedrockStand() != null && kitchen.getBedrockStand().isValid()) {
                Location standLoc = targetLoc.clone().subtract(0, 0.70, 0);
                standLoc.setYaw(pose.entityYaw());
                kitchen.getBedrockStand().teleport(standLoc);
            }
        }

        // Hologram above chef
        Location holoLoc = targetLoc.clone().add(0, 0.75, 0);
        TextDisplay holo = kitchen.getHologramDisplay();
        String text = buildChefHoloText(kitchen, data);

        if (holo == null || !holo.isValid()) {
            holo = holoLoc.getWorld().spawn(holoLoc, TextDisplay.class, t -> {
                t.setPersistent(false);
                t.setBillboard(Display.Billboard.CENTER);
                t.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                t.text(ColorUtil.component(text));
            });
            kitchen.setHologramDisplay(holo);
            kitchen.setLastRenderedText(text);
        } else if (!kitchen.isGliding()) {
            holo.teleport(holoLoc);
            holo.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            if (!text.equals(kitchen.getLastRenderedText())) {
                holo.text(ColorUtil.component(text));
                kitchen.setLastRenderedText(text);
            }
        }
    }

    public void updateChefVisibility(PetKitchen kitchen) {
        if (kitchen == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateChefVisibilityFor(kitchen, p);
        }
    }

    public void updateChefVisibilityFor(PetKitchen kitchen, Player player) {
        if (kitchen == null || player == null || !player.isOnline()) return;
        boolean isBedrock = BedrockUtil.isBedrockPlayer(player);
        ItemDisplay display = kitchen.getChefDisplay();
        ArmorStand stand = kitchen.getBedrockStand();

        if (display != null && display.isValid()) {
            if (isBedrock) player.hideEntity(plugin, display);
            else player.showEntity(plugin, display);
        }
        if (stand != null && stand.isValid()) {
            if (isBedrock) player.showEntity(plugin, stand);
            else player.hideEntity(plugin, stand);
        }
    }

    private String buildChefHoloText(PetKitchen kitchen, PetData data) {
        PetKitchen.KitchenStation st = kitchen.getCurrentStation();
        String energyBar = data.getEnergyProgressBar() + " <white>" + (int) data.getEnergy() + "%</white>";
        String line1 = ColorUtil.getLevelTag(data.getLevel()) + " " + data.getName() + " <gradient:#4facfe:#00f2fe>[ᴋᴏᴋɪ ᴍʙɢ]</gradient>";

        if (st == PetKitchen.KitchenStation.TIRED) {
            return line1 + "\n" +
                    "<gradient:#ff5f6d:#ffc371><b>ᴘᴇᴛ ᴋᴏᴋɪ ʟᴀᴘᴀʀ!</b></gradient>\n" +
                    "<yellow>▶ ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴅᴇɴɢᴀɴ ᴍᴀᴋᴀɴᴀɴ</yellow>\n" +
                    "<gray>ᴇɴᴇʀɢɪ: </gray>" + energyBar;
        }

        String timerLine = (st == PetKitchen.KitchenStation.DELIVERY) ?
                "<green><b>[sᴇʀᴀʜᴋᴀɴ ᴘᴇsᴀɴᴀɴ]</b></green>" :
                "<aqua>ᴡᴀᴋᴛᴜ: </aqua><white>" + kitchen.getCurrentProgressSeconds() + "s / " + st.getDurationSeconds() + "s</white>";

        return line1 + "\n" +
                "<gray>sᴛᴀᴛᴜs: </gray>" + st.getDisplayName() + "\n" +
                timerLine + "\n" +
                "<gray>ᴇɴᴇʀɢɪ: </gray>" + energyBar;
    }

    private void updateCashierHologram(PetKitchen kitchen) {
        Location holoLoc = structureManager.getCashierHologramLocation(kitchen.getLocation(), kitchen.getRotation());
        if (holoLoc == null || holoLoc.getWorld() == null) return;

        // Facing towards entrance from the center
        float yaw = switch (kitchen.getRotation()) {
            case CLOCKWISE_90 -> 0f; // facing South (+Z) towards entrance at +Z
            case CLOCKWISE_180 -> 90f; // facing West (-X) towards entrance at -X
            case COUNTERCLOCKWISE_90 -> 180f; // facing North (-Z) towards entrance at -Z
            default -> -90f; // facing East (+X) towards entrance at +X
        };
        holoLoc.setYaw(yaw);
        holoLoc.setPitch(0f);

        TextDisplay cashier = kitchen.getCashierDisplay();
        String status = kitchen.isPetAssigned() ?
                kitchen.getCurrentStation().getDisplayName() :
                "<red>ᴛɪᴅᴀᴋ ᴀᴋᴛɪғ (ʙᴇʟᴜᴍ ᴅɪᴛᴜɢᴀsᴋᴀɴ)</red>";

        String text = "<gradient:#4facfe:#00f2fe><b>ᴅᴀᴘᴜʀ ᴍʙɢ [ᴛʏᴄᴏᴏɴ]</b></gradient>\n" +
                "&7ᴘᴇᴍɪʟɪᴋ: &f" + kitchen.getCachedOwnerName() + "\n" +
                "&7sᴛᴀᴛᴜs ᴋᴏᴋɪ: " + status + "\n" +
                "&7ᴜᴀɴɢ ᴅɪ ᴋᴀsɪʀ: &a+$" + plugin.getEconomyManager().format(kitchen.getStoredEarnings()) + " &7(" + kitchen.getCompletedOrders() + " ʙᴏx)\n" +
                "&e▶ ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴜɴᴛᴜᴋ ʙᴜᴋᴀ ᴍᴇɴᴜ";

        if (cashier == null || !cashier.isValid()) {
            cashier = holoLoc.getWorld().spawn(holoLoc, TextDisplay.class, t -> {
                t.setPersistent(false);
                t.setBillboard(Display.Billboard.FIXED);
                t.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                t.text(ColorUtil.component(text));
            });
            kitchen.setCashierDisplay(cashier);
        } else {
            cashier.teleport(holoLoc);
            cashier.setBillboard(Display.Billboard.FIXED);
            cashier.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            cashier.text(ColorUtil.component(text));
        }
    }

    public void removeKitchen(Location loc) {
        PetKitchen k = kitchens.remove(loc.getBlock().getLocation());
        if (k != null) {
            k.removeEntities();
            for (Location bLoc : k.getAllBlockLocations()) {
                blockToKitchen.remove(bLoc);
            }
            saveKitchens();
        }
    }

    public void loadKitchens() {
        kitchens.clear();
        blockToKitchen.clear();
        if (!kitchenFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(kitchenFile);
        ConfigurationSection sec = config.getConfigurationSection("kitchens");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                UUID kitchenId = UUID.fromString(key);
                UUID ownerUuid = UUID.fromString(sec.getString(key + ".owner-uuid", ""));
                String ownerName = sec.getString(key + ".owner-name", "Unknown");

                String worldName = sec.getString(key + ".origin.world");
                World world = Bukkit.getWorld(worldName != null ? worldName : "world");
                if (world == null) continue;

                int x = sec.getInt(key + ".origin.x");
                int y = sec.getInt(key + ".origin.y");
                int z = sec.getInt(key + ".origin.z");
                Location origin = new Location(world, x, y, z);

                String rotStr = sec.getString(key + ".rotation", "NONE");
                StructureRotation rotation = StructureRotation.valueOf(rotStr);
                String facing = sec.getString(key + ".facing", "WEST");

                boolean isPetAssigned = sec.getBoolean(key + ".pet-assigned", false);
                double earnings = sec.getDouble(key + ".stored-earnings", 0.0);
                int orders = sec.getInt(key + ".completed-orders", 0);
                int progress = sec.getInt(key + ".progress-seconds", 0);
                String stationStr = sec.getString(key + ".current-station", "COOKING");
                PetKitchen.KitchenStation station = PetKitchen.KitchenStation.valueOf(stationStr);

                PetKitchen kitchen = new PetKitchen(kitchenId, ownerUuid, ownerName, origin, rotation, facing,
                        isPetAssigned, earnings, orders, progress, station);

                List<String> blockList = sec.getStringList(key + ".blocks");
                Set<Location> bSet = new HashSet<>();
                for (String bStr : blockList) {
                    String[] parts = bStr.split(",");
                    if (parts.length == 3) {
                        int bx = Integer.parseInt(parts[0]);
                        int by = Integer.parseInt(parts[1]);
                        int bz = Integer.parseInt(parts[2]);
                        Location bLoc = new Location(world, bx, by, bz);
                        bSet.add(bLoc);
                        blockToKitchen.put(bLoc, kitchen);
                    }
                }
                kitchen.setAllBlockLocations(bSet);

                kitchens.put(origin, kitchen);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kitchen: " + key + " -> " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + kitchens.size() + " active Dapur MBG buildings.");
    }

    public void saveKitchens() {
        FileConfiguration config = new YamlConfiguration();
        for (PetKitchen k : kitchens.values()) {
            String path = "kitchens." + k.getKitchenId().toString();
            config.set(path + ".owner-uuid", k.getOwnerUuid().toString());
            config.set(path + ".owner-name", k.getCachedOwnerName());
            config.set(path + ".origin.world", k.getLocation().getWorld().getName());
            config.set(path + ".origin.x", k.getLocation().getBlockX());
            config.set(path + ".origin.y", k.getLocation().getBlockY());
            config.set(path + ".origin.z", k.getLocation().getBlockZ());
            config.set(path + ".rotation", k.getRotation().name());
            config.set(path + ".facing", k.getFacing());
            config.set(path + ".pet-assigned", k.isPetAssigned());
            config.set(path + ".stored-earnings", k.getStoredEarnings());
            config.set(path + ".completed-orders", k.getCompletedOrders());
            config.set(path + ".progress-seconds", k.getCurrentProgressSeconds());
            config.set(path + ".current-station", k.getCurrentStation().name());

            List<String> bList = new ArrayList<>();
            for (Location bLoc : k.getAllBlockLocations()) {
                bList.add(bLoc.getBlockX() + "," + bLoc.getBlockY() + "," + bLoc.getBlockZ());
            }
            config.set(path + ".blocks", bList);
        }

        try {
            config.save(kitchenFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save kitchens.yml: " + e.getMessage());
        }
    }

    public void cleanup() {
        for (PetKitchen k : kitchens.values()) {
            k.removeEntities();
        }
    }

    public void removeAllEntities() {
        cleanup();
    }
}
