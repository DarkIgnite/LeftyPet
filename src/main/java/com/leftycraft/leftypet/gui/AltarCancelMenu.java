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

    public static final String TITLE = "§8[§bʟᴇғᴛʏᴘᴇᴛ§8] §0ʙᴀᴛᴀʟᴋᴀɴ ᴛʀᴀɪɴɪɴɢ?";

    public static void open(Player player, PetAltar altar, LeftyPetPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.component(TITLE));

        ItemStack filler = PetMenu.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());

        // Slot 11: Ambil Pet (Batalkan)
        inv.setItem(11, PetMenu.createItem(Material.RED_TERRACOTTA,
                "&c&lᴀᴍʙɪʟ ᴘᴇᴛ &7(ʙᴀᴛᴀʟᴋᴀɴ ᴛʀᴀɪɴɪɴɢ)",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢᴀᴍʙɪʟ ᴋᴇᴍʙᴀʟɪ ᴘᴇᴛ ᴋᴀᴍᴜ.",
                "&eᴘᴇʀʜᴀᴛɪᴀɴ: &7Progress waktu yang berjalan akan hilang.",
                "&aᴘᴇᴛ ᴀᴋᴀɴ ʟᴀɴɢsᴜɴɢ ᴅɪᴘᴀɴɢɢɪʟ ᴋᴇ sᴀᴍᴘɪɴɢᴍᴜ!"));

        // Slot 13: Info Training
        inv.setItem(13, PetMenu.createItem(Material.CLOCK,
                "&6&lsᴇᴅᴀɴɢ ᴛʀᴀɪɴɪɴɢ...",
                "&7ᴘᴇᴛ: &f" + data.getName(),
                "&7ᴛᴀʀɢᴇᴛ: &eʟᴇᴠᴇʟ " + altar.getTargetLevel(),
                "&7sɪsᴀ ᴡᴀᴋᴛᴜ: &b" + altar.getFormattedRemainingTime(),
                "&7ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ: &e" + altar.getAltarLevel() + " &7(-" + (int) altar.getTimeReductionPercent() + "%)"));

        // Slot 15: Tetap Lanjutkan
        inv.setItem(15, PetMenu.createItem(Material.LIME_TERRACOTTA,
                "&a&lᴛᴇᴛᴀᴘ ʟᴀɴᴊᴜᴛᴋᴀɴ ᴛʀᴀɪɴɪɴɢ",
                "&7ʙɪᴀʀᴋᴀɴ ᴘᴇᴛ ᴛᴇʀᴜs ʙᴇʀʟᴀᴛɪʜ ᴅɪ ᴀʟᴛᴀʀ.",
                "&7ᴛᴜᴛᴜᴘ ᴍᴇɴᴜ ɪɴɪ."));

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
