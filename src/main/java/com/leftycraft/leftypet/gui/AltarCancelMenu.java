package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AltarCancelMenu {

    public static final String TITLE = "§8» §b§lʟᴇғᴛʏᴘᴇᴛ §8| §fʙᴀᴛᴀʟᴋᴀɴ ᴛʀᴀɪɴɪɴɢ?";

    public static void open(Player player, PetAltar altar, LeftyPetPlugin plugin) {
        com.leftycraft.leftypet.gui.holder.AltarCancelMenuHolder holder = new com.leftycraft.leftypet.gui.holder.AltarCancelMenuHolder(altar);
        Inventory inv = Bukkit.createInventory(holder, 27, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        ItemStack darkFiller = PetMenu.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack cyanCorner = PetMenu.createItem(Material.CYAN_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkFiller);
        }
        inv.setItem(0, cyanCorner);
        inv.setItem(8, cyanCorner);
        inv.setItem(18, cyanCorner);
        inv.setItem(26, cyanCorner);

        PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());

        // Slot 11: Ambil Pet (Batalkan)
        inv.setItem(11, PetMenu.createItem(Material.RED_TERRACOTTA, true,
                "<gradient:#ff416c:#ff4b2b><b>ᴀᴍʙɪʟ ᴘᴇᴛ (ʙᴀᴛᴀʟᴋᴀɴ)</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢᴀᴍʙɪʟ ᴋᴇᴍʙᴀʟɪ ᴘᴇᴛ ᴋᴀᴍᴜ.",
                "&cᴘᴇʀʜᴀᴛɪᴀɴ: &7Progress waktu yang berjalan akan hilang.",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&a✔ ᴘᴇᴛ ᴀᴋᴀɴ ʟᴀɴɢsᴜɴɢ ᴅɪᴘᴀɴɢɢɪʟ ᴋᴇ sᴀᴍᴘɪɴɢᴍᴜ!",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴀᴛᴀʟᴋᴀɴ"));

        // Slot 13: Info Training
        inv.setItem(13, PetMenu.createItem(Material.CLOCK, true,
                "<gradient:#f7971e:#ffd200><b>sᴇᴅᴀɴɢ ᴛʀᴀɪɴɪɴɢ ᴀғᴋ...</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7• ᴘᴇᴛ: &f" + data.getName(),
                "&7• ᴛᴀʀɢᴇᴛ: &eʟᴇᴠᴇʟ " + altar.getTargetLevel(),
                "&7• sɪsᴀ ᴡᴀᴋᴛᴜ: &b<b>" + altar.getFormattedRemainingTime() + "</b>",
                "&7• ᴀʟᴛᴀʀ: &fʟᴇᴠᴇʟ " + altar.getAltarLevel() + " &a(-" + (int) altar.getTimeReductionPercent() + "%)",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));

        // Slot 15: Tetap Lanjutkan
        inv.setItem(15, PetMenu.createItem(Material.LIME_TERRACOTTA, true,
                "<gradient:#a8ff78:#78ffd6><b>ᴛᴇᴛᴀᴘ ʟᴀɴᴊᴜᴛᴋᴀɴ ᴛʀᴀɪɴɪɴɢ</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ʙɪᴀʀᴋᴀɴ ᴘᴇᴛ ᴛᴇʀᴜs ʙᴇʀʟᴀᴛɪʜ ᴅɪ ᴀʟᴛᴀʀ.",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴᴜᴛᴜᴘ ᴍᴇɴᴜ ɪɴɪ"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 11) {
            player.closeInventory();
            plugin.getAltarManager().cancelTraining(player);
        } else if (slot == 15) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        }
    }
}
