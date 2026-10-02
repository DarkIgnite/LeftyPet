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
    private final NamespacedKey altarKey;
    private final File altarFile;

    public AltarManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.structureManager = new AltarStructureManager(plugin);
        this.altarKey = new NamespacedKey(plugin, "is_pet_altar");
        this.altarFile = new File(plugin.getDataFolder(), "altars.yml");
        loadAltars();
        startAltarTicker();
    }

    public AltarStructureManager getStructureManager() {
        return structureManager;
    }

    public ItemStack createAltarItem() {
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component("<gradient:#ff9900:#ff5500><b>ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)</b></gradient>"));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("&7ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴀʀᴇᴀ &e3x3 &7ᴛᴇʀʙᴜᴋᴀ"));
            lore.add(ColorUtil.component("&7ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴀɴɢᴜɴ ғᴀsɪʟɪᴛᴀs ᴀғᴋ ᴛʀᴀɪɴɪɴɢ!"));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&eᴀʟᴛᴀʀ ʟᴇᴠᴇʟ: &f1 &7(-10% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ)"));
            lore.add(ColorUtil.component("&bʙɪsᴀ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ &7ʜɪɴɢɢᴀ ʟᴇᴠᴇʟ 3 (-30%)"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(altarKey, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }
        return item;
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

        // Put directly into inventory
        player.getInventory().addItem(createAltarItem());
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴀʟᴛᴀʀ 3x3 ʙᴇʀʜᴀsɪʟ ᴅɪʙᴏɴɢᴋᴀʀ ᴅᴀɴ ᴅɪᴍᴀsᴜᴋᴋᴀɴ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ!</gradient>"));
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

        int maxLvl = plugin.getConfigManager().getMaxLevel();
        if (data.getLevel() >= maxLvl) {
            String msg = plugin.getConfigManager().getMessage("altar-max-level").replace("{maxLevel}", String.valueOf(maxLvl));
            player.sendMessage(ColorUtil.component(msg));
            return;
        }

        int currentLevel = data.getLevel();
        int targetLevel = currentLevel + 1;
        int baseSec = plugin.getConfigManager().getUpgradeDuration(currentLevel);
        int finalSec = (int) Math.round(baseSec * altar.getTimeMultiplier());
        long finishTime = System.currentTimeMillis() + (finalSec * 1000L);

        altar.setTraining(true);
        altar.setFinishTimestamp(finishTime);
        altar.setTargetLevel(targetLevel);

        data.setTraining(true);
        data.setCurrentAltarId(altar.getAltarId());
        plugin.getPetManager().despawnPet(uuid);

        spawnAltarDisplays(altar);
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
    }

    private void spawnAltarDisplays(PetAltar altar) {
        Location lodestoneLoc = altar.getLocation();
        if (!lodestoneLoc.isWorldLoaded() || !lodestoneLoc.getChunk().isLoaded()) return;

        altar.removeEntities();

        // 1. Floating Head: Positioned at Y=1.20 (lower middle of glass chamber)
        Location headLoc = lodestoneLoc.clone().add(0.5, 1.20, 0.5);
        ItemDisplay display = headLoc.getWorld().spawn(headLoc, ItemDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.FIXED); // Stays in place!
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            float scale = 1.25f;
            Transformation t = new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 1f, 0f),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            );
            d.setTransformation(t);

            PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());
            PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
            ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
            d.setItemStack(head);
        });
        altar.setFloatingDisplay(display);

        // 2. Hologram Display: placed right above the skull at Y=1.70 (well below roof slabs)
        Location textLoc = lodestoneLoc.clone().add(0.5, 1.70, 0.5);
        TextDisplay text = textLoc.getWorld().spawn(textLoc, TextDisplay.class, t -> {
            t.setPersistent(false);
            t.setBillboard(Display.Billboard.CENTER);
            t.setDefaultBackground(false);
            t.setShadowed(true);
        });
        altar.setHologramDisplay(text);
        updateAltarHologram(altar);
    }

    private void updateAltarHologram(PetAltar altar) {
        TextDisplay text = altar.getHologramDisplay();
        if (text == null || !text.isValid()) return;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(altar.getOwnerUuid());
        String ownerName = owner.getName() != null ? owner.getName() : "Player";

        String statusLine;
        if (altar.isFinished()) {
            statusLine = "<gradient:#43e97b:#38f9d7><b>ᴜᴘɢʀᴀᴅᴇ sᴇʟᴇsᴀɪ!</b></gradient>\n<gray>(ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴜɴᴛᴜᴋ ᴋʟᴀɪᴍ)</gray>";
        } else {
            statusLine = "<yellow>sɪsᴀ ᴡᴀᴋᴛᴜ: </yellow><gradient:#00f2fe:#4facfe><b>" + altar.getFormattedRemainingTime() + "</b></gradient>";
        }

        String full = "<gradient:#ff9900:#ff5500><b>ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (ʟᴠ." + altar.getAltarLevel() + ")</b></gradient>\n" +
                "<gray>ᴘᴇᴍɪʟɪᴋ: </gray><white>" + ownerName + "</white>\n" +
                "<aqua>ᴛᴀʀɢᴇᴛ: </aqua>" + ColorUtil.getLevelTag(altar.getTargetLevel()) + "\n" +
                statusLine;

        text.text(ColorUtil.component(full));
    }

    private void startAltarTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PetAltar altar : altars.values()) {
                Location loc = altar.getLocation();
                if (!loc.isWorldLoaded() || !loc.getChunk().isLoaded()) continue;

                // Subtle ambient particles per level (Revisi 15)
                spawnAltarAmbientParticles(altar);

                if (!altar.isTraining()) continue;

                if (altar.getFloatingDisplay() == null || !altar.getFloatingDisplay().isValid()) {
                    spawnAltarDisplays(altar);
                }

                // Smooth rotation of head on Y axis
                if (altar.getFloatingDisplay() != null) {
                    Location dLoc = altar.getFloatingDisplay().getLocation();
                    dLoc.setYaw((dLoc.getYaw() + 3.0f) % 360f);
                    altar.getFloatingDisplay().teleport(dLoc);
                }

                // Update text
                updateAltarHologram(altar);
            }
        }, 20L, 20L); // 1 second
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
        long min = seconds / 60;
        long sec = seconds % 60;
        if (min > 0) return min + "m " + sec + "s";
        return sec + "s";
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
                long finishTime = sec.getLong(key + ".finish", 0L);
                int targetLvl = sec.getInt(key + ".target-level", 1);
                boolean isTrain = sec.getBoolean(key + ".is-training", false);

                if (loc != null) {
                    PetAltar altar = new PetAltar(altarId, ownerUuid, loc, altarLvl, finishTime, targetLvl, isTrain);
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
            cfg.set(key + ".finish", altar.getFinishTimestamp());
            cfg.set(key + ".target-level", altar.getTargetLevel());
            cfg.set(key + ".is-training", altar.isTraining());
        }
        try {
            cfg.save(altarFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save altars.yml: " + e.getMessage());
        }
    }

    public void removeAllEntities() {
        for (PetAltar altar : altars.values()) {
            altar.removeEntities();
        }
    }
}
