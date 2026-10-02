package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.holder.CosmeticMenuHolder;
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

    public static final String TITLE = "§8» §b§lʟᴇғᴛʏᴘᴇᴛ §8| §fᴘɪʟɪʜ ᴋᴏsᴍᴇᴛɪᴋ";
    private static final Map<Integer, String> SLOT_TO_SKIN = new HashMap<>();
    private static final Map<Integer, String> SLOT_TO_TRAIL = new HashMap<>();

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        CosmeticMenuHolder holder = new CosmeticMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 45, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack grayFiller = createFiller(Material.GRAY_STAINED_GLASS_PANE);
        ItemStack cyanCorner = createFiller(Material.CYAN_STAINED_GLASS_PANE);

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, darkFiller);
        }

        // Cyan corners
        inv.setItem(0, cyanCorner);
        inv.setItem(8, cyanCorner);
        inv.setItem(36, cyanCorner);
        inv.setItem(44, cyanCorner);

        // Header for Skins (Slot 4)
        ItemStack skinHeader = new ItemStack(Material.NAME_TAG);
        ItemMeta shMeta = skinHeader.getItemMeta();
        if (shMeta != null) {
            shMeta.displayName(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>ᴘɪʟɪʜ sᴋɪɴ ᴘᴇᴛ</b></gradient>"));
            List<Component> lore = List.of(
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"),
                    ColorUtil.component("&7ᴘɪʟɪʜ sᴋɪɴ ᴋᴇᴘᴀʟᴀ ᴄᴜsᴛᴏᴍ ᴜɴᴛᴜᴋ ᴘᴇᴛ ᴋᴀᴍᴜ!"),
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>")
            );
            shMeta.lore(lore);
            try {
                shMeta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            skinHeader.setItemMeta(shMeta);
        }
        inv.setItem(4, skinHeader);

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
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                boolean isSelected = key.equalsIgnoreCase(data.getSkinKey());
                if (isSelected) {
                    lore.add(ColorUtil.component("&a✔ sᴇᴅᴀɴɢ ᴅɪɢᴜɴᴀᴋᴀɴ"));
                    try {
                        meta.setEnchantmentGlintOverride(true);
                    } catch (Throwable ignored) {}
                } else {
                    lore.add(ColorUtil.component("&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀᴋᴀɪ sᴋɪɴ ɪɴɪ!"));
                }
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(skinSlot, head);
            SLOT_TO_SKIN.put(skinSlot, key);
            skinSlot++;
        }

        // Row 2: Divider bar & Trail Header (Slots 18 to 26)
        for (int i = 18; i <= 26; i++) {
            inv.setItem(i, grayFiller);
        }
        inv.setItem(18, cyanCorner);
        inv.setItem(26, cyanCorner);

        ItemStack trailHeader = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta thMeta = trailHeader.getItemMeta();
        if (thMeta != null) {
            thMeta.displayName(ColorUtil.component("<gradient:#f7971e:#ffd200><b>ᴘɪʟɪʜ ᴇғᴇᴋ ᴘᴀʀᴛɪᴋᴇʟ</b></gradient>"));
            List<Component> lore = List.of(
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"),
                    ColorUtil.component("&7ᴘɪʟɪʜ ᴇғᴇᴋ ᴘᴀʀᴛɪᴋᴇʟ ᴛʀᴀɪʟ ʏᴀɴɢ ᴍᴇɴɢɪᴋᴜᴛɪ ᴘᴇᴛ!"),
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>")
            );
            thMeta.lore(lore);
            try {
                thMeta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            trailHeader.setItemMeta(thMeta);
        }
        inv.setItem(22, trailHeader);

        // Trails row (Slots 28 to 33)
        SLOT_TO_TRAIL.clear();
        int trailSlot = 28;
        Map<String, Material> trailIcons = Map.of(
                "FLAME", Material.BLAZE_POWDER,
                "SOUL_FIRE_FLAME", Material.SOUL_TORCH,
                "HEART", Material.POPPY,
                "ENCHANT", Material.ENCHANTING_TABLE,
                "HAPPY_VILLAGER", Material.EMERALD,
                "END_ROD", Material.END_ROD
        );

        for (Map.Entry<String, String> entry : plugin.getConfigManager().getTrails().entrySet()) {
            if (trailSlot > 34) break;
            String key = entry.getKey();
            String name = entry.getValue();
            Material mat = trailIcons.getOrDefault(key, Material.BLAZE_POWDER);

            boolean isSelected = key.equalsIgnoreCase(data.getTrailKey());
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(ColorUtil.component(name));
                List<Component> lore = new ArrayList<>();
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                if (isSelected) {
                    lore.add(ColorUtil.component("&a✔ sᴇᴅᴀɴɢ ᴅɪɢᴜɴᴀᴋᴀɴ"));
                    try {
                        meta.setEnchantmentGlintOverride(true);
                    } catch (Throwable ignored) {}
                } else {
                    lore.add(ColorUtil.component("&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀᴋᴀɪ ᴇғᴇᴋ ɪɴɪ!"));
                }
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(trailSlot, item);
            SLOT_TO_TRAIL.put(trailSlot, key);
            trailSlot++;
        }

        // Slot 40: Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(ColorUtil.component("<red><b>« ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ</b></red>"));
            List<Component> lore = List.of(ColorUtil.component("&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴍᴇɴᴜ ᴜᴛᴀᴍᴀ."));
            backMeta.lore(lore);
            back.setItemMeta(backMeta);
        }
        inv.setItem(40, back);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6f, 1.2f);
    }

    private static ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());

        if (slot == 40) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            PetMenu.open(player, plugin);
            return;
        }

        if (SLOT_TO_SKIN.containsKey(slot)) {
            String skinKey = SLOT_TO_SKIN.get(slot);
            data.setSkinKey(skinKey);

            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null) {
                pet.updateSkin();
            }

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>sᴋɪɴ ᴘᴇᴛ ʙᴇʀʜᴀsɪʟ ᴅɪɢᴀɴᴛɪ!</gradient>"));
            open(player, plugin);
            return;
        }

        if (SLOT_TO_TRAIL.containsKey(slot)) {
            String trailKey = SLOT_TO_TRAIL.get(slot);
            data.setTrailKey(trailKey);

            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴇғᴇᴋ ᴛʀᴀɪʟ ᴘᴇᴛ ʙᴇʀʜᴀsɪʟ ᴅɪɢᴀɴᴛɪ!</gradient>"));
            open(player, plugin);
        }
    }
}
