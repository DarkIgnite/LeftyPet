package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.holder.KitchenMenuHolder;
import com.leftycraft.leftypet.model.PetData;
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

    public static final String TITLE = "<gradient:#4facfe:#00f2fe><b>ᴅᴀᴘᴜʀ ᴍʙɢ</b></gradient> <dark_gray>|</dark_gray> <white>ᴍᴀɴᴀɢᴇᴍᴇɴᴛ</white>";

    public static void open(Player player, PetKitchen kitchen, LeftyPetPlugin plugin) {
        KitchenMenuHolder holder = new KitchenMenuHolder(kitchen);
        Inventory inv = Bukkit.createInventory(holder, 27, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        // Fillers
        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack lightBlueCorner = createFiller(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemStack cyanCorner = createFiller(Material.CYAN_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkFiller);
        }
        inv.setItem(0, lightBlueCorner);
        inv.setItem(8, cyanCorner);
        inv.setItem(18, cyanCorner);
        inv.setItem(26, lightBlueCorner);

        PetData data = plugin.getPetManager().getPetData(kitchen.getOwnerUuid());
        boolean assigned = kitchen.isPetAssigned();

        // Slot 11: Assign / Recall Pet
        if (!assigned) {
            inv.setItem(11, createItem(Material.EMERALD_BLOCK, true,
                    "<gradient:#4facfe:#00f2fe><b>▶ ᴛᴜɢᴀsᴋᴀɴ ᴘᴇᴛ ᴊᴀᴅɪ ᴋᴏᴋɪ</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴛᴜɢᴀsᴋᴀɴ ᴘᴇᴛ ᴋᴀᴍᴜ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀsᴀᴋ ᴅɪ ᴅᴀᴘᴜʀ ɪɴɪ.",
                    "&7ᴘᴇᴛ ᴀᴋᴀɴ ʙᴇʀᴋᴇʟɪʟɪɴɢ ᴍᴇᴍᴀsᴀᴋ ᴅɪ ғᴜʀɴᴀᴄᴇ,",
                    "&7ᴍᴇɴɢᴇᴍᴀs ᴅɪ ᴍᴇᴊᴀ, & ᴍᴇʟᴀʏᴀɴɪ ᴅɪ ᴊᴇɴᴅᴇʟᴀ!",
                    "&7<i>(ᴘᴇᴛ ʏᴀɴɢ sᴇᴅᴀɴɢ ᴍᴇɴɢɪᴋᴜᴛɪ ᴀᴋᴀɴ ᴏᴛᴏᴍᴀᴛɪs ᴅɪsɪᴍᴘᴀɴ)</i>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴᴜɢᴀsᴋᴀɴ ᴘᴇᴛ!"));
        } else {
            inv.setItem(11, createItem(Material.REDSTONE_BLOCK, true,
                    "<gradient:#ff5f6d:#ffc371><b>⏹ ᴛᴀʀɪᴋ ᴘᴇᴛ ᴋᴇᴍʙᴀʟɪ</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴘᴇᴛ sᴀᴀᴛ ɪɴɪ sᴇᴅᴀɴɢ ʙᴇᴋᴇʀᴊᴀ sᴇʙᴀɢᴀɪ ᴋᴏᴋɪ.",
                    "&7• sᴛᴀᴛᴜs: &f" + kitchen.getCurrentStation().getDisplayName(),
                    "&7• ᴇɴᴇʀɢɪ: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴᴀʀɪᴋ ᴘᴇᴛ ᴋᴇᴍʙᴀʟɪ!"));
        }

        // Slot 13: Kitchen Status & Progress
        String stationText = assigned ? kitchen.getCurrentStation().getDisplayName() : "&cᴛɪᴅᴀᴋ ᴀᴋᴛɪғ";
        inv.setItem(13, createItem(Material.SMOKER, true,
                "<gradient:#4facfe:#00f2fe><b>✦ sᴛᴀᴛᴜs ᴅᴀᴘᴜʀ & ᴋᴏᴋɪ ✦</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7• ᴘᴇᴍɪʟɪᴋ: &f" + kitchen.getCachedOwnerName(),
                "&7• ʜᴀᴅᴀᴘ ɢᴇᴅᴜɴɢ: &e" + kitchen.getFacing(),
                "&7• sᴛᴀᴛᴜs ᴋᴏᴋɪ: &f" + stationText,
                "&7• ᴇɴᴇʀɢɪ ᴘᴇᴛ: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%",
                "&7• ᴛᴏᴛᴀʟ ᴘᴇsᴀɴᴀɴ: &e" + kitchen.getCompletedOrders() + " ʙᴏx",
                "&7• ᴜᴀɴɢ ᴅɪ ᴋᴀsɪʀ: &a+$" + plugin.getEconomyManager().format(kitchen.getStoredEarnings()),
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e💡 ᴋʟɪᴋ ᴋᴀɴᴀɴ ᴘᴇᴛ ᴅɪ ᴅᴀᴘᴜʀ ᴅᴇɴɢᴀɴ ᴍᴀᴋᴀɴᴀɴ ᴜɴᴛᴜᴋ ɪsɪ ᴇɴᴇʀɢɪ!"));

        // Slot 15: Claim Cashier Earnings
        double earnings = kitchen.getStoredEarnings();
        if (earnings > 0) {
            inv.setItem(15, createItem(Material.GOLD_BLOCK, true,
                    "<gradient:#4facfe:#00f2fe><b>💰 ᴋʟᴀɪᴍ ᴜᴀɴɢ ᴋᴀsɪʀ (+$" + plugin.getEconomyManager().format(earnings) + ")</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴛᴀʀɪᴋ ᴜᴀɴɢ ʜᴀsɪʟ ᴘᴇɴᴊᴜᴀʟᴀɴ ᴘᴇsᴀɴᴀɴ ᴍʙɢ",
                    "&7ʟᴀɴɢsᴜɴɢ ᴋᴇ sᴀʟᴅᴏ ᴀᴋᴜɴ ᴋᴀᴍᴜ!",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7• ᴛᴏᴛᴀʟ ᴋᴀsɪʀ: &a&l+$" + plugin.getEconomyManager().format(earnings),
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴᴄᴀɪʀᴋᴀɴ!"));
        } else {
            inv.setItem(15, createItem(Material.IRON_BARS, false,
                    "<gradient:#757f9a:#d7dde8><b>💰 ᴋᴀsɪʀ ᴅᴀᴘᴜʀ ᴋᴏsᴏɴɢ</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ʙᴇʟᴜᴍ ᴀᴅᴀ ᴘᴇsᴀɴᴀɴ ʏᴀɴɢ sᴇʟᴇsᴀɪ ᴅɪᴀɴᴛᴀʀ.",
                    "&7ᴘᴇᴛ ᴀᴋᴀɴ ᴍᴇɴʏᴇʀᴀʜᴋᴀɴ ᴘᴇsᴀɴᴀɴ ᴅɪ ᴊᴇɴᴅᴇʟᴀ",
                    "&7ᴅᴀɴ ᴍᴇɴᴀᴍʙᴀʜ &a+$100 &7sᴇᴛɪᴀᴘ sɪᴋʟᴜs sᴇʟᴇsᴀɪ.",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&c▶ ᴛᴜɴɢɢᴜ ᴘᴇᴛ ᴍᴇɴʏᴇʟᴇsᴀɪᴋᴀɴ ᴘᴇsᴀɴᴀɴ"));
        }

        // Slot 22: Dismantle Building
        inv.setItem(22, createItem(Material.BARRIER, false,
                "<red><b>⚠ ʙᴏɴɢᴋᴀʀ ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ</b></red>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ʜᴀᴘᴜs sᴇʟᴜʀᴜʜ ɢᴇᴅᴜɴɢ ᴅᴀᴘᴜʀ (14x7x13) ɪɴɪ",
                "&7ᴅᴀɴ ᴋᴇᴍʙᴀʟɪᴋᴀɴ ɪᴛᴇᴍ ʙᴜɪʟᴅɪɴɢ ᴋᴇ ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ.",
                "&7<i>(sɪsᴀ ᴜᴀɴɢ ᴋᴀsɪʀ ᴀᴋᴀɴ ᴏᴛᴏᴍᴀᴛɪs ᴅɪᴋʟᴀɪᴍ)</i>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴏɴɢᴋᴀʀ!"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.6f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof KitchenMenuHolder holder)) return;

        PetKitchen kitchen = holder.getKitchen();
        if (kitchen == null) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 11 -> { // Assign / Recall Pet
                if (!kitchen.isPetAssigned()) {
                    plugin.getKitchenManager().assignPet(player, kitchen);
                } else {
                    plugin.getKitchenManager().recallPet(player, kitchen);
                }
                open(player, kitchen, plugin);
            }
            case 15 -> { // Claim Earnings
                plugin.getKitchenManager().claimEarnings(player, kitchen);
                open(player, kitchen, plugin);
            }
            case 22 -> { // Dismantle Building
                player.closeInventory();
                plugin.getKitchenManager().dismantleKitchen(player, kitchen);
            }
        }
    }

    public static ItemStack createItem(Material material, boolean glint, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(name));
            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String l : lore) {
                    loreList.add(ColorUtil.component(l));
                }
                meta.lore(loreList);
            }
            if (glint) {
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (Throwable ignored) {}
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(" "));
            item.setItemMeta(meta);
        }
        return item;
    }
}
