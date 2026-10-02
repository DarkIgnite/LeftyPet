package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.holder.PetRoadmapMenuHolder;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.model.PetSkin;
import com.leftycraft.leftypet.util.ColorUtil;
import com.leftycraft.leftypet.util.HeadUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PetRoadmapMenu {

    public static final String TITLE = "§8» §b§lʟᴇғᴛʏᴘᴇᴛ §8| §fʟᴇᴠᴇʟ ʀᴏᴀᴅᴍᴀᴘ";

    record MilestoneInfo(int level, String skillName, String skillIcon, Material material, String description, String hitParticle) {}

    private static final MilestoneInfo[] MILESTONES = {
        new MilestoneInfo(10, "Swift Strike", "⚡", Material.IRON_SWORD, "Peluang 25% serangan ganda (instant double hit).", "Crit Sparkles & Sweep Attack"),
        new MilestoneInfo(20, "Vampiric Link", "🩸", Material.REDSTONE, "15% damage pet menyembuhkan HP/Hunger pemilik.", "Soul Flame & Witch Magic"),
        new MilestoneInfo(30, "Arcane Chain", "⛓️", Material.CHAIN, "Serangan memantul ke 2 mob di sekitar target.", "Electric Spark & Copper Zap"),
        new MilestoneInfo(40, "Guardian Aegis", "🛡️", Material.SHIELD, "Saat HP < 30%, memberi Resistance II & Absorption II.", "Dragon Breath & Enchant Glyphs"),
        new MilestoneInfo(50, "Armor Shatter", "💔", Material.ANVIL, "Melemahkan armor musuh (+25% bonus damage player).", "Trial Omen & Heavy Smoke"),
        new MilestoneInfo(60, "Fortune's Favor", "🍀", Material.EMERALD, "Peluang 35% double EXP dan bonus loot drop.", "Emerald Sparkle & Totem Flash"),
        new MilestoneInfo(70, "Overdrive", "🔋", Material.REDSTONE_BLOCK, "Konsumsi energi -50% & auto regen energi pasif.", "Sonic Boom & End Rod Stardust"),
        new MilestoneInfo(80, "Celestial Smite", "☄️", Material.BEACON, "Tiap 5 hit memanggil ledakan petir suci mistik.", "Firework Burst & Flash Shockwave"),
        new MilestoneInfo(90, "Spectral Clone", "👥", Material.ECHO_SHARD, "Memanggil bayangan spirit untuk menembak bersamaan.", "Cosmic Reverse Portal & Sculk Soul"),
        new MilestoneInfo(100, "Mythic Transcendence", "👑", Material.NETHER_STAR, "Aura Speed & Strength pasif + Totem of Undying Savior.", "Cherry Blossom Storm, Glow & Reverse Portal")
    };

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        PetRoadmapMenuHolder holder = new PetRoadmapMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        // 1. Fill base frame
        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack grayFiller = createFiller(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack cyanCorner = createFiller(Material.CYAN_STAINED_GLASS_PANE);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, darkFiller);
        }

        // Cyan corners
        inv.setItem(0, cyanCorner);
        inv.setItem(8, cyanCorner);
        inv.setItem(45, cyanCorner);
        inv.setItem(53, cyanCorner);

        // Gray filler rows
        int[] graySlots = {9, 10, 16, 17, 27, 28, 34, 35};
        for (int s : graySlots) {
            inv.setItem(s, grayFiller);
        }

        // 2. Slot 4: Central Pet Profile & Progression Banner
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        ItemMeta headMeta = head.getItemMeta();
        if (headMeta != null) {
            headMeta.displayName(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>ᴘʀᴏɢʀᴇsɪ ʟᴇᴠᴇʟ ᴘᴇᴛ (1-100)</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ᴘᴇᴛ: &f" + data.getName()));
            lore.add(ColorUtil.component("&7ʟᴇᴠᴇʟ sᴀᴀᴛ ɪɴɪ: " + ColorUtil.getLevelTag(data.getLevel())));
            lore.add(ColorUtil.component("&7ᴘʀᴏɢʀᴇss: " + getProgressBar(data.getLevel(), 100) + " &e" + data.getLevel() + "%"));
            lore.add(ColorUtil.component("&7ᴋᴇʟᴀs ᴘᴇᴛ: " + data.getPetClass().getDisplayName()));
            double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
            lore.add(ColorUtil.component("&7ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", data.getAttackDamage(classMult))));
            int unlockedSkills = Math.min(10, data.getLevel() / 10);
            lore.add(ColorUtil.component("&7sᴋɪʟʟ ᴛᴇʀʙᴜᴋᴀ: &a" + unlockedSkills + "/10 ᴍɪʟᴇsᴛᴏɴᴇs"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ᴛɪɴɢᴋᴀᴛᴋᴀɴ ʟᴇᴠᴇʟ ᴘᴇᴛ ᴅᴇɴɢᴀɴ ᴍᴇʟᴇᴛᴀᴋᴋᴀɴɴʏᴀ"));
            lore.add(ColorUtil.component("&7ᴅɪ &eᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3) &7ᴜɴᴛᴜᴋ ᴀғᴋ ᴛʀᴀɪɴɪɴɢ!"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            headMeta.lore(lore);
            try {
                headMeta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            head.setItemMeta(headMeta);
        }
        inv.setItem(4, head);

        // 3. Row 1: Milestones 1 to 5 (Lv 10, 20, 30, 40, 50) -> Slots 11, 12, 13, 14, 15
        int petLevel = data.getLevel();
        int[] row1Slots = {11, 12, 13, 14, 15};
        for (int i = 0; i < 5; i++) {
            inv.setItem(row1Slots[i], createMilestoneNode(MILESTONES[i], petLevel, plugin));
        }

        // 4. Row 2: Connecting Beams (Slots 20 to 24)
        int[] row2Connectors = {20, 21, 22, 23, 24};
        for (int i = 0; i < 5; i++) {
            int targetLvl = (i + 1) * 10;
            Material beamMat = (petLevel >= targetLvl) ? Material.LIME_STAINED_GLASS_PANE
                    : (petLevel >= targetLvl - 10) ? Material.YELLOW_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            String beamTitle = (petLevel >= targetLvl) ? "&a✔ ᴛɪᴇʀ " + (i + 1) + " ᴛᴇʀᴄᴀᴘᴀɪ"
                    : (petLevel >= targetLvl - 10) ? "&e★ sᴇᴅᴀɴɢ ᴍᴇɴᴜᴊᴜ ᴛɪᴇʀ " + (i + 1) : "&8🔒 ᴛɪᴇʀ " + (i + 1) + " ᴛᴇʀᴋᴜɴᴄɪ";
            inv.setItem(row2Connectors[i], createFiller(beamMat, beamTitle));
        }

        // 5. Row 3: Milestones 6 to 10 (Lv 60, 70, 80, 90, 100) -> Slots 29, 30, 31, 32, 33
        int[] row3Slots = {29, 30, 31, 32, 33};
        for (int i = 0; i < 5; i++) {
            inv.setItem(row3Slots[i], createMilestoneNode(MILESTONES[i + 5], petLevel, plugin));
        }

        // 6. Navigation Footer (Row 5)
        // Slot 45: Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(ColorUtil.component("<red><b>« ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ</b></red>"));
            List<Component> lore = List.of(ColorUtil.component("&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴍᴇɴᴜ ᴜᴛᴀᴍᴀ."));
            backMeta.lore(lore);
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        // Slot 49: Altar Training Info & Shortcut
        ItemStack altarInfo = new ItemStack(Material.LODESTONE);
        ItemMeta altarMeta = altarInfo.getItemMeta();
        if (altarMeta != null) {
            altarMeta.displayName(ColorUtil.component("<gradient:#43e97b:#38f9d7><b>ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ɢᴜɴᴀᴋᴀɴ &e/pet altar &7ᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ ᴀʟᴛᴀʀ!"));
            lore.add(ColorUtil.component("&7• ʟᴇᴠᴇʟ 1: &a-10% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ"));
            lore.add(ColorUtil.component("&7• ʟᴇᴠᴇʟ 2: &a-20% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ"));
            lore.add(ColorUtil.component("&7• ʟᴇᴠᴇʟ 3: &a-30% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&eᴛɪᴘ: ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ ᴀɢᴀʀ ᴘᴇᴛ ɴᴀɪᴋ ʟᴇᴠᴇʟ ʟᴇʙɪʜ ᴄᴇᴘᴀᴛ!"));
            altarMeta.lore(lore);
            try {
                altarMeta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            altarInfo.setItemMeta(altarMeta);
        }
        inv.setItem(49, altarInfo);

        // Slot 53: Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(ColorUtil.component("<red><b>ᴛᴜᴛᴜᴘ</b></red>"));
            close.setItemMeta(closeMeta);
        }
        inv.setItem(53, close);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.6f, 1.2f);
    }

    private static ItemStack createMilestoneNode(MilestoneInfo info, int currentPetLevel, LeftyPetPlugin plugin) {
        int lvl = info.level();
        double baseDmg = 2.0 + (lvl * 0.6);

        Material mat;
        boolean glint;
        String name;
        List<Component> lore = new ArrayList<>();
        lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));

        if (currentPetLevel >= lvl) {
            // UNLOCKED
            mat = (lvl == 100) ? Material.NETHER_STAR : info.material();
            glint = true;
            name = (lvl == 100)
                    ? "<gradient:#ff007f:#7928ca:#00dfd8><b>👑 ʟᴇᴠᴇʟ 100 [ᴍʏᴛʜɪᴄ ᴛɪᴇʀ] ✔</b></gradient>"
                    : "<gradient:#a8ff78:#78ffd6><b>✔ ʟᴇᴠᴇʟ " + lvl + " [" + info.skillIcon() + " " + info.skillName() + "]</b></gradient>";

            lore.add(ColorUtil.component("&a✔ sᴛᴀᴛᴜs: sᴋɪʟʟ ᴀᴋᴛɪғ & ᴛᴇʀʙᴜᴋᴀ"));
            lore.add(ColorUtil.component("&e" + info.skillIcon() + " sᴋɪʟʟ: &f<b>" + info.skillName() + "</b>"));
            lore.add(ColorUtil.component("&7" + info.description()));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&7• ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", baseDmg)));
            lore.add(ColorUtil.component("&7• ʜɪᴛ ᴘᴀʀᴛɪᴄʟᴇ: &b" + info.hitParticle()));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&aᴘᴇᴛ ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇɴɢᴜᴀsᴀɪ ᴋᴇᴋᴜᴀᴛᴀɴ ɪɴɪ!"));
        } else if (currentPetLevel >= lvl - 10) {
            // CURRENT TARGET TIER
            mat = (lvl == 100) ? Material.NETHER_STAR : Material.GOLD_BLOCK;
            glint = true;
            name = (lvl == 100)
                    ? "<gradient:#ff007f:#7928ca><b>👑 ʟᴇᴠᴇʟ 100 [ᴍʏᴛʜɪᴄ ᴛɪᴇʀ] ★</b></gradient>"
                    : "<gradient:#f7971e:#ffd200><b>★ ʟᴇᴠᴇʟ " + lvl + " [" + info.skillIcon() + " " + info.skillName() + "]</b></gradient>";

            lore.add(ColorUtil.component("&e★ sᴛᴀᴛᴜs: sᴇᴅᴀɴɢ ᴅɪᴛᴜᴊᴜ (" + currentPetLevel + "/" + lvl + ")"));
            lore.add(ColorUtil.component("&e" + info.skillIcon() + " sᴋɪʟʟ: &f<b>" + info.skillName() + "</b>"));
            lore.add(ColorUtil.component("&7" + info.description()));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&7• ᴛᴀʀɢᴇᴛ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", baseDmg)));
            lore.add(ColorUtil.component("&7• ʜɪᴛ ᴘᴀʀᴛɪᴄʟᴇ: &b" + info.hitParticle()));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&6ᴜᴘɢʀᴀᴅᴇ ᴘᴇᴛ ᴅɪ &eᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ &6ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ sᴋɪʟʟ!"));
        } else {
            // LOCKED
            mat = Material.GRAY_STAINED_GLASS;
            glint = false;
            name = "&7🔒 ʟᴇᴠᴇʟ " + lvl + " [" + info.skillIcon() + " " + info.skillName() + "] &8[ᴛᴇʀᴋᴜɴᴄɪ]";

            lore.add(ColorUtil.component("&c🔒 sᴛᴀᴛᴜs: ᴛᴇʀᴋᴜɴᴄɪ"));
            lore.add(ColorUtil.component("&8" + info.skillIcon() + " sᴋɪʟʟ: &7<b>" + info.skillName() + "</b>"));
            lore.add(ColorUtil.component("&8" + info.description()));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&7• sʏᴀʀᴀᴛ: &fᴍᴇɴᴄᴀᴘᴀɪ ʟᴇᴠᴇʟ " + lvl));
            lore.add(ColorUtil.component("&7• ʜɪᴛ ᴘᴀʀᴛɪᴄʟᴇ: &8" + info.hitParticle()));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&8ʙᴜᴋᴀ ᴛɪᴇʀ sᴇʙᴇʟᴜᴍɴʏᴀ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ."));
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(name));
            meta.lore(lore);
            try {
                meta.setEnchantmentGlintOverride(glint);
            } catch (Throwable ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String getProgressBar(int current, int max) {
        int totalBars = 10;
        int active = Math.min(totalBars, Math.max(0, current));
        StringBuilder sb = new StringBuilder("<#00f2fe>[");
        for (int i = 0; i < totalBars; i++) {
            if (i < active) {
                sb.append("■");
            } else {
                if (i == active) sb.append("<dark_gray>");
                sb.append("■");
            }
        }
        sb.append("<#00f2fe>]");
        return sb.toString();
    }

    private static String formatSec(int seconds) {
        if (seconds <= 0) return "ɪɴsᴛᴀɴ";
        int m = seconds / 60;
        int s = seconds % 60;
        if (m > 0 && s > 0) return m + "m " + s + "s";
        if (m > 0) return m + "m";
        return s + "s";
    }

    private static ItemStack createFiller(Material mat) {
        return createFiller(mat, " ");
    }

    private static ItemStack createFiller(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 45) {
            // Back button
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            PetMenu.open(player, plugin);
        } else if (slot == 53) {
            // Close button
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
            player.closeInventory();
        } else if (slot == 49) {
            // Altar info
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ɢᴜɴᴀᴋᴀɴ &e/pet altar &fᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ ʙʟᴏᴋ ᴀʟᴛᴀʀ ᴛʀᴀɪɴɪɴɢ (3x3)!</gradient>"));
        } else if ((slot >= 11 && slot <= 15) || (slot >= 29 && slot <= 33)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
        }
    }
}
