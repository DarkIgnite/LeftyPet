package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.model.PetSkin;
import com.leftycraft.leftypet.util.ColorUtil;
import com.leftycraft.leftypet.util.HeadUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AltarManager {

    private final LeftyPetPlugin plugin;
    private final AltarStructureManager structureManager;
    private final Map<Location, PetAltar> altars = new ConcurrentHashMap<>();
    private final Set<Location> buildingAltars = ConcurrentHashMap.newKeySet();
    private final NamespacedKey altarKey;
    private final NamespacedKey altarLevelKey;
    private final File altarFile;

    public AltarManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.structureManager = new AltarStructureManager(plugin);
        this.altarKey = new NamespacedKey(plugin, "is_pet_altar");
        this.altarLevelKey = new NamespacedKey(plugin, "pet_altar_level");
        this.altarFile = new File(plugin.getDataFolder(), "altars.yml");
        loadAltars();
        startAltarTicker();
    }

    public AltarStructureManager getStructureManager() {
        return structureManager;
    }

    public boolean isBuilding(Location loc) {
        if (buildingAltars.isEmpty()) return false;
        for (Location cLoc : buildingAltars) {
            if (structureManager.isPartOfStructure(cLoc, loc)) {
                return true;
            }
        }
        return false;
    }

    public void setBuilding(Location center, boolean building) {
        if (building) {
            buildingAltars.add(center.getBlock().getLocation());
        } else {
            buildingAltars.remove(center.getBlock().getLocation());
        }
    }

    public Set<Location> getBuildingAltars() {
        return buildingAltars;
    }

    public ItemStack createAltarItem() {
        return createAltarItem(1);
    }

    public ItemStack createAltarItem(int level) {
        int lvl = Math.max(1, Math.min(4, level));
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component("<gradient:#ff9900:#ff5500><b>ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ [ʟᴠ." + lvl + "] (3x3)</b></gradient>"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("&7ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴀʀᴇᴀ &e3x3 &7ᴛᴇʀʙᴜᴋᴀ"));
            lore.add(ColorUtil.component("&7ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴀɴɢᴜɴ ғᴀsɪʟɪᴛᴀs ᴀғᴋ ᴛʀᴀɪɴɪɴɢ!"));
            lore.add(ColorUtil.component(""));
            int discount = (lvl == 4) ? 50 : ((lvl - 1) * 10);
            if (discount > 0) {
                lore.add(ColorUtil.component("&eᴀʟᴛᴀʀ ʟᴇᴠᴇʟ: &f" + lvl + " &7(-" + discount + "% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ)"));
            } else {
                lore.add(ColorUtil.component("&eᴀʟᴛᴀʀ ʟᴇᴠᴇʟ: &f" + lvl + " &7(sᴛᴀɴᴅᴀʀ / 0% ᴅɪsᴋᴏɴ)"));
            }
            if (lvl == 4) {
                lore.add(ColorUtil.component("&d&l✦ CELESTIAL EXCLUSIVE &7- Max Level!"));
            } else if (lvl < 4) {
                lore.add(ColorUtil.component("&bʙɪsᴀ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ &7ʜɪɴɢɢᴀ ʟᴇᴠᴇʟ 4 (-50%)"));
            }
            meta.lore(lore);
            meta.getPersistentDataContainer().set(altarKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(altarLevelKey, PersistentDataType.INTEGER, lvl);
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getAltarItemLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 1;
        Integer lvl = item.getItemMeta().getPersistentDataContainer().get(altarLevelKey, PersistentDataType.INTEGER);
        return (lvl != null) ? Math.max(1, Math.min(4, lvl)) : 1;
    }

    public boolean isAltarItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(altarKey, PersistentDataType.BOOLEAN);
    }

    public NamespacedKey getAltarKey() {
        return altarKey;
    }

    public void registerAltar(UUID altarId, UUID ownerUuid, Location loc, int level) {
        PetAltar altar = new PetAltar(altarId, ownerUuid, loc, level, 0L, 0, false);
        altars.put(loc.getBlock().getLocation(), altar);
        saveAltars();
    }

    public Map<Location, PetAltar> getAltarsMap() {
        return Collections.unmodifiableMap(altars);
    }

    public PetAltar getAltarAt(Location loc) {
        return altars.get(loc.getBlock().getLocation());
    }

    public PetAltar getAltarByOwner(UUID ownerUuid) {
        for (PetAltar altar : altars.values()) {
            if (altar.getOwnerUuid().equals(ownerUuid)) {
                return altar;
            }
        }
        return null;
    }

    public void removeAltar(Location loc) {
        PetAltar altar = altars.remove(loc.getBlock().getLocation());
        if (altar != null) {
            altar.removeEntities();
            saveAltars();
        }
    }

    public void dismantleAltar(Player player, PetAltar altar) {
        if (!altar.getOwnerUuid().equals(player.getUniqueId()) && !player.hasPermission("leftypet.admin")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴɪ ʙᴜᴋᴀɴ ᴀʟᴛᴀʀ ᴍɪʟɪᴋᴍᴜ!</gradient>"));
            return;
        }

        // Check if player inventory is full (Revisi 14)
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ ᴘᴇɴᴜʜ! ᴋᴏsᴏɴɢᴋᴀɴ sʟᴏᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴏɴɢᴋᴀʀ ᴀʟᴛᴀʀ.</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        if (altar.isTraining()) {
            cancelTraining(player);
        }

        Location loc = altar.getLocation();
        altar.removeEntities();
        structureManager.removeStructure(loc);
        removeAltar(loc);

        // Put directly into inventory keeping altar level
        player.getInventory().addItem(createAltarItem(altar.getAltarLevel()));
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴀʟᴛᴀʀ [ʟᴠ." + altar.getAltarLevel() + "] 3x3 ʙᴇʀʜᴀsɪʟ ᴅɪʙᴏɴɢᴋᴀʀ ᴅᴀɴ ᴅɪᴍᴀsᴜᴋᴋᴀɴ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ!</gradient>"));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.7f, 1.2f);
    }

    public void startTraining(Player player, Location altarLoc) {
        UUID uuid = player.getUniqueId();
        PetData data = plugin.getPetManager().getPetData(uuid);

        if (!plugin.getPetManager().isPetSummoned(uuid)) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴘᴇᴛ ᴋᴀᴍᴜ ʜᴀʀᴜs ᴅɪᴘᴀɴɢɢɪʟ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ sᴇʙᴇʟᴜᴍ ʙɪsᴀ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ ᴅɪ ᴀʟᴛᴀʀ!</gradient>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        if (data.isTraining()) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("altar-already-training")));
            return;
        }

        PetAltar altar = getAltarAt(altarLoc);
        if (altar == null) return;

        // Upgrade cooldown check
        if (data.isUpgradeOnCooldown()) {
            int remSec = data.getUpgradeCooldownRemainingSeconds();
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴘᴇᴛ ᴋᴀᴍᴜ ᴍᴀsɪʜ ᴅᴀʟᴀᴍ ᴄᴏᴏʟᴅᴏᴡɴ sᴇᴛᴇʟᴀʜ ᴜᴘɢʀᴀᴅᴇ! sɪsᴀ ᴡᴀᴋᴛᴜ: </gradient><yellow>" + formatDuration(remSec) + "</yellow>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        // Celestial rank restriction for Level 4 altar
        if (altar.getAltarLevel() == 4 && !player.hasPermission("leftypet.celestial")) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ 4 ʜᴀɴʏᴀ ʙɪsᴀ ᴅɪɢᴜɴᴀᴋᴀɴ ᴏʟᴇʜ ʀᴀɴᴋ </gradient><gradient:#d946ef:#8b5cf6><b>CELESTIAL</b></gradient>!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        int maxLvl = plugin.getConfigManager().getMaxLevel();
        if (data.getLevel() >= maxLvl) {
            String msg = plugin.getConfigManager().getMessage("altar-max-level").replace("{maxLevel}", String.valueOf(maxLvl));
            player.sendMessage(ColorUtil.component(msg));
            return;
        }

        int currentLevel = data.getLevel();
        int targetLevel = currentLevel + 1;

        double cost = plugin.getConfigManager().getUpgradeCost(currentLevel);
        if (cost > 0.0 && !plugin.getEconomyManager().hasEnough(player, cost)) {
            String formattedCost = plugin.getEconomyManager().format(cost);
            String formattedBal = plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player));
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴜᴀɴɢ ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ᴄᴜᴋᴜᴘ! ʙɪᴀʏᴀ ᴜᴘɢʀᴀᴅᴇ: </gradient><yellow>" + formattedCost + "</yellow> <gray>(sᴀʟᴅᴏ: <red>" + formattedBal + "</red>)</gray>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        int baseSec = plugin.getConfigManager().getUpgradeDuration(currentLevel);
        int finalSec = (int) Math.round(baseSec * altar.getTimeMultiplier());
        long finishTime = System.currentTimeMillis() + (finalSec * 1000L);

        // Deduct money
        if (cost > 0.0) {
            plugin.getEconomyManager().withdraw(player, cost);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gray>ʙɪᴀʏᴀ ᴜᴘɢʀᴀᴅᴇ ᴛᴇʀᴘᴏᴛᴏɴɢ: </gray><red>-" + plugin.getEconomyManager().format(cost) + "</red>"));
        }

        altar.setTraining(true);
        altar.setRemainingSeconds(finalSec);
        altar.setTargetLevel(targetLevel);

        data.setTraining(true);
        data.setCurrentAltarId(altar.getAltarId());
        plugin.getPetManager().despawnPet(uuid);

        spawnFloatingHead(altar);
        updateAltarHologram(altar);
        saveAltars();

        String timeStr = formatDuration(finalSec);
        String msg = plugin.getConfigManager().getMessage("altar-started")
                .replace("{targetLevel}", String.valueOf(targetLevel))
                .replace("{time}", timeStr);
        player.sendMessage(ColorUtil.component(msg));
        player.playSound(altarLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.3f);
    }

    public void cancelTraining(Player player) {
        UUID uuid = player.getUniqueId();
        PetData data = plugin.getPetManager().getPetData(uuid);

        PetAltar altar = null;
        for (PetAltar a : altars.values()) {
            if (a.getOwnerUuid().equals(uuid) && a.isTraining()) {
                altar = a;
                break;
            }
        }

        if (altar != null) {
            double cost = plugin.getConfigManager().getUpgradeCost(data.getLevel());
            if (cost > 0.0) {
                plugin.getEconomyManager().deposit(player, cost);
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gray>ʙɪᴀʏᴀ ᴜᴘɢʀᴀᴅᴇ ᴅɪᴋᴇᴍʙᴀʟɪᴋᴀɴ: </gray><green>+" + plugin.getEconomyManager().format(cost) + "</green>"));
            }
            altar.setTraining(false);
            altar.removeEntities();
            saveAltars();
        }

        data.setTraining(false);
        data.setCurrentAltarId(null);

        // Re-summon pet beside player
        plugin.getPetManager().summonPet(player);

        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#f6d365:#fda085>ᴛʀᴀɪɴɪɴɢ ᴅɪʙᴀᴛᴀʟᴋᴀɴ! ᴘᴇᴛ ᴋᴀᴍᴜ ᴛᴇʟᴀʜ ᴋᴇᴍʙᴀʟɪ ᴋᴇ sᴀᴍᴘɪɴɢᴍᴜ.</gradient>"));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
    }

    public void claimTraining(Player player, PetAltar altar) {
        UUID uuid = player.getUniqueId();
        if (!altar.getOwnerUuid().equals(uuid)) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#ff5f6d:#ffc371>ɪɴɪ ʙᴜᴋᴀɴ ᴀʟᴛᴀʀ ᴍɪʟɪᴋᴍᴜ!</gradient>"));
            return;
        }

        if (!altar.isFinished()) {
            return;
        }

        PetData data = plugin.getPetManager().getPetData(uuid);
        data.setLevel(altar.getTargetLevel());
        data.setEnergy(100.0);
        data.setTraining(false);
        data.setCurrentAltarId(null);

        altar.setTraining(false);
        altar.removeEntities();
        saveAltars();

        String msg = plugin.getConfigManager().getMessage("altar-claimed")
                .replace("{level}", String.valueOf(data.getLevel()));
        player.sendMessage(ColorUtil.component(msg));

        // Broadcast to all players (Revisi 13)
        Component bc = ColorUtil.component("<gradient:#ff9900:#ff00cc><b>[ʟᴇғᴛʏᴘᴇᴛ]</b></gradient> <yellow>"
                + player.getName() + "</yellow> <white>ʙᴀʀᴜ sᴀᴊᴀ ᴍᴇɴɢ-ᴜᴘɢʀᴀᴅᴇ ᴘᴇᴛ ᴍᴇʀᴇᴋᴀ ᴋᴇ</white> <gradient:#00f2fe:#4facfe><b>ʟᴇᴠᴇʟ "
                + data.getLevel() + "</b></gradient> <gray>ᴅɪ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ!</gray>");
        Bukkit.broadcast(bc);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.4f);
        }

        // Fanfare for claimant
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, altar.getLocation().clone().add(0.5, 1.5, 0.5), 45, 0.5, 0.5, 0.5, 0.2);

        // Auto summon
        plugin.getPetManager().summonPet(player);

        // Apply Upgrade Cooldown (Revisi 18)
        int cooldownSec = getUpgradeCooldownSeconds(data.getLevel());
        data.setUpgradeCooldownUntil(System.currentTimeMillis() + (cooldownSec * 1000L));
        plugin.getPetManager().savePetData(uuid);
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gray>ᴘᴇᴛ ᴍᴀsᴜᴋ ᴍᴀsᴀ ᴄᴏᴏʟᴅᴏᴡɴ ᴜᴘɢʀᴀᴅᴇ: </gray><yellow>" + formatDuration(cooldownSec) + "</yellow> <gray>(ᴛɪᴅᴀᴋ ʙɪsᴀ ᴛʀᴀɪɴɪɴɢ sᴇʟᴀᴍᴀ ᴄᴏᴏʟᴅᴏᴡɴ)</gray>"));
    }

    /**
     * Calculates upgrade cooldown in seconds based on pet level.
     * Lv 1-10: 1 to 5 minutes (60s to 300s)
     * Lv 11-20: 5 to 8 minutes (300s to 480s)
     * Lv 21-30: 8 to 11 minutes (480s to 660s)
     * Lv 31-40: 11 to 14 minutes (660s to 840s)
     * etc.
     */
    public static int getUpgradeCooldownSeconds(int level) {
        if (level <= 10) {
            double frac = (level - 1) / 9.0;
            double mins = 1.0 + (frac * 4.0);
            return (int) Math.round(mins * 60.0);
        } else {
            int tier = (level - 1) / 10;
            double baseMins = 5.0 + ((tier - 1) * 3.0);
            double frac = (level - 1 - (tier * 10)) / 9.0;
            double mins = baseMins + (frac * 3.0);
            return (int) Math.round(mins * 60.0);
        }
    }

    public Location getIdealHologramLocation(PetAltar altar) {
        Location lodestoneLoc = altar.getLocation();
        if (lodestoneLoc.getWorld() == null) return lodestoneLoc;

        double cx = lodestoneLoc.getX() + 0.5;
        double cy = lodestoneLoc.getY();
        double cz = lodestoneLoc.getZ() + 0.5;

        if (altar.isTraining()) {
            // Inside the altar chamber, sitting directly above the custom skull (skull at Y=1.55, text lowered to 1.62)
            Location loc = new Location(lodestoneLoc.getWorld(), cx, cy + 1.62, cz);
            loc.setYaw(0f);
            loc.setPitch(0f);
            return loc;
        }

        // Idle state: Hologram is placed OUTSIDE the 3x3 altar and follows the nearest player's side (N, S, E, W)
        // Lowered 1 block down (from 1.70 to 0.70)
        Player nearest = null;
        double minDistanceSq = 144.0; // within 12 blocks
        for (Player p : lodestoneLoc.getWorld().getPlayers()) {
            double dSq = p.getLocation().distanceSquared(new Location(lodestoneLoc.getWorld(), cx, cy + 0.5, cz));
            if (dSq < minDistanceSq) {
                minDistanceSq = dSq;
                nearest = p;
            }
        }

        // Default to South (+Z) if no player nearby
        double offX = 0.0;
        double offZ = 1.75;
        float yaw = 0f; // facing South

        if (nearest != null) {
            double dx = nearest.getLocation().getX() - cx;
            double dz = nearest.getLocation().getZ() - cz;

            if (Math.abs(dx) > Math.abs(dz)) {
                // East (+X) or West (-X)
                if (dx > 0) {
                    offX = 1.75; // East
                    offZ = 0.0;
                    yaw = 270f; // facing East
                } else {
                    offX = -1.75; // West
                    offZ = 0.0;
                    yaw = 90f; // facing West
                }
            } else {
                // South (+Z) or North (-Z)
                if (dz > 0) {
                    offX = 0.0;
                    offZ = 1.75; // South
                    yaw = 0f; // facing South
                } else {
                    offX = 0.0;
                    offZ = -1.75; // North
                    yaw = 180f; // facing North
                }
            }
        }

        Location loc = new Location(lodestoneLoc.getWorld(), cx + offX, cy + 0.70, cz + offZ);
        loc.setYaw(yaw);
        loc.setPitch(0f);
        return loc;
    }

    public void ensureHologram(PetAltar altar) {
        Location lodestoneLoc = altar.getLocation();
        if (!lodestoneLoc.isWorldLoaded() || !lodestoneLoc.getChunk().isLoaded()) return;

        TextDisplay text = altar.getHologramDisplay();
        if (text == null || !text.isValid()) {
            Location textLoc = getIdealHologramLocation(altar);
            text = textLoc.getWorld().spawn(textLoc, TextDisplay.class, t -> {
                t.setPersistent(false);
                t.setBillboard(altar.isTraining() ? Display.Billboard.CENTER : Display.Billboard.FIXED);
                t.setDefaultBackground(false);
                t.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // Transparent background
                t.setShadowed(true);
            });
            altar.setHologramDisplay(text);
        }
    }

    public void spawnFloatingHead(PetAltar altar) {
        Location lodestoneLoc = altar.getLocation();
        if (!lodestoneLoc.isWorldLoaded() || !lodestoneLoc.getChunk().isLoaded()) return;

        if (altar.getFloatingDisplay() != null && altar.getFloatingDisplay().isValid()) {
            altar.getFloatingDisplay().remove();
        }
        if (altar.getBedrockStand() != null && altar.getBedrockStand().isValid()) {
            altar.getBedrockStand().remove();
        }

        PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);

        // Floating Head: Positioned at Y=1.55 (exact vertical center of 2-block-high chamber)
        Location headLoc = lodestoneLoc.clone().add(0.5, 1.55, 0.5);
        ItemDisplay display = headLoc.getWorld().spawn(headLoc, ItemDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.FIXED);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            float scale = 1.25f;
            Transformation t = new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 1f, 0f),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            );
            d.setTransformation(t);
            d.setItemStack(head);
        });
        altar.setFloatingDisplay(display);

        // Bedrock ArmorStand Fallback (GeyserMC)
        Location standLoc = headLoc.clone().subtract(0, 0.70, 0);
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
        altar.setBedrockStand(stand);

        updateAltarHeadVisibility(altar);
    }

    public void updateAltarHeadVisibility(PetAltar altar) {
        if (altar == null) return;
        ItemDisplay display = altar.getFloatingDisplay();
        ArmorStand stand = altar.getBedrockStand();
        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean isBedrock = com.leftycraft.leftypet.util.BedrockUtil.isBedrockPlayer(p);
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

    public void updateAltarHeadVisibilityFor(PetAltar altar, Player player) {
        if (altar == null || player == null || !player.isOnline()) return;
        boolean isBedrock = com.leftycraft.leftypet.util.BedrockUtil.isBedrockPlayer(player);
        ItemDisplay display = altar.getFloatingDisplay();
        ArmorStand stand = altar.getBedrockStand();
        if (display != null && display.isValid()) {
            if (isBedrock) player.hideEntity(plugin, display);
            else player.showEntity(plugin, display);
        }
        if (stand != null && stand.isValid()) {
            if (isBedrock) player.showEntity(plugin, stand);
            else player.hideEntity(plugin, stand);
        }
    }

    public void updateAltarHologram(PetAltar altar) {
        ensureHologram(altar);
        TextDisplay text = altar.getHologramDisplay();
        if (text == null || !text.isValid()) return;

        // Follow player perspective (N, S, E, W outside when idle, inside directly above skull when training)
        Location idealLoc = getIdealHologramLocation(altar);
        if (text.getLocation().distanceSquared(idealLoc) > 0.04 || Math.abs(text.getLocation().getYaw() - idealLoc.getYaw()) > 1.0f) {
            text.teleport(idealLoc);
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(altar.getOwnerUuid());
        String ownerName = owner.getName() != null ? owner.getName() : "Player";
        int discount = (int) altar.getTimeReductionPercent();

        if (!altar.isTraining()) {
            // Idle Altar Hologram: FIXED billboard, 1 block lower, shortened to concise 2 lines
            text.setBillboard(Display.Billboard.FIXED);

            String idleText = "<gradient:#ff9900:#ff5500><b>✦ ᴘᴇᴛ ᴀʟᴛᴀʀ [ʟᴠ." + altar.getAltarLevel() + "] ✦</b></gradient>\n" +
                    "<white>" + ownerName + "</white> <gray>•</gray> <green>-" + discount + "% ᴡᴀᴋᴛᴜ</green>";
            text.text(ColorUtil.component(idleText));
            return;
        }

        // Training Altar Hologram: Merged with training details (no duplicate hologram, Billboard.CENTER)
        text.setBillboard(Display.Billboard.CENTER);

        boolean isOnline = owner.isOnline();
        String statusLine;
        if (altar.isFinished()) {
            statusLine = "<gradient:#43e97b:#38f9d7><b>ᴜᴘɢʀᴀᴅᴇ sᴇʟᴇsᴀɪ!</b></gradient>\n<gray>(ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴜɴᴛᴜᴋ ᴋʟᴀɪᴍ)</gray>";
        } else if (!isOnline) {
            statusLine = "<gradient:#ff416c:#ff4b2b><b>● ᴘʟᴀʏᴇʀ ᴏғғʟɪɴᴇ (ᴛᴇʀᴊᴇᴅᴀ)</b></gradient>\n<gray>sɪsᴀ ᴡᴀᴋᴛᴜ: </gray><yellow><b>" + altar.getFormattedRemainingTime() + "</b></yellow>";
        } else {
            statusLine = "<yellow>sɪsᴀ ᴡᴀᴋᴛᴜ: </yellow><gradient:#00f2fe:#4facfe><b>" + altar.getFormattedRemainingTime() + "</b></gradient>";
        }

        String full = "<gradient:#ff9900:#ff5500><b>✦ ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ ✦</b></gradient>\n" +
                "<gray>ᴘᴇᴍɪʟɪᴋ: </gray><white>" + ownerName + "</white>\n" +
                "<yellow>ʟᴇᴠᴇʟ: </yellow><gold><b>ʟᴠ." + altar.getAltarLevel() + "</b></gold> <green>(-" + discount + "% ᴡᴀᴋᴛᴜ)</green>\n" +
                "<aqua>ᴛᴀʀɢᴇᴛ: </aqua>" + ColorUtil.getLevelTag(altar.getTargetLevel()) + "\n" +
                statusLine;

        text.text(ColorUtil.component(full));
    }

    private int tickerStep = 0;

    private void startAltarTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickerStep++;
            boolean isSecondTick = (tickerStep % 2 == 0);

            for (PetAltar altar : altars.values()) {
                Location loc = altar.getLocation();
                if (!loc.isWorldLoaded() || !loc.getChunk().isLoaded()) continue;

                if (isSecondTick) {
                    // Subtle ambient particles per level
                    spawnAltarAmbientParticles(altar);
                }

                // Always ensure hologram exists for all altars
                ensureHologram(altar);

                if (altar.isTraining()) {
                    if (altar.getFloatingDisplay() == null || !altar.getFloatingDisplay().isValid()) {
                        spawnFloatingHead(altar);
                    }

                    // Smooth rotation of head on Y axis
                    if (altar.getFloatingDisplay() != null) {
                        Location dLoc = altar.getFloatingDisplay().getLocation();
                        dLoc.setYaw((dLoc.getYaw() + 2.0f) % 360f);
                        altar.getFloatingDisplay().teleport(dLoc);
                    }

                    Player owner = Bukkit.getPlayer(altar.getOwnerUuid());
                    boolean isOnline = (owner != null && owner.isOnline());

                    // Altar training ONLY progresses while player is online!
                    if (isSecondTick && isOnline && !altar.isFinished()) {
                        altar.decrementRemainingSeconds();
                        if (altar.isFinished()) {
                            owner.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                                    "<gradient:#43e97b:#38f9d7><b>ᴜᴘɢʀᴀᴅᴇ sᴇʟᴇsᴀɪ!</b> Pet kamu di altar sudah siap diklaim.</gradient>"));
                            owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
                        }
                    }
                } else {
                    // Remove floating head when not training
                    if (altar.getFloatingDisplay() != null && altar.getFloatingDisplay().isValid()) {
                        altar.getFloatingDisplay().remove();
                        altar.setFloatingDisplay(null);
                    }
                }

                // Update text display (unified hologram with player perspective tracking)
                updateAltarHologram(altar);
            }
        }, 10L, 10L); // 0.5 second interval for responsive perspective tracking
    }

    public boolean dismantleAltarByAdmin(org.bukkit.command.CommandSender sender, OfflinePlayer target) {
        PetAltar altar = getAltarByOwner(target.getUniqueId());
        if (altar == null) {
            return false;
        }

        if (altar.isTraining()) {
            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null && targetPlayer.isOnline()) {
                cancelTraining(targetPlayer);
            } else {
                altar.setTraining(false);
                altar.removeEntities();
            }
        }

        Location loc = altar.getLocation();
        altar.removeEntities();
        structureManager.removeStructure(loc);
        removeAltar(loc);

        sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴍᴇɴɢʜᴀᴘᴜs ᴀʟᴛᴀʀ ᴍɪʟɪᴋ <yellow>" + (target.getName() != null ? target.getName() : "Player") + "</yellow>!</gradient>"));

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                    "<gradient:#ff5f6d:#ffc371>ᴀʟᴛᴀʀ ᴋᴀᴍᴜ ᴛᴇʟᴀʜ ᴅɪʜᴀᴘᴜs ᴏʟᴇʜ ᴀᴅᴍɪɴ!</gradient>"));
            onlineTarget.playSound(onlineTarget.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.7f, 1.2f);
        }

        return true;
    }

    private void spawnAltarAmbientParticles(PetAltar altar) {
        Location loc = altar.getLocation().clone().add(0.5, 1.2, 0.5);
        int lvl = altar.getAltarLevel();
        if (lvl == 3) {
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 2, 0.6, 0.4, 0.6, 0.02);
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 1, 0.4, 0.2, 0.4, 0.01);
        } else if (lvl == 2) {
            loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0.6, 0.4, 0.6, 0.01);
            loc.getWorld().spawnParticle(Particle.GLOW, loc, 2, 0.4, 0.3, 0.4, 0.01);
        } else {
            loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 2, 0.6, 0.4, 0.6, 0.02);
            loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 1, 0.4, 0.2, 0.4, 0.01);
        }
    }

    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int min = (seconds % 3600) / 60;
        int sec = seconds % 60;
        if (hours > 0) return hours + "j " + min + "m " + sec + "d";
        if (min > 0) return min + "m " + sec + "d";
        return sec + "d";
    }

    public void loadAltars() {
        if (!altarFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(altarFile);
        ConfigurationSection sec = cfg.getConfigurationSection("altars");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                UUID altarId = UUID.fromString(key);
                UUID ownerUuid = UUID.fromString(sec.getString(key + ".owner"));
                Location loc = sec.getLocation(key + ".location");
                int altarLvl = sec.getInt(key + ".altar-level", 1);
                
                int remainingSec;
                if (sec.contains(key + ".remaining-seconds")) {
                    remainingSec = sec.getInt(key + ".remaining-seconds", 0);
                } else {
                    long finishTime = sec.getLong(key + ".finish", 0L);
                    remainingSec = (int) Math.max(0, (finishTime - System.currentTimeMillis()) / 1000L);
                }

                int targetLvl = sec.getInt(key + ".target-level", 1);
                boolean isTrain = sec.getBoolean(key + ".is-training", false);

                if (loc != null) {
                    PetAltar altar = new PetAltar(altarId, ownerUuid, loc, altarLvl, remainingSec, targetLvl, isTrain);
                    altars.put(loc.getBlock().getLocation(), altar);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading altar: " + e.getMessage());
            }
        }
    }

    public void saveAltars() {
        FileConfiguration cfg = new YamlConfiguration();
        for (PetAltar altar : altars.values()) {
            String key = "altars." + altar.getAltarId().toString();
            cfg.set(key + ".owner", altar.getOwnerUuid().toString());
            cfg.set(key + ".location", altar.getLocation());
            cfg.set(key + ".altar-level", altar.getAltarLevel());
            cfg.set(key + ".remaining-seconds", altar.getRemainingSeconds());
            cfg.set(key + ".target-level", altar.getTargetLevel());
            cfg.set(key + ".is-training", altar.isTraining());
        }
        try {
            cfg.save(altarFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save altars.yml: " + e.getMessage());
        }
    }

    public Map<Location, PetAltar> getAltars() {
        return altars;
    }

    public void removeAllEntities() {
        for (PetAltar altar : altars.values()) {
            altar.removeEntities();
        }
    }
}
