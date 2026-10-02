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

    public static final String TITLE = "§8[§bAltar§8] §0Batalkan Training?";

    public static void open(Player player, PetAltar altar, LeftyPetPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.component(TITLE));

        ItemStack filler = PetMenu.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        PetData data = plugin.getPetManager().getPetData(altar.getOwnerUuid());

        // Slot 11: Ambil Pet (Batalkan)
        inv.setItem(11, PetMenu.createItem(Material.RED_TERRACOTTA,
                "&c&lAmbil Pet &7(Batalkan Training)",
                "&7Klik untuk mengambil kembali pet kamu.",
                "&ePerhatian: &7Progress waktu yang berjalan akan hilang.",
                "&aPet akan langsung dipanggil kembali ke sampingmu."));

        // Slot 13: Info Training
        inv.setItem(13, PetMenu.createItem(Material.CLOCK,
                "&6&lSedang Training...",
                "&7Pet: &f" + data.getName(),
                "&7Target: &eLevel " + altar.getTargetLevel(),
                "&7Sisa Waktu: &b" + altar.getFormattedRemainingTime(),
                "&7Altar Level: &e" + altar.getAltarLevel() + " &7(-" + (int) altar.getTimeReductionPercent() + "%)"));

        // Slot 15: Tetap Lanjutkan
        inv.setItem(15, PetMenu.createItem(Material.LIME_TERRACOTTA,
                "&a&lTetap Lanjutkan Training",
                "&7Biarkan pet terus berlatih di Altar.",
                "&7Tutup menu ini."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 11) { // Batalkan & Ambil
            player.closeInventory();
            plugin.getAltarManager().cancelTraining(player);
        } else if (slot == 15) { // Lanjutkan
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
        }
    }
}
