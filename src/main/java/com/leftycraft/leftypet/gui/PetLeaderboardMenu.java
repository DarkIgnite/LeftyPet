package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.holder.PetLeaderboardMenuHolder;
import com.leftycraft.leftypet.model.PetLeaderboardEntry;
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

public class PetLeaderboardMenu {

    public static final String TITLE = "§8» §e§lᴛᴏᴘ 10 ᴘᴇᴛ ʟᴇᴠᴇʟ §8| §fʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ";

    private static final int[] SLOTS = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetLeaderboardMenuHolder holder = new PetLeaderboardMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        ItemStack darkFiller = PetMenu.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack goldFiller = PetMenu.createFiller(Material.YELLOW_STAINED_GLASS_PANE);

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, darkFiller);
        }
        inv.setItem(0, goldFiller);
        inv.setItem(8, goldFiller);
        inv.setItem(27, goldFiller);
        inv.setItem(35, goldFiller);

        // Slot 4: Info Trophy
        inv.setItem(4, PetMenu.createItem(Material.NETHER_STAR, true,
                "<gradient:#ffe259:#ffa751><b>✦ ᴛᴏᴘ 10 ᴘᴇᴛ ʟᴇᴠᴇʟ ✦</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7Daftar 10 Pet terkuat dengan level tertinggi",
                "&7di seluruh server Survival Season 7!",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e• Update otomatis berkala",
                "&7• Tingkatkan level pet kamu di Training Altar!"));

        List<PetLeaderboardEntry> topPets = plugin.getLeaderboardManager().getTopPets(10);

        for (int i = 0; i < 10; i++) {
            int slot = SLOTS[i];
            int rank = i + 1;

            if (i < topPets.size()) {
                PetLeaderboardEntry entry = topPets.get(i);
                inv.setItem(slot, createEntryItem(entry, rank, plugin));
            } else {
                inv.setItem(slot, PetMenu.createItem(Material.GRAY_DYE, false,
                        "&8#" + rank + " &7- &o(Kosong)",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&8Belum ada pemain di peringkat ini."));
            }
        }

        // Slot 31: Back to Pet Menu
        inv.setItem(31, PetMenu.createItem(Material.ARROW, false,
                "&c&l◀ ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴍᴇɴᴜ ᴘᴇᴛ",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7Klik untuk kembali ke dashboard pet."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.7f, 1.2f);
    }

    private static ItemStack createEntryItem(PetLeaderboardEntry entry, int rank, LeftyPetPlugin plugin) {
        PetSkin skin = plugin.getConfigManager().getSkin(entry.skinKey());
        ItemStack item = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String rankPrefix = switch (rank) {
                case 1 -> "<gradient:#ffe259:#ffa751><b>🥇 #1 " + entry.playerName() + "</b></gradient>";
                case 2 -> "<gradient:#e0eafc:#cfdef3><b>🥈 #2 " + entry.playerName() + "</b></gradient>";
                case 3 -> "<gradient:#f7971e:#ffd200><b>🥉 #3 " + entry.playerName() + "</b></gradient>";
                default -> "<white><b>#" + rank + " " + entry.playerName() + "</b></white>";
            };

            meta.displayName(ColorUtil.component(rankPrefix));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7• ᴘᴇᴍɪʟɪᴋ: &e" + entry.playerName()));
            lore.add(ColorUtil.component("&7• ɴᴀᴍᴀ ᴘᴇᴛ: &f" + entry.petName()));
            lore.add(ColorUtil.component("&7• ʟᴇᴠᴇʟ ᴘᴇᴛ: " + ColorUtil.getLevelTag(entry.level())));
            lore.add(ColorUtil.component("&7• ᴋᴇʟᴀs: " + entry.petClass().getDisplayName()));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            meta.lore(lore);
            try {
                if (rank <= 3) meta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 31) {
            PetMenu.open(player, plugin);
        }
    }
}
