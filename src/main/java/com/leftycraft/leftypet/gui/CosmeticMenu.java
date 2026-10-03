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

    // Row 1 (Regular Skins): slots 10..16 (7 slots, centered)
    private static final int[] REGULAR_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    // Row 3 (Celestial Skins): slots 28..34 (7 slots, centered)
    private static final int[] CELESTIAL_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    // Row 4 (Trails): slots 37, 38, 39 and 41, 42, 43 (6 slots around center header at 40)
    private static final int[] TRAIL_SLOTS = {37, 38, 39, 41, 42, 43};

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        CosmeticMenuHolder holder = new CosmeticMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack cyanBorder = createFiller(Material.CYAN_STAINED_GLASS_PANE);
        ItemStack purpleBorder = createFiller(Material.PURPLE_STAINED_GLASS_PANE);
        ItemStack orangeBorder = createFiller(Material.ORANGE_STAINED_GLASS_PANE);

        // Fill all with dark filler first
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, darkFiller);
        }

        // ══════════════════════════════════════════════════════
        // ROW 0: Regular Skin Header & Borders (Slots 0..8)
        // ══════════════════════════════════════════════════════
        inv.setItem(0, cyanBorder);
        inv.setItem(8, cyanBorder);

        ItemStack skinHeader = new ItemStack(Material.NAME_TAG);
        ItemMeta shMeta = skinHeader.getItemMeta();
        if (shMeta != null) {
            shMeta.displayName(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>sᴋɪɴ ᴘᴇᴛ (ʀᴇɢᴜʟᴀʀ)</b></gradient>"));
            List<Component> lore = List.of(
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"),
                    ColorUtil.component("&7Pilih tampilan kepala gratis untuk pet kamu!"),
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>")
            );
            shMeta.lore(lore);
            try { shMeta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
            skinHeader.setItemMeta(shMeta);
        }
        inv.setItem(4, skinHeader);

        // ══════════════════════════════════════════════════════
        // ROW 1: Regular Skins (Slots 10..16), Borders at 9 & 17
        // ══════════════════════════════════════════════════════
        inv.setItem(9, cyanBorder);
        inv.setItem(17, cyanBorder);

        // Separate skins into regular and celestial
        List<Map.Entry<String, PetSkin>> regularSkins = new ArrayList<>();
        List<Map.Entry<String, PetSkin>> celestialSkins = new ArrayList<>();

        for (Map.Entry<String, PetSkin> entry : plugin.getConfigManager().getSkins().entrySet()) {
            if (entry.getValue().hasPermission()) {
                celestialSkins.add(entry);
            } else {
                regularSkins.add(entry);
            }
        }

        SLOT_TO_SKIN.clear();

        for (int i = 0; i < regularSkins.size() && i < REGULAR_SLOTS.length; i++) {
            Map.Entry<String, PetSkin> entry = regularSkins.get(i);
            String key = entry.getKey();
            PetSkin skin = entry.getValue();
            int slot = REGULAR_SLOTS[i];

            ItemStack head = HeadUtil.createCustomHead(skin.getTexture());
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                meta.displayName(ColorUtil.component(skin.getDisplayName()));
                List<Component> lore = new ArrayList<>();
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                boolean isSelected = key.equalsIgnoreCase(data.getSkinKey());
                if (isSelected) {
                    lore.add(ColorUtil.component("&a✔ sᴇᴅᴀɴɢ ᴅɪɢᴜɴᴀᴋᴀɴ"));
                    try { meta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
                } else {
                    lore.add(ColorUtil.component("&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀᴋᴀɪ sᴋɪɴ ɪɴɪ!"));
                }
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
            SLOT_TO_SKIN.put(slot, key);
        }

        // ══════════════════════════════════════════════════════
        // ROW 2: Celestial Header & Divider (Slots 18..26)
        // ══════════════════════════════════════════════════════
        for (int i = 18; i <= 26; i++) {
            inv.setItem(i, purpleBorder);
        }

        boolean hasMemberPP = player.hasPermission("leftypet.memberplusplus") || player.hasPermission("leftypet.memberpp");

        ItemStack memberPPHeader = new ItemStack(Material.NETHER_STAR);
        ItemMeta chMeta = memberPPHeader.getItemMeta();
        if (chMeta != null) {
            chMeta.displayName(ColorUtil.component("<gradient:#ff9a00:#7928ca><b>✦ sᴋɪɴ ᴍᴇᴍʙᴇʀ++ (ᴇxᴄʟᴜsɪᴠᴇ) ✦</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7Koleksi 7 skin kosmetik mistis rank Member++!"));
            if (hasMemberPP) {
                lore.add(ColorUtil.component("&a✔ ʀᴀɴᴋ ᴍᴇᴍʙᴇʀ++ ᴀᴋᴛɪғ! sᴇᴍᴜᴀ sᴋɪɴ ᴛᴇʀʙᴜᴋᴀ."));
            } else {
                lore.add(ColorUtil.component("&c🔒 ᴋʜᴜsᴜs ᴜɴᴛᴜᴋ ʀᴀɴᴋ &d&lMEMBER++&c!"));
                lore.add(ColorUtil.component("&7ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋᴍᴜ ᴅɪ: &e/ranks"));
            }
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            chMeta.lore(lore);
            try { chMeta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
            memberPPHeader.setItemMeta(chMeta);
        }
        inv.setItem(22, memberPPHeader);

        // ══════════════════════════════════════════════════════
        // ROW 3: Celestial Skins (Slots 28..34), Borders at 27 & 35
        // ══════════════════════════════════════════════════════
        inv.setItem(27, purpleBorder);
        inv.setItem(35, purpleBorder);

        for (int i = 0; i < celestialSkins.size() && i < CELESTIAL_SLOTS.length; i++) {
            Map.Entry<String, PetSkin> entry = celestialSkins.get(i);
            String key = entry.getKey();
            PetSkin skin = entry.getValue();
            int slot = CELESTIAL_SLOTS[i];

            boolean hasAccess = hasMemberPP || (!skin.hasPermission() || player.hasPermission(skin.getRequiredPermission()));
            boolean isSelected = key.equalsIgnoreCase(data.getSkinKey());

            // Always display the actual custom head!
            ItemStack head = HeadUtil.createCustomHead(skin.getTexture());
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                List<Component> lore = new ArrayList<>();
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));

                if (!hasAccess) {
                    meta.displayName(ColorUtil.component(skin.getDisplayName() + " <red>[🔒 ʟᴏᴄᴋᴇᴅ]</red>"));
                    lore.add(ColorUtil.component("&c🔒 ᴇxᴄʟᴜsɪᴠᴇ ᴜɴᴛᴜᴋ ʀᴀɴᴋ &d&lMEMBER++"));
                    lore.add(ColorUtil.component("&7Kamu belum memiliki rank ini!"));
                    lore.add(ColorUtil.component("&7Upgrade rank kamu di &e/ranks &7untuk membuka skin ini."));
                    lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                    lore.add(ColorUtil.component("&c✖ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ɪɴғᴏ ʀᴀɴᴋ ᴍᴇᴍʙᴇʀ++"));
                } else {
                    meta.displayName(ColorUtil.component(skin.getDisplayName()));
                    lore.add(ColorUtil.component("<gradient:#ff9a00:#7928ca>✦ ᴍᴇᴍʙᴇʀ++ ᴇxᴄʟᴜsɪᴠᴇ</gradient>"));
                    if (isSelected) {
                        lore.add(ColorUtil.component("&a✔ sᴇᴅᴀɴɢ ᴅɪɢᴜɴᴀᴋᴀɴ"));
                        try { meta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
                    } else {
                        lore.add(ColorUtil.component("&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀᴋᴀɪ sᴋɪɴ ɪɴɪ!"));
                    }
                    lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                }
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
            SLOT_TO_SKIN.put(slot, key);
        }

        // ══════════════════════════════════════════════════════
        // ROW 4: Particle Trails (Slots 37..39, 40 Header, 41..43)
        // ══════════════════════════════════════════════════════
        inv.setItem(36, orangeBorder);
        inv.setItem(44, orangeBorder);

        ItemStack trailHeader = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta thMeta = trailHeader.getItemMeta();
        if (thMeta != null) {
            thMeta.displayName(ColorUtil.component("<gradient:#f7971e:#ffd200><b>ᴘɪʟɪʜ ᴇғᴇᴋ ᴘᴀʀᴛɪᴋᴇʟ</b></gradient>"));
            List<Component> lore = List.of(
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"),
                    ColorUtil.component("&7Pilih efek partikel trail yang mengikuti pet!"),
                    ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>")
            );
            thMeta.lore(lore);
            try { thMeta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
            trailHeader.setItemMeta(thMeta);
        }
        inv.setItem(40, trailHeader);

        SLOT_TO_TRAIL.clear();
        Map<String, Material> trailIcons = Map.of(
                "FLAME", Material.BLAZE_POWDER,
                "SOUL_FIRE_FLAME", Material.SOUL_TORCH,
                "HEART", Material.POPPY,
                "ENCHANT", Material.ENCHANTING_TABLE,
                "HAPPY_VILLAGER", Material.EMERALD,
                "END_ROD", Material.END_ROD
        );

        List<Map.Entry<String, String>> trailEntries = new ArrayList<>(plugin.getConfigManager().getTrails().entrySet());
        for (int i = 0; i < trailEntries.size() && i < TRAIL_SLOTS.length; i++) {
            Map.Entry<String, String> entry = trailEntries.get(i);
            String key = entry.getKey();
            String name = entry.getValue();
            int slot = TRAIL_SLOTS[i];
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
                    try { meta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
                } else {
                    lore.add(ColorUtil.component("&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀᴋᴀɪ ᴇғᴇᴋ ɪɴɪ!"));
                }
                lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            SLOT_TO_TRAIL.put(slot, key);
        }

        // ══════════════════════════════════════════════════════
        // ROW 5: Footer & Back Button (Slot 49), Corners 45 & 53
        // ══════════════════════════════════════════════════════
        inv.setItem(45, cyanBorder);
        inv.setItem(53, cyanBorder);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(ColorUtil.component("<red><b>« ᴋᴇᴍʙᴀʟɪ ᴋᴇ ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ</b></red>"));
            List<Component> lore = List.of(ColorUtil.component("&7Klik untuk kembali ke menu utama."));
            backMeta.lore(lore);
            back.setItemMeta(backMeta);
        }
        inv.setItem(49, back);

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

        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            PetMenu.open(player, plugin);
            return;
        }

        if (slot == 22) {
            boolean hasMemberPP = player.hasPermission("leftypet.memberplusplus") || player.hasPermission("leftypet.memberpp");
            if (!hasMemberPP) {
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#ff9a00:#7928ca><b>✦ sᴋɪɴ ᴍᴇᴍʙᴇʀ++ ᴇxᴄʟᴜsɪᴠᴇ!</b></gradient> " +
                        "<gray>ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋᴍᴜ ᴅɪ <yellow>/ranks</yellow> ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ sᴇᴍᴜᴀ sᴋɪɴ ᴇxᴄʟᴜsɪᴠᴇ ɪɴɪ!</gray>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);
            }
            return;
        }

        if (slot == 4 || slot == 40) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
            return;
        }

        if (SLOT_TO_SKIN.containsKey(slot)) {
            String skinKey = SLOT_TO_SKIN.get(slot);
            PetSkin skin = plugin.getConfigManager().getSkin(skinKey);
            if (skin == null) return;

            // Check Member++ rank permission
            boolean hasSkinAccess = !skin.hasPermission() || player.hasPermission(skin.getRequiredPermission()) ||
                    player.hasPermission("leftypet.memberplusplus") || player.hasPermission("leftypet.memberpp");
            if (!hasSkinAccess) {
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#ff9a00:#7928ca><b>🔒 sᴋɪɴ ɪɴɪ ᴇxᴄʟᴜsɪᴠᴇ ᴜɴᴛᴜᴋ ʀᴀɴᴋ MEMBER++!</b></gradient> " +
                        "<gray>ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋᴍᴜ ᴅɪ <yellow>/ranks</yellow> ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ sᴋɪɴ ɪɴɪ!</gray>"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

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
