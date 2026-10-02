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
            headMeta.displayName(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>ᴘʀᴏɢʀᴇsɪ ʟᴇᴠᴇʟ ᴘᴇᴛ</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ᴘᴇᴛ: &f" + data.getName()));
            lore.add(ColorUtil.component("&7ʟᴇᴠᴇʟ sᴀᴀᴛ ɪɴɪ: " + ColorUtil.getLevelTag(data.getLevel())));
            lore.add(ColorUtil.component("&7ᴘʀᴏɢʀᴇss: " + getProgressBar(data.getLevel(), 10) + " &e" + (data.getLevel() * 10) + "%"));
            lore.add(ColorUtil.component("&7ᴋᴇʟᴀs ᴘᴇᴛ: " + data.getPetClass().getDisplayName()));
            double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
            lore.add(ColorUtil.component("&7ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", data.getAttackDamage(classMult))));
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

        // 3. Level Nodes: Row 1 (Levels 1 to 5) -> Slots 11, 12, 13, 14, 15
        int petLevel = data.getLevel();
        int[] row1Slots = {11, 12, 13, 14, 15};
        for (int i = 0; i < 5; i++) {
            int lvl = i + 1;
            inv.setItem(row1Slots[i], createLevelNode(lvl, petLevel, plugin));
        }

        // 4. Row 2: Connecting Beams (Slots 20 to 24)
        int[] row2Connectors = {20, 21, 22, 23, 24};
        for (int i = 0; i < 5; i++) {
            int tierLvl = i + 1;
            Material beamMat = (petLevel >= tierLvl) ? Material.LIME_STAINED_GLASS_PANE
                    : (petLevel == tierLvl - 1) ? Material.YELLOW_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            String beamTitle = (petLevel >= tierLvl) ? "&a✔ ᴛɪᴇʀ " + tierLvl + " ᴛᴇʀᴄᴀᴘᴀɪ"
                    : (petLevel == tierLvl - 1) ? "&e★ sᴇᴅᴀɴɢ ᴍᴇɴᴜᴊᴜ ᴛɪᴇʀ " + tierLvl : "&8🔒 ᴛɪᴇʀ " + tierLvl + " ᴛᴇʀᴋᴜɴᴄɪ";
            inv.setItem(row2Connectors[i], createFiller(beamMat, beamTitle));
        }

        // 5. Level Nodes: Row 3 (Levels 6 to 10) -> Slots 29, 30, 31, 32, 33
        int[] row3Slots = {29, 30, 31, 32, 33};
        for (int i = 0; i < 5; i++) {
            int lvl = i + 6;
            inv.setItem(row3Slots[i], createLevelNode(lvl, petLevel, plugin));
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

    private static ItemStack createLevelNode(int lvl, int currentPetLevel, LeftyPetPlugin plugin) {
        int durationSec = (lvl > 1) ? plugin.getConfigManager().getUpgradeDuration(lvl - 1) : 0;
        double baseDmg = 2.0 + (lvl * 1.5);
        int perkBonus = lvl * 10;

        Material mat;
        boolean glint;
        String name;
        List<Component> lore = new ArrayList<>();
        lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));

        if (currentPetLevel >= lvl) {
            // UNLOCKED / COMPLETED
            mat = (lvl == 10) ? Material.NETHER_STAR : Material.EMERALD_BLOCK;
            glint = true;
            name = (lvl == 10)
                    ? "<gradient:#ff00cc:#333399><b>👑 ʟᴇᴠᴇʟ 10 [ᴍᴀx ᴛɪᴇʀ] ✔</b></gradient>"
                    : "<gradient:#a8ff78:#78ffd6><b>✔ ʟᴇᴠᴇʟ " + lvl + " [ᴛᴇʀᴄᴀᴘᴀɪ]</b></gradient>";

            lore.add(ColorUtil.component("&a✔ sᴛᴀᴛᴜs: sᴜᴅᴀʜ ᴛᴇʀᴄᴀᴘᴀɪ"));
            lore.add(ColorUtil.component("&7• ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", baseDmg)));
            lore.add(ColorUtil.component("&7• ᴇɴᴇʀɢʏ ᴄᴀᴘ: &e100%"));
            lore.add(ColorUtil.component("&7• ᴘᴇʀᴋ ʙᴏɴᴜs: &b+" + perkBonus + "% sᴛᴀᴛs"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&aᴘᴇᴛ ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇɴɢᴜᴀsᴀɪ ʟᴇᴠᴇʟ ɪɴɪ!"));
        } else if (currentPetLevel == lvl - 1) {
            // CURRENT TARGET
            mat = (lvl == 10) ? Material.NETHER_STAR : Material.GOLD_BLOCK;
            glint = true;
            name = (lvl == 10)
                    ? "<gradient:#ff00cc:#333399><b>👑 ʟᴇᴠᴇʟ 10 [ᴍᴀx ᴛɪᴇʀ] ★</b></gradient>"
                    : "<gradient:#f7971e:#ffd200><b>★ ʟᴇᴠᴇʟ " + lvl + " [ᴛᴀʀɢᴇᴛ sᴇʟᴀɴᴊᴜᴛɴʏᴀ]</b></gradient>";

            lore.add(ColorUtil.component("&e★ sᴛᴀᴛᴜs: ᴛᴀʀɢᴇᴛ ᴜᴘɢʀᴀᴅᴇ ʙᴇʀɪᴋᴜᴛɴʏᴀ"));
            lore.add(ColorUtil.component("&7• ᴡᴀᴋᴛᴜ ᴛʀᴀɪɴɪɴɢ: &e" + formatSec(durationSec)));
            lore.add(ColorUtil.component("&7• ʀᴇᴡᴀʀᴅ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", baseDmg) + " &a(+1.5)"));
            lore.add(ColorUtil.component("&7• ʀᴇᴡᴀʀᴅ ʙᴏɴᴜs: &b+" + perkBonus + "% sᴛᴀᴛs"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&6ʟᴇᴛᴀᴋᴋᴀɴ ᴘᴇᴛ ᴅɪ &eᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ &6ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ!"));
        } else {
            // LOCKED
            mat = Material.GRAY_STAINED_GLASS;
            glint = false;
            name = (lvl == 10)
                    ? "&8🔒 ʟᴇᴠᴇʟ 10 [ᴍᴀx ᴛɪᴇʀ]"
                    : "&7🔒 ʟᴇᴠᴇʟ " + lvl + " &8[ᴛᴇʀᴋᴜɴᴄɪ]";

            lore.add(ColorUtil.component("&c🔒 sᴛᴀᴛᴜs: ᴛᴇʀᴋᴜɴᴄɪ"));
            lore.add(ColorUtil.component("&7• sʏᴀʀᴀᴛ: &fᴍᴇɴᴄᴀᴘᴀɪ ʟᴇᴠᴇʟ " + (lvl - 1)));
            lore.add(ColorUtil.component("&7• ᴡᴀᴋᴛᴜ ᴛʀᴀɪɴɪɴɢ: &8" + formatSec(durationSec)));
            lore.add(ColorUtil.component("&7• ʀᴇᴡᴀʀᴅ ᴅᴀᴍᴀɢᴇ: &8" + String.format("%.1f", baseDmg)));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&8ʙᴜᴋᴀ ʟᴇᴠᴇʟ sᴇʙᴇʟᴜᴍɴʏᴀ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ."));
        }

        ItemStack item = new ItemStack(mat, lvl);
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
