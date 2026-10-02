package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
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

public class PetMenu {

    public static final String TITLE = "§8[§bLeftyPet§8] §0Pet Dashboard";

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        ActivePet activePet = plugin.getPetManager().getActivePet(player.getUniqueId());
        boolean isSummoned = (activePet != null && activePet.isValid());

        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.component(TITLE));

        // Border
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Slot 4: Pet Head Profile
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        ItemMeta headMeta = head.getItemMeta();
        if (headMeta != null) {
            headMeta.displayName(ColorUtil.component(data.getName()));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("&7Level: &e" + data.getLevel() + " &7/ &e" + plugin.getConfigManager().getMaxLevel()));
            lore.add(ColorUtil.component("&7Energi: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%"));
            lore.add(ColorUtil.component("&7Class: " + data.getPetClass().getDisplayName()));
            double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
            lore.add(ColorUtil.component("&7Attack Damage: &c" + String.format("%.1f", data.getAttackDamage(classMult))));
            lore.add(ColorUtil.component("&7Status: " + (data.isTraining() ? "&eTraining di Altar" : (isSummoned ? "&aDipanggil" : "&7Disimpan"))));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&eKlik kanan pet sambil bawa makanan"));
            lore.add(ColorUtil.component("&7untuk mengisi energi pet!"));
            headMeta.lore(lore);
            head.setItemMeta(headMeta);
        }
        inv.setItem(4, head);

        // Slot 10: Summon / Dismiss
        if (isSummoned) {
            inv.setItem(10, createItem(Material.REDSTONE_BLOCK, "&c&lSimpan Pet",
                    "&7Klik untuk menyembunyikan pet", "&7ke alam spiritual."));
        } else {
            inv.setItem(10, createItem(Material.EMERALD_BLOCK, "&a&lPanggil Pet",
                    "&7Klik untuk memunculkan pet", "&7di samping bahumu!"));
        }

        // Slot 12: Mount (Saddle)
        boolean canMount = plugin.getMountManager().canMount(data);
        if (canMount) {
            boolean isRiding = isSummoned && activePet.isMounting();
            inv.setItem(12, createItem(Material.SADDLE, isRiding ? "&e&lTurun dari Pet" : "&a&lNaiki Pet (Mount)",
                    "&7Gunakan &eW-A-S-D &7untuk mengendalikan!",
                    "&7Tekan &eShift &7untuk turun."));
        } else {
            inv.setItem(12, createItem(Material.BARRIER, "&c&lMount Terkunci",
                    "&7Bisa dinaiki mulai &eLevel " + plugin.getConfigManager().getMountUnlockLevel() + "&7!",
                    "&7Level pet kamu sekarang: &e" + data.getLevel()));
        }

        // Slot 14: Cosmetic Selector
        inv.setItem(14, createItem(Material.PAINTING, "&d&lKustomisasi Kosmetik",
                "&7Pilih tampilan skin kepala custom", "&7dan efek partikel trail!"));

        // Slot 16: Class Selector
        inv.setItem(16, createItem(Material.NETHER_STAR, "&6&lGanti Kelas (" + data.getPetClass().name() + ")",
                "&7Klik untuk mengganti spesialisasi:",
                "&c• Fighter &7(+Damage)",
                "&a• Support &7(Healing Aura)",
                "&6• Looter &7(Auto Pickup & EXP)",
                "&b• Traveler &7(+Mount Speed)"));

        // Slot 22: Altar Info
        inv.setItem(22, createItem(Material.LODESTONE, "&6&lTraining Altar",
                "&7Gunakan &e/pet altar &7untuk mendapatkan",
                "&7blok Altar tempat AFK training pet!"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());

        switch (slot) {
            case 10 -> { // Summon / Dismiss
                if (plugin.getPetManager().isPetSummoned(player.getUniqueId())) {
                    plugin.getPetManager().despawnPet(player.getUniqueId());
                    player.sendMessage(plugin.getConfigManager().getMessage("pet-dismissed"));
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.4f);
                } else {
                    plugin.getPetManager().summonPet(player);
                }
                open(player, plugin); // refresh
            }
            case 12 -> { // Mount
                if (!plugin.getMountManager().canMount(data)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                    return;
                }
                ActivePet active = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (active == null) {
                    plugin.getPetManager().summonPet(player);
                }
                player.closeInventory();
                plugin.getMountManager().startMount(player);
            }
            case 14 -> { // Cosmetics
                player.closeInventory();
                CosmeticMenu.open(player, plugin);
            }
            case 16 -> { // Cycle Class
                PetClass[] classes = PetClass.values();
                int nextIndex = (data.getPetClass().ordinal() + 1) % classes.length;
                data.setPetClass(classes[nextIndex]);
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§aKelas pet diubah ke: " + data.getPetClass().getDisplayName());
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.4f);
                open(player, plugin);
            }
            case 22 -> { // Altar Info
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        "§7Gunakan perintah §e/pet altar §7untuk mendapatkan Altar!");
            }
        }
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
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
            item.setItemMeta(meta);
        }
        return item;
    }
}
