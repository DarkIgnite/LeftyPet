package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
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

import java.util.*;

public class CosmeticMenu {

    public static final String TITLE = "§8[§bLeftyPet§8] §0Pilih Kosmetik";
    private static final Map<Integer, String> SLOT_TO_SKIN = new HashMap<>();
    private static final Map<Integer, String> SLOT_TO_TRAIL = new HashMap<>();

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 36, ColorUtil.component(TITLE));

        ItemStack filler = PetMenu.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, filler);
        }

        // Skins row (Slots 10 to 16)
        SLOT_TO_SKIN.clear();
        int skinSlot = 10;
        for (Map.Entry<String, PetSkin> entry : plugin.getConfigManager().getSkins().entrySet()) {
            if (skinSlot > 16) break;
            String key = entry.getKey();
            PetSkin skin = entry.getValue();

            ItemStack head = HeadUtil.createCustomHead(skin.getTexture());
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.displayName(ColorUtil.component(skin.getDisplayName()));
                List<Component> lore = new ArrayList<>();
                boolean isSelected = key.equalsIgnoreCase(data.getSkinKey());
                if (isSelected) {
                    lore.add(ColorUtil.component("&a✔ Sedang Dipakai"));
                } else {
                    lore.add(ColorUtil.component("&eKlik untuk memilih skin ini!"));
                }
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(skinSlot, head);
            SLOT_TO_SKIN.put(skinSlot, key);
            skinSlot++;
        }

        // Trails row (Slots 19 to 25)
        SLOT_TO_TRAIL.clear();
        int trailSlot = 19;
        Map<String, Material> trailIcons = Map.of(
                "FLAME", Material.BLAZE_POWDER,
                "SOUL_FIRE_FLAME", Material.SOUL_TORCH,
                "HEART", Material.POPPY,
                "ENCHANT", Material.ENCHANTING_TABLE,
                "HAPPY_VILLAGER", Material.EMERALD,
                "END_ROD", Material.END_ROD
        );

        for (Map.Entry<String, String> entry : plugin.getConfigManager().getTrails().entrySet()) {
            if (trailSlot > 25) break;
            String key = entry.getKey();
            String name = entry.getValue();
            Material mat = trailIcons.getOrDefault(key, Material.BLAZE_POWDER);

            boolean isSelected = key.equalsIgnoreCase(data.getTrailKey());
            ItemStack item = PetMenu.createItem(mat, name,
                    isSelected ? "&a✔ Sedang Dipakai" : "&eKlik untuk memilih efek trail ini!");
            inv.setItem(trailSlot, item);
            SLOT_TO_TRAIL.put(trailSlot, key);
            trailSlot++;
        }

        // Slot 31: Back Button
        inv.setItem(31, PetMenu.createItem(Material.ARROW, "&c&lKembali ke Menu Utama"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());

        if (slot == 31) {
            PetMenu.open(player, plugin);
            return;
        }

        // Skin clicked
        if (SLOT_TO_SKIN.containsKey(slot)) {
            String skinKey = SLOT_TO_SKIN.get(slot);
            data.setSkinKey(skinKey);

            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null) {
                pet.updateSkin();
            }

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aSkin pet berhasil diganti!");
            open(player, plugin); // refresh
            return;
        }

        // Trail clicked
        if (SLOT_TO_TRAIL.containsKey(slot)) {
            String trailKey = SLOT_TO_TRAIL.get(slot);
            data.setTrailKey(trailKey);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aEfek trail pet berhasil diganti!");
            open(player, plugin); // refresh
        }
    }
}
