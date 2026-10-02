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
    private final Map<Location, PetAltar> altars = new ConcurrentHashMap<>();
    private final NamespacedKey altarKey;
    private final File altarFile;

    private float spinAngle = 0f;

    public AltarManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        this.altarKey = new NamespacedKey(plugin, "is_pet_altar");
        this.altarFile = new File(plugin.getDataFolder(), "altars.yml");
        loadAltars();
        startAltarTicker();
    }

    public ItemStack createAltarItem() {
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component("&6&lPet Training Altar"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("&7Tempatkan di tanah dan letakkan"));
            lore.add(ColorUtil.component("&7pet kamu untuk AFK training!"));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&eKlik kanan &7untuk interaksi."));
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

    public void registerAltar(UUID altarId, UUID ownerUuid, Location loc, long finishTimestamp, int targetLevel) {
        PetAltar altar = new PetAltar(altarId, ownerUuid, loc, finishTimestamp, targetLevel);
        altars.put(loc.getBlock().getLocation(), altar);
        spawnAltarDisplays(altar);
        saveAltars();
    }

    public PetAltar getAltarAt(Location loc) {
        return altars.get(loc.getBlock().getLocation());
    }

    public void removeAltar(Location loc) {
        PetAltar altar = altars.remove(loc.getBlock().getLocation());
        if (altar != null) {
            altar.removeEntities();
            saveAltars();
        }
    }

    public void startTraining(Player player, Location altarLoc) {
        UUID uuid = player.getUniqueId();
        PetData data = plugin.getPetManager().getPetData(uuid);

        if (data.isTraining()) {
            player.sendMessage(plugin.getConfigManager().getMessage("altar-already-training"));
            return;
        }

        int maxLvl = plugin.getConfigManager().getMaxLevel();
        if (data.getLevel() >= maxLvl) {
            String msg = plugin.getConfigManager().getMessage("altar-max-level")
                    .replace("{maxLevel}", String.valueOf(maxLvl));
            player.sendMessage(msg);
            return;
        }

        int currentLevel = data.getLevel();
        int targetLevel = currentLevel + 1;
        int durationSec = plugin.getConfigManager().getUpgradeDuration(currentLevel);
        long finishTime = System.currentTimeMillis() + (durationSec * 1000L);

        UUID altarId = UUID.randomUUID();
        data.setTraining(true);
        data.setCurrentAltarId(altarId);
        plugin.getPetManager().despawnPet(uuid);

        registerAltar(altarId, uuid, altarLoc, finishTime, targetLevel);

        String timeStr = formatDuration(durationSec);
        String msg = plugin.getConfigManager().getMessage("altar-started")
                .replace("{targetLevel}", String.valueOf(targetLevel))
                .replace("{time}", timeStr);
        player.sendMessage(msg);
        player.playSound(altarLoc, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.3f);
    }

    public void claimTraining(Player player, PetAltar altar) {
        UUID uuid = player.getUniqueId();
        if (!altar.getOwnerUuid().equals(uuid)) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cIni bukan altar training milikmu!");
            return;
        }

        if (!altar.isFinished()) {
            String msg = plugin.getConfigManager().getMessage("altar-in-progress")
                    .replace("{time}", altar.getFormattedRemainingTime());
            player.sendMessage(msg);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        PetData data = plugin.getPetManager().getPetData(uuid);
        data.setLevel(altar.getTargetLevel());
        data.setEnergy(100.0);
        data.setTraining(false);
        data.setCurrentAltarId(null);

        removeAltar(altar.getLocation());

        String msg = plugin.getConfigManager().getMessage("altar-claimed")
                .replace("{level}", String.valueOf(data.getLevel()));
        player.sendMessage(msg);

        // Fanfare effects
        player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, altar.getLocation().add(0.5, 1.2, 0.5), 40, 0.4, 0.4, 0.4, 0.2);

        // Auto summon
        plugin.getPetManager().summonPet(player);
    }

    private void spawnAltarDisplays(PetAltar altar) {
        Location baseLoc = altar.getLocation().clone().add(0.5, 1.2, 0.5);
        if (!baseLoc.isWorldLoaded() || !baseLoc.getChunk().isLoaded()) return;

        altar.removeEntities();

        // 1. Floating Head on Altar
        ItemDisplay display = baseLoc.getWorld().spawn(baseLoc, ItemDisplay.class, d -> {
            d.setPersistent(false);
            d.setBillboard(Display.Billboard.CENTER);
            float scale = plugin.getConfigManager().getPetScale();
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

        // 2. Hologram Display above Head
        TextDisplay text = baseLoc.getWorld().spawn(baseLoc.clone().add(0, 0.75, 0), TextDisplay.class, t -> {
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
            statusLine = "&a&lUPGRADE SELESAI!\n&7(Klik kanan untuk klaim)";
        } else {
            statusLine = "&eSisa Waktu: &b" + altar.getFormattedRemainingTime();
        }

        String full = "&6&lPET TRAINING ALTAR\n" +
                "&7Pemilik: &f" + ownerName + "\n" +
                "&bTarget: &eLevel " + altar.getTargetLevel() + "\n" +
                statusLine;

        text.text(ColorUtil.component(full));
    }

    private void startAltarTicker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            spinAngle += 0.08f;
            for (PetAltar altar : altars.values()) {
                Location loc = altar.getLocation();
                if (!loc.isWorldLoaded() || !loc.getChunk().isLoaded()) continue;

                if (altar.getFloatingDisplay() == null || !altar.getFloatingDisplay().isValid()) {
                    spawnAltarDisplays(altar);
                }

                // Smooth rotation of head
                if (altar.getFloatingDisplay() != null) {
                    Location dLoc = altar.getFloatingDisplay().getLocation();
                    dLoc.setYaw((dLoc.getYaw() + 3.0f) % 360f);
                    altar.getFloatingDisplay().teleport(dLoc);
                }

                // Update text every 20 ticks
                updateAltarHologram(altar);

                // Spawn subtle training runes
                loc.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0.5, 1.2, 0.5), 2, 0.2, 0.2, 0.2, 0.02);
            }
        }, 20L, 20L); // Every 1 second
    }

    private String formatDuration(int seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;
        if (min > 0) {
            return min + "m " + sec + "s";
        }
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
                long finishTime = sec.getLong(key + ".finish");
                int targetLvl = sec.getInt(key + ".target-level");

                if (loc != null) {
                    PetAltar altar = new PetAltar(altarId, ownerUuid, loc, finishTime, targetLvl);
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
            cfg.set(key + ".finish", altar.getFinishTimestamp());
            cfg.set(key + ".target-level", altar.getTargetLevel());
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
