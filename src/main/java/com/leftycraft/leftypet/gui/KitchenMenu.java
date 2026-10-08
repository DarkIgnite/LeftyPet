package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.holder.KitchenMenuHolder;
import com.leftycraft.leftypet.model.PetKitchen;
import com.leftycraft.leftypet.util.ColorUtil;
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

public class KitchenMenu {

    public static final String TITLE = "§8» §6§lᴅᴀᴘᴜʀ ᴍʙɢ §8| §fᴍᴀɴᴀɢᴇᴍᴇɴᴛ";

    public static void open(Player player, PetKitchen kitchen, LeftyPetPlugin plugin) {
        KitchenMenuHolder holder = new KitchenMenuHolder(kitchen);
        Inventory inv = Bukkit.createInventory(holder, 27, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        // Frame
        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack redCorner = createFiller(Material.RED_STAINED_GLASS_PANE);
        ItemStack whiteCorner = createFiller(Material.WHITE_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkFiller);
        }
        inv.setItem(0, redCorner);
        inv.setItem(8, whiteCorner);
        inv.setItem(18, whiteCorner);
        inv.setItem(26, redCorner);

        int lvl = kitchen.getKitchenLevel();
        int portions = kitchen.getCookedPortions();
        int max = kitchen.getMaxCapacity();
        double earnings = portions * kitchen.getRewardPerPortion();

        // Slot 11: Claim Earnings / Distribute Boxes
        if (portions > 0) {
            inv.setItem(11, createItem(Material.GOLD_BLOCK, true,
                    "<gradient:#ffe259:#ffa751><b>🍱 ᴅɪsᴛʀɪʙᴜsɪᴋᴀɴ ɴᴀsɪ ᴋᴏᴛᴀᴋ</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴋɪʀɪᴍ ɴᴀsɪ ᴋᴏᴛᴀᴋ ʏᴀɴɢ sᴜᴅᴀʜ sɪᴀᴘ sᴀᴊɪ",
                    "&7ᴜɴᴛᴜᴋ ᴍᴇɴᴄᴀɪʀᴋᴀɴ ᴜᴀɴɢ sᴜʙsɪᴅɪ ᴍʙɢ!",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7• sɪᴀᴘ ᴅɪsᴛʀɪʙᴜsɪ: &e" + portions + " &7/ &a" + max + " ʙᴏx",
                    "&7• ᴛᴏᴛᴀʟ sᴜʙsɪᴅɪ: &a&l+$" + plugin.getEconomyManager().format(earnings),
                    "&7• ʙᴏɴᴜs ʟᴏɢɪsᴛɪᴋ: &620% ᴄʜᴀɴᴄᴇ",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴋʟᴀɪᴍ sᴜʙsɪᴅɪ!"));
        } else {
            inv.setItem(11, createItem(Material.IRON_BARS, false,
                    "<gradient:#757f9a:#d7dde8><b>🍱 ᴅɪsᴛʀɪʙᴜsɪ ʙᴏx ᴍʙɢ</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ʙᴇʟᴜᴍ ᴀᴅᴀ ɴᴀsɪ ᴋᴏᴛᴀᴋ ʏᴀɴɢ sᴇʟᴇsᴀɪ.",
                    "&7ᴘᴇᴛ sᴇᴅᴀɴɢ ᴍᴇᴍᴀsᴀᴋ ᴅɪ ᴅᴀᴘᴜʀ...",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7• ᴋᴀᴘᴀsɪᴛᴀs: &e0 &7/ &b" + max + " ʙᴏx",
                    "&7• sᴛᴀsɪᴜɴ: &f" + kitchen.getCurrentStation().getDisplayName(),
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&c▶ ᴛᴜɴɢɢᴜ ᴘᴇᴛ sᴇʟᴇsᴀɪ ᴍᴇᴍᴀsᴀᴋ"));
        }

        // Slot 13: Kitchen Status Info
        String lvlTitle = switch (lvl) {
            case 3 -> "sᴇɴᴛʀᴀ ɪɴᴅᴜsᴛʀɪ ᴍʙɢ";
            case 2 -> "ᴅᴀᴘᴜʀ ᴋᴀᴛᴇʀɪɴɢ ʙᴇʀsᴀᴍᴀ";
            default -> "ᴅᴀᴘᴜʀ ʀᴜᴍᴀʜᴀɴ ᴜᴍᴋᴍ";
        };
        inv.setItem(13, createItem(Material.SMOKER, true,
                "<gradient:#ff512f:#dd2476><b>✦ ᴅᴀᴘᴜʀ ᴍʙɢ [ʟᴠ." + lvl + "] ✦</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴛɪᴘᴇ: &f" + lvlTitle,
                "&7sᴛᴀsɪᴜɴ sᴀᴀᴛ ɪɴɪ: &e" + kitchen.getCurrentStation().getDisplayName(),
                "&7sᴛᴀᴛᴜs ᴋᴏᴋɪ: " + (kitchen.isStorageFull() ? "&cʙᴏx ᴘᴇɴᴜʜ!" : (player.isOnline() ? "&aʙᴇᴋᴇʀᴊᴀ" : "&eᴛᴇʀᴊᴇᴅᴀ")),
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7• ʜᴀsɪʟ ᴘᴇʀ ʙᴏx: &a$" + kitchen.getRewardPerPortion(),
                "&7• ᴍᴀᴋsɪᴍᴀʟ ʙᴏx: &b" + max + " ʙᴏx",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ᴏᴛᴏᴍᴀᴛɪs ᴍᴇᴍᴀsᴀᴋ sᴇʟᴀᴍᴀ ᴏɴʟɪɴᴇ!"));

        // Slot 15: Upgrade Kitchen Level
        if (lvl < 3) {
            int nextLvl = lvl + 1;
            double cost = (nextLvl == 2) ? 15000.0 : 35000.0;
            boolean canAfford = plugin.getEconomyManager().hasEnough(player, cost);

            inv.setItem(15, createItem(Material.NETHER_STAR, true,
                    "<gradient:#00c6ff:#0072ff><b>⬆ ᴜᴘɢʀᴀᴅᴇ ᴅᴀᴘᴜʀ ᴋᴇ [ʟᴠ." + nextLvl + "]</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴛɪɴɢᴋᴀᴛᴋᴀɴ ᴇғɪsɪᴇɴsɪ & ʜᴀsɪʟ ᴍᴀsᴀᴋ!",
                    "&7• ʜᴀsɪʟ ʙᴀʀᴜ: &a+$" + (nextLvl == 3 ? "1,000" : "500") + " &7/ ʙᴏx",
                    "&7• ᴋᴀᴘᴀsɪᴛᴀs ʙᴀʀᴜ: &b" + (nextLvl == 3 ? "30" : "20") + " ʙᴏx",
                    "&7• ᴡᴀᴋᴛᴜ ᴍᴀsᴀᴋ: &dʟᴇʙɪʜ ᴄᴇᴘᴀᴛ!",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7• ʙɪᴀʏᴀ: " + (canAfford ? "&a" : "&c") + "$" + plugin.getEconomyManager().format(cost),
                    "&7• sᴀʟᴅᴏᴍᴜ: &f$" + plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player)),
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    (canAfford ? "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ!" : "&c▶ sᴀʟᴅᴏ ᴛɪᴅᴀᴋ ᴄᴜᴋᴜᴘ!")));
        } else {
            inv.setItem(15, createItem(Material.BEACON, true,
                    "<gradient:#ffe259:#ffa751><b>✦ ᴅᴀᴘᴜʀ ᴍᴀᴋsɪᴍᴀʟ (ʟᴠ.3) ✦</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴅᴀᴘᴜʀ ᴍʙɢ sᴜᴅᴀʜ ᴍᴇɴᴊᴀᴅɪ",
                    "&7sᴇɴᴛʀᴀ ɪɴᴅᴜsᴛʀɪ ᴛᴇʀʙᴇsᴀʀ!",
                    "&7• ʜᴀsɪʟ ᴍᴀᴋsɪᴍᴀʟ: &a$1,000 / ʙᴏx",
                    "&7• ᴋᴀᴘᴀsɪᴛᴀs ᴍᴀᴋsɪᴍᴀʟ: &b30 ʙᴏx",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&a✔ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ ᴛᴇʀᴄᴀᴘᴀɪ!"));
        }

        // Slot 22: Dismantle Kitchen
        inv.setItem(22, createItem(Material.BARRIER, false,
                "<gradient:#ff416c:#ff4b2b><b>✕ ʙᴏɴɢᴋᴀʀ ᴅᴀᴘᴜʀ ᴍʙɢ</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ʙᴏɴɢᴋᴀʀ sᴛʀᴜᴋᴛᴜʀ 3x3x4 ᴅᴀɴ",
                "&7ᴋᴇᴍʙᴀʟɪᴋᴀɴ ʙʟᴏᴋ ᴅᴀᴘᴜʀ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ.",
                "&7(ʟᴇᴠᴇʟ ᴅᴀᴘᴜʀ ᴛᴇᴛᴀᴘ ᴛᴇʀsɪᴍᴘᴀɴ)",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴏɴɢᴋᴀʀ!"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof KitchenMenuHolder holder)) return;

        PetKitchen kitchen = holder.getKitchen();
        int slot = event.getRawSlot();

        switch (slot) {
            case 11 -> { // Claim portions
                plugin.getKitchenManager().claimPortions(player, kitchen);
                open(player, kitchen, plugin);
            }
            case 15 -> { // Upgrade
                if (kitchen.getKitchenLevel() < 3) {
                    plugin.getKitchenManager().upgradeKitchen(player, kitchen);
                    open(player, kitchen, plugin);
                }
            }
            case 22 -> { // Dismantle
                player.closeInventory();
                plugin.getKitchenManager().dismantleKitchen(player, kitchen);
            }
        }
    }

    private static ItemStack createItem(Material material, boolean glow, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(name));
            List<Component> compLore = new ArrayList<>();
            for (String l : lore) {
                compLore.add(ColorUtil.component(l));
            }
            meta.lore(compLore);
            if (glow) {
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (Throwable ignored) {}
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }
}
