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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitchenManager {

    private final LeftyPetPlugin plugin;
    private final KitchenStructureManager structureManager;
    private final Map<Location, PetKitchen> kitchens = new HashMap<>();
    private final Set<Location> buildingKitchens = new HashSet<>();
    private final NamespacedKey kitchenLevelKey;
    private final File kitchenFile;

    public KitchenManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.structureManager = new KitchenStructureManager(plugin);
        this.kitchenLevelKey = new NamespacedKey(plugin, "kitchen_level");
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
        return kitchens.get(loc.getBlock().getLocation());
    }

    public PetKitchen getKitchenOfBlock(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        PetKitchen direct = kitchens.get(loc.getBlock().getLocation());
        if (direct != null) return direct;
        for (Map.Entry<Location, PetKitchen> entry : kitchens.entrySet()) {
            if (structureManager.isPartOfStructure(entry.getKey(), loc)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isKitchenAreaOrBuilding(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (isBuilding(loc)) return true;
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

    public boolean isBuilding(Location loc) {
        return buildingKitchens.contains(loc.getBlock().getLocation());
    }

    public void setBuilding(Location loc, boolean building) {
        if (building) {
            buildingKitchens.add(loc.getBlock().getLocation());
        } else {
            buildingKitchens.remove(loc.getBlock().getLocation());
        }
    }

    public ItemStack createKitchenItem(int level) {
        int lvl = Math.max(1, Math.min(3, level));
        ItemStack item = new ItemStack(Material.SMOKER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component("<gradient:#ff512f:#dd2476><b>🍱 ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ." + lvl + "]</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ᴘᴀʙʀɪᴋ & ᴅᴀᴘᴜʀ ᴋᴀᴛᴇʀɪɴɢ ᴏᴛᴏᴍᴀᴛɪs!"));
            lore.add(ColorUtil.component("&7ᴘᴇᴛ ᴀᴋᴀɴ ʙᴇᴋᴇʀᴊᴀ ᴍᴇᴍᴀsᴀᴋ & ᴍᴇɴɢᴇᴍᴀs"));
            lore.add(ColorUtil.component("&7ɴᴀsɪ ᴋᴏᴛᴀᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢʜᴀsɪʟᴋᴀɴ ᴜᴀɴɢ."));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7• ʟᴇᴠᴇʟ ᴅᴀᴘᴜʀ: &e" + lvl));
            lore.add(ColorUtil.component("&7• ʜᴀsɪʟ: &a$" + (lvl == 3 ? "1,000" : (lvl == 2 ? "500" : "250")) + " &7/ ʙᴏx"));
            lore.add(ColorUtil.component("&7• ᴋᴀᴘᴀsɪᴛᴀs: &b" + (lvl == 3 ? "30" : (lvl == 2 ? "20" : "10")) + " ʙᴏx"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&a▶ ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴀʀᴇᴀ 3x3x4 ᴛᴇʀʙᴜᴋᴀ!"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(kitchenLevelKey, PersistentDataType.INTEGER, lvl);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isKitchenItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(kitchenLevelKey, PersistentDataType.INTEGER);
    }

    public int getKitchenItemLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 1;
        Integer lvl = item.getItemMeta().getPersistentDataContainer().get(kitchenLevelKey, PersistentDataType.INTEGER);
        return (lvl != null) ? Math.max(1, Math.min(3, lvl)) : 1;
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

        player.getInventory().addItem(createKitchenItem(1));
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#43e97b:#38f9d7>ᴋᴀᴍᴜ ᴍᴇɴᴇʀɪᴍᴀ 1x ʙʟᴏᴋ ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ.1]! ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴀʀᴇᴀ 3x3x4 ᴛᴇʀʙᴜᴋᴀ.</gradient>"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.4f);
        return true;
    }

    public void registerKitchen(UUID kitchenId, UUID ownerUuid, String ownerName, Location loc, int level) {
        Location center = loc.getBlock().getLocation();
        PetKitchen kitchen = new PetKitchen(kitchenId, ownerUuid, ownerName, center, level);
        kitchens.put(center, kitchen);
        saveKitchens();
    }

    public void removeKitchen(Location loc) {
        PetKitchen k = kitchens.remove(loc.getBlock().getLocation());
        if (k != null) {
            k.removeEntities();
            saveKitchens();
        }
    }

    public void dismantleKitchen(Player player, PetKitchen kitchen) {
        if (!kitchen.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴɪ ʙᴜᴋᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ ᴍɪʟɪᴋᴍᴜ!</gradient>"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ ᴘᴇɴᴜʜ! ᴋᴏsᴏɴɢᴋᴀɴ sʟᴏᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴏɴɢᴋᴀʀ ᴅᴀᴘᴜʀ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Auto collect pending earnings if any
        if (kitchen.getCookedPortions() > 0) {
            claimPortions(player, kitchen);
        }

        Location loc = kitchen.getLocation();
        kitchen.removeEntities();
        structureManager.removeStructure(loc);
        removeKitchen(loc);

        player.getInventory().addItem(createKitchenItem(kitchen.getKitchenLevel()));
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#43e97b:#38f9d7>ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ." + kitchen.getKitchenLevel() + "] ʙᴇʀʜᴀsɪʟ ᴅɪʙᴏɴɢᴋᴀʀ ᴅᴀɴ ᴅɪᴍᴀsᴜᴋᴋᴀɴ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ!</gradient>"));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.7f, 1.2f);
    }

    public void claimPortions(Player player, PetKitchen kitchen) {
        int portions = kitchen.getCookedPortions();
        if (portions <= 0) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ʙᴇʟᴜᴍ ᴀᴅᴀ ɴᴀsɪ ᴋᴏᴛᴀᴋ ᴍʙɢ ʏᴀɴɢ sɪᴀᴘ ᴅɪᴀᴍʙɪʟ! ᴛᴜɴɢɢᴜ ᴘᴇᴛ ᴍᴇᴍᴀsᴀᴋ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        double money = portions * kitchen.getRewardPerPortion();
        plugin.getEconomyManager().deposit(player, money);

        kitchen.setCookedPortions(0);
        saveKitchens();

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴍᴇɴᴅɪsᴛʀɪʙᴜsɪᴋᴀɴ </gradient><yellow><b>" + portions + "x ʙᴏx ᴍʙɢ</b></yellow><gradient:#43e97b:#38f9d7>! ᴍᴇɴᴇʀɪᴍᴀ sᴜʙsɪᴅɪ: </gradient><green><b>+$" + plugin.getEconomyManager().format(money) + "</b></green>"));

        // 20% chance for bonus food supply
        if (Math.random() < 0.20) {
            ItemStack bonusFood = switch (new Random().nextInt(4)) {
                case 0 -> new ItemStack(Material.GOLDEN_CARROT, 4);
                case 1 -> new ItemStack(Material.COOKED_BEEF, 8);
                case 2 -> new ItemStack(Material.BREAD, 16);
                default -> new ItemStack(Material.RAW_GOLD, 3);
            };
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(bonusFood);
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#ffd200:#f7971e>🎁 ʙᴏɴᴜs ʟᴏɢɪsᴛɪᴋ ᴅᴀᴘᴜʀ: ᴋᴀᴍᴜ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ </gradient><white>" + bonusFood.getAmount() + "x " + bonusFood.getType().name() + "</white>!"));
            }
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        updateKitchenHologram(kitchen);
    }

    public void upgradeKitchen(Player player, PetKitchen kitchen) {
        int currentLvl = kitchen.getKitchenLevel();
        if (currentLvl >= 3) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴅᴀᴘᴜʀ ᴍʙɢ sᴜᴅᴀʜ ᴍᴇɴᴄᴀᴘᴀɪ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ (ʟᴠ.3)!</gradient>"));
            return;
        }

        int targetLvl = currentLvl + 1;
        double cost = (targetLvl == 2) ? 15000.0 : 35000.0;

        if (!plugin.getEconomyManager().hasEnough(player, cost)) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴜᴀɴɢ ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ᴄᴜᴋᴜᴘ ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ! ʙɪᴀʏᴀ: </gradient><yellow>$" + plugin.getEconomyManager().format(cost) + "</yellow>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        plugin.getEconomyManager().withdraw(player, cost);
        kitchen.setKitchenLevel(targetLvl);
        saveKitchens();

        structureManager.buildStructure(kitchen.getLocation(), targetLvl);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#43e97b:#38f9d7>🎉 sᴇʟᴀᴍᴀᴛ! ᴅᴀᴘᴜʀ ᴍʙɢ ʙᴇʀʜᴀsɪʟ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ ᴋᴇ </gradient><yellow><b>ʟᴇᴠᴇʟ " + targetLvl + "</b></yellow>!"));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        updateKitchenHologram(kitchen);
    }

    private int tickerStep = 0;

    private void startKitchenTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickerStep++;

            for (PetKitchen kitchen : kitchens.values()) {
                Player owner = Bukkit.getPlayer(kitchen.getOwnerUuid());
                boolean isOnline = (owner != null && owner.isOnline());

                // 1. KITCHEN TYCOON LOGIC: Progresses as long as owner is online
                if (isOnline && kitchen.isCooking() && !kitchen.isStorageFull()) {
                    tickKitchenLogic(kitchen);
                }

                // 2. PROXIMITY CULLING & VISUALS: Only run when chunk loaded and player within 32 blocks
                Location loc = kitchen.getLocation();
                World world = loc.getWorld();
                if (world == null || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                    continue;
                }

                boolean hasNearbyPlayer = false;
                double lx = loc.getX();
                double ly = loc.getY();
                double lz = loc.getZ();
                for (Player p : world.getPlayers()) {
                    Location pl = p.getLocation();
                    double dx = pl.getX() - lx;
                    double dy = pl.getY() - ly;
                    double dz = pl.getZ() - lz;
                    if ((dx * dx + dy * dy + dz * dz) <= 1024.0) { // 32 blocks
                        hasNearbyPlayer = true;
                        break;
                    }
                }

                if (!hasNearbyPlayer) {
                    continue;
                }

                // Visual updates
                ensureHologram(kitchen);
                updateChefDisplay(kitchen);
                updateKitchenHologram(kitchen);

                // Ambient particles based on active station
                spawnStationParticles(kitchen);
            }
        }, 20L, 20L); // 1.0s interval
    }

    private void tickKitchenLogic(PetKitchen kitchen) {
        int lvl = kitchen.getKitchenLevel();
        // Station durations per level
        int prepMax = (lvl == 3) ? 6 : ((lvl == 2) ? 10 : 15);
        int cookMax = (lvl == 3) ? 10 : ((lvl == 2) ? 15 : 20);
        int packMax = (lvl == 3) ? 5 : ((lvl == 2) ? 8 : 10);

        int curSec = kitchen.getCurrentProgressSeconds() + 1;
        PetKitchen.KitchenStation station = kitchen.getCurrentStation();

        switch (station) {
            case PREPARING -> {
                if (curSec >= prepMax) {
                    kitchen.setCurrentStation(PetKitchen.KitchenStation.COOKING);
                    kitchen.setCurrentProgressSeconds(0);
                } else {
                    kitchen.setCurrentProgressSeconds(curSec);
                }
            }
            case COOKING -> {
                if (curSec >= cookMax) {
                    kitchen.setCurrentStation(PetKitchen.KitchenStation.PACKING);
                    kitchen.setCurrentProgressSeconds(0);
                } else {
                    kitchen.setCurrentProgressSeconds(curSec);
                }
            }
            case PACKING -> {
                if (curSec >= packMax) {
                    kitchen.addCookedPortion();
                    kitchen.setCurrentStation(PetKitchen.KitchenStation.PREPARING);
                    kitchen.setCurrentProgressSeconds(0);
                    saveKitchens();
                } else {
                    kitchen.setCurrentProgressSeconds(curSec);
                }
            }
        }
    }

    private void spawnStationParticles(PetKitchen kitchen) {
        Location center = kitchen.getLocation();
        PetKitchen.KitchenStation station = kitchen.getCurrentStation();
        Location stLoc = structureManager.getStationLocation(center, station);

        switch (station) {
            case PREPARING -> {
                // Slice / Prep particles
                center.getWorld().spawnParticle(Particle.SWEEP_ATTACK, stLoc.clone().subtract(0, 0.4, 0), 1, 0.1, 0.1, 0.1, 0.0);
            }
            case COOKING -> {
                // Flame & Sizzling smoke
                center.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, stLoc.clone().subtract(0, 0.3, 0), 2, 0.1, 0.1, 0.1, 0.02);
                center.getWorld().spawnParticle(Particle.FLAME, stLoc.clone().subtract(0, 0.4, 0), 1, 0.1, 0.05, 0.1, 0.01);
            }
            case PACKING -> {
                // Box packing sparks
                center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, stLoc.clone().subtract(0, 0.3, 0), 2, 0.2, 0.1, 0.2, 0.0);
            }
        }
    }

    public void updateChefDisplay(PetKitchen kitchen) {
        Location center = kitchen.getLocation();
        Location targetLoc = structureManager.getStationLocation(center, kitchen.getCurrentStation());

        ItemDisplay display = kitchen.getChefDisplay();
        ArmorStand stand = kitchen.getBedrockStand();

        if (display == null || !display.isValid()) {
            spawnChefEntities(kitchen, targetLoc);
            return;
        }

        // Smooth move towards current station
        if (display.getLocation().distanceSquared(targetLoc) > 0.04) {
            targetLoc.setYaw((display.getLocation().getYaw() + 4.0f) % 360f);
            display.teleport(targetLoc);
            if (stand != null && stand.isValid()) {
                stand.teleport(targetLoc.clone().subtract(0, 0.70, 0));
            }
        } else {
            // Gentle continuous rotation
            Location cur = display.getLocation();
            cur.setYaw((cur.getYaw() + 4.0f) % 360f);
            display.teleport(cur);
        }
    }

    private void spawnChefEntities(PetKitchen kitchen, Location targetLoc) {
        PetData data = plugin.getPetManager().getPetData(kitchen.getOwnerUuid());
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);

        ItemDisplay display = targetLoc.getWorld().spawn(targetLoc, ItemDisplay.class, d -> {
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
        kitchen.setChefDisplay(display);

        // Bedrock ArmorStand fallback
        Location standLoc = targetLoc.clone().subtract(0, 0.70, 0);
        ArmorStand stand = standLoc.getWorld().spawn(standLoc, ArmorStand.class, s -> {
            s.setPersistent(false);
            s.setInvisible(true);
            s.setMarker(true);
            s.setSmall(true);
            s.setGravity(false);
            s.setInvulnerable(true);
            s.setCollidable(false);
            s.setSilent(true);
            s.setBasePlate(false);
            s.setArms(false);
            s.getEquipment().setHelmet(head);
        });
        kitchen.setBedrockStand(stand);

        updateChefVisibility(kitchen);
    }

    public void updateChefVisibility(PetKitchen kitchen) {
        ItemDisplay display = kitchen.getChefDisplay();
        ArmorStand stand = kitchen.getBedrockStand();
        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean isBedrock = BedrockUtil.isBedrockPlayer(p);
            if (display != null && display.isValid()) {
                if (isBedrock) p.hideEntity(plugin, display);
                else p.showEntity(plugin, display);
            }
            if (stand != null && stand.isValid()) {
                if (isBedrock) p.showEntity(plugin, stand);
                else p.hideEntity(plugin, stand);
            }
        }
    }

    public void ensureHologram(PetKitchen kitchen) {
        Location center = kitchen.getLocation();
        Location holoLoc = center.clone().add(0.5, 2.3, 0.5);

        TextDisplay text = kitchen.getHologramDisplay();
        if (text == null || !text.isValid()) {
            text = holoLoc.getWorld().spawn(holoLoc, TextDisplay.class, t -> {
                t.setPersistent(false);
                t.setBillboard(Display.Billboard.CENTER);
                t.setDefaultBackground(false);
                t.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                t.setShadowed(true);
            });
            kitchen.setHologramDisplay(text);
        }
    }

    public void updateKitchenHologram(PetKitchen kitchen) {
        ensureHologram(kitchen);
        TextDisplay text = kitchen.getHologramDisplay();
        if (text == null || !text.isValid()) return;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(kitchen.getOwnerUuid());
        String ownerName = (kitchen.getCachedOwnerName() != null) ? kitchen.getCachedOwnerName() : (owner.getName() != null ? owner.getName() : "Player");

        int portions = kitchen.getCookedPortions();
        int max = kitchen.getMaxCapacity();
        boolean isFull = kitchen.isStorageFull();
        boolean isOnline = owner.isOnline();

        String statusLine;
        if (isFull) {
            statusLine = "<gradient:#ff416c:#ff4b2b><b>⚠️ ʙᴏx ᴘᴇɴᴜʜ (" + portions + "/" + max + ")</b></gradient>\n<yellow>ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴜɴᴛᴜᴋ ᴋʟᴀɪᴍ ᴜᴀɴɢ sᴜʙsɪᴅɪ!</yellow>";
        } else if (!isOnline) {
            statusLine = "<gradient:#ff416c:#ff4b2b><b>● ᴘʟᴀʏᴇʀ ᴏғғʟɪɴᴇ (ᴛᴇʀᴊᴇᴅᴀ)</b></gradient>";
        } else {
            statusLine = "<yellow>sᴛᴀsɪᴜɴ: </yellow><gradient:#00f2fe:#4facfe><b>" + kitchen.getCurrentStation().getDisplayName() + "</b></gradient>\n" +
                    "<gray>ʙᴏx sɪᴀᴘ sᴀᴊɪ: </gray><green><b>" + portions + "/" + max + " ʙᴏx</b></green>";
        }

        String full = "<gradient:#ff512f:#dd2476><b>✦ ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ." + kitchen.getKitchenLevel() + "] ✦</b></gradient>\n" +
                "<gray>ᴘᴇᴍɪʟɪᴋ: </gray><white>" + ownerName + "</white>\n" +
                statusLine;

        if (!full.equals(kitchen.getLastRenderedText())) {
            kitchen.setLastRenderedText(full);
            text.text(ColorUtil.component(full));
        }
    }

    public void loadKitchens() {
        if (!kitchenFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(kitchenFile);
        ConfigurationSection sec = cfg.getConfigurationSection("kitchens");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                UUID ownerUuid = UUID.fromString(sec.getString(key + ".owner"));
                String ownerName = sec.getString(key + ".owner-name", null);
                Location loc = sec.getLocation(key + ".location");
                int lvl = sec.getInt(key + ".level", 1);
                int portions = sec.getInt(key + ".portions", 0);
                int progSec = sec.getInt(key + ".progress-sec", 0);
                String stationStr = sec.getString(key + ".station", "PREPARING");
                PetKitchen.KitchenStation station = PetKitchen.KitchenStation.valueOf(stationStr);
                boolean isCook = sec.getBoolean(key + ".is-cooking", true);

                if (loc != null) {
                    PetKitchen kitchen = new PetKitchen(id, ownerUuid, ownerName, loc, lvl, portions, progSec, station, isCook);
                    kitchens.put(loc.getBlock().getLocation(), kitchen);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading kitchen: " + e.getMessage());
            }
        }
    }

    public void saveKitchens() {
        FileConfiguration cfg = new YamlConfiguration();
        for (PetKitchen k : kitchens.values()) {
            String key = "kitchens." + k.getKitchenId().toString();
            cfg.set(key + ".owner", k.getOwnerUuid().toString());
            if (k.getCachedOwnerName() != null) {
                cfg.set(key + ".owner-name", k.getCachedOwnerName());
            }
            cfg.set(key + ".location", k.getLocation());
            cfg.set(key + ".level", k.getKitchenLevel());
            cfg.set(key + ".portions", k.getCookedPortions());
            cfg.set(key + ".progress-sec", k.getCurrentProgressSeconds());
            cfg.set(key + ".station", k.getCurrentStation().name());
            cfg.set(key + ".is-cooking", k.isCooking());
        }
        try {
            cfg.save(kitchenFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kitchens.yml: " + e.getMessage());
        }
    }

    public void removeAllEntities() {
        for (PetKitchen k : kitchens.values()) {
            k.removeEntities();
        }
    }
}
