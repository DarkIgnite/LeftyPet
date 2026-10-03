package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.holder.PetMenuHolder;
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

    public static final String TITLE = "§8» §b§lʟᴇғᴛʏᴘᴇᴛ §8| §fᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ";

    public static void open(Player player, LeftyPetPlugin plugin) {
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
        ActivePet activePet = plugin.getPetManager().getActivePet(player.getUniqueId());
        boolean isSummoned = (activePet != null && activePet.isValid());

        PetMenuHolder holder = new PetMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, ColorUtil.component(TITLE));
        holder.setInventory(inv);

        // Frame
        ItemStack darkFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack cyanCorner = createFiller(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, darkFiller);
        }
        inv.setItem(0, cyanCorner);
        inv.setItem(8, cyanCorner);
        inv.setItem(18, cyanCorner);
        inv.setItem(26, cyanCorner);

        // Slot 4: Pet Head Profile
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        ItemMeta headMeta = head.getItemMeta();
        if (headMeta != null) {
            headMeta.displayName(ColorUtil.component(data.getName()));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&7ʟᴇᴠᴇʟ: " + ColorUtil.getLevelTag(data.getLevel())));
            lore.add(ColorUtil.component("&7ᴇɴᴇʀɢɪ: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%"));
            lore.add(ColorUtil.component("&7ᴋᴇʟᴀs: " + data.getPetClass().getDisplayName()));
            double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
            lore.add(ColorUtil.component("&7ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", data.getAttackDamage(classMult))));
            lore.add(ColorUtil.component("&7sᴛᴀᴛᴜs: " + (data.isTraining() ? "&eᴛʀᴀɪɴɪɴɢ ᴅɪ ᴀʟᴛᴀʀ" : (isSummoned ? "&aᴅɪᴘᴀɴɢɢɪʟ" : "&7ᴅɪsɪᴍᴘᴀɴ"))));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            lore.add(ColorUtil.component("&eᴋʟɪᴋ ᴋᴀɴᴀɴ ᴘᴇᴛ sᴀᴍʙɪʟ ʙᴀᴡᴀ ᴍᴀᴋᴀɴᴀɴ"));
            lore.add(ColorUtil.component("&7ᴜɴᴛᴜᴋ ᴍᴇɴɢɪsɪ ᴇɴᴇʀɢɪ ᴘᴇᴛ!"));
            lore.add(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            headMeta.lore(lore);
            try {
                headMeta.setEnchantmentGlintOverride(true);
            } catch (Throwable ignored) {}
            head.setItemMeta(headMeta);
        }
        inv.setItem(4, head);

        // Slot 10: Summon / Dismiss
        if (isSummoned) {
            inv.setItem(10, createItem(Material.REDSTONE_BLOCK, true, "&c&lsɪᴍᴘᴀɴ ᴘᴇᴛ",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴʏᴇᴍʙᴜɴʏɪᴋᴀɴ ᴘᴇᴛ",
                    "&7ᴋᴇ ᴀʟᴀᴍ sᴘɪʀɪᴛᴜᴀʟ.",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴʏɪᴍᴘᴀɴ"));
        } else {
            inv.setItem(10, createItem(Material.EMERALD_BLOCK, true, "&a&lᴘᴀɴɢɢɪʟ ᴘᴇᴛ",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴜɴᴄᴜʟᴋᴀɴ ᴘᴇᴛ",
                    "&7ᴅɪ sᴀᴍᴘɪɴɢ ʙᴀʜᴜᴍᴜ!",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&a▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀɴɢɢɪʟ"));
        }

        // Slot 12: Rename Pet
        inv.setItem(12, createItem(Material.NAME_TAG, false, "&e&lɢᴀɴᴛɪ ɴᴀᴍᴀ ᴘᴇᴛ",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴜʙᴀʜ ɴᴀᴍᴀ ᴘᴇᴛ ᴋᴀᴍᴜ sᴇsᴜᴀɪ ᴋᴇɪɴɢɪɴᴀɴ!",
                "&7• ᴍᴀᴋsɪᴍᴀʟ: &e15 ᴋᴀʀᴀᴋᴛᴇʀ",
                "&7• ᴍᴇɴᴅᴜᴋᴜɴɢ: &dᴄᴏʟᴏʀ & ɢʀᴀᴅɪᴇɴᴛ",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ɢᴜɴᴀᴋᴀɴ: &f/pet rename [nama baru]"));

        // Slot 13: Level Roadmap (AuraSkills style)
        inv.setItem(13, createItem(Material.EXPERIENCE_BOTTLE, true, "<gradient:#00c6ff:#0072ff><b>ʟᴇᴠᴇʟ ʀᴏᴀᴅᴍᴀᴘ (1-10)</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ʟɪʜᴀᴛ ᴘᴏʜᴏɴ ᴘʀᴏɢʀᴇsɪ ʟᴇᴠᴇʟ sᴇᴘᴇʀᴛɪ &eᴀᴜʀᴀsᴋɪʟʟs&7!",
                "&7• ʟᴇᴠᴇʟ sᴀᴀᴛ ɪɴɪ: " + ColorUtil.getLevelTag(data.getLevel()),
                "&7• ᴘʀᴏɢʀᴇss: &a" + (data.getLevel() * 10) + "%",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ ʀᴏᴀᴅᴍᴀᴘ!"));

        // Slot 14: Cosmetic Selector
        inv.setItem(14, createItem(Material.PAINTING, false, "<gradient:#ff758c:#ff7eb3><b>ᴋᴜsᴛᴏᴍɪsᴀsɪ ᴋᴏsᴍᴇᴛɪᴋ</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴘɪʟɪʜ sᴋɪɴ ᴋᴇᴘᴀʟᴀ ᴄᴜsᴛᴏᴍ & ᴇғᴇᴋ ᴛʀᴀɪʟ!",
                "&7• sᴋɪɴ: &f" + (skin != null ? skin.getDisplayName() : data.getSkinKey()),
                "&7• ᴛʀᴀɪʟ: &f" + data.getTrailKey(),
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ ᴋᴏsᴍᴇᴛɪᴋ!"));

        // Slot 16: Class Selector
        inv.setItem(16, createItem(Material.NETHER_STAR, true, "<gradient:#f7971e:#ffd200><b>sᴘᴇsɪᴀʟɪsᴀsɪ ᴋᴇʟᴀs</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴋᴇʟᴀs sᴀᴀᴛ ɪɴɪ: " + data.getPetClass().getDisplayName(),
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢɢᴀɴᴛɪ sᴘᴇsɪᴀʟɪsᴀsɪ:",
                "&c• FIGHTER &7(+Damage)",
                "&a• SUPPORT &7(Healing Aura)",
                "&6• LOOTER &7(Auto Pickup & EXP)",
                "&b• TRAVELER &7(+Speed Aura)",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ɢᴀɴᴛɪ ᴋᴇʟᴀs!"));

        // Slot 22: Altar Info
        inv.setItem(22, createItem(Material.LODESTONE, true, "<gradient:#43e97b:#38f9d7><b>ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴛᴇᴍᴘᴀᴛ ᴀғᴋ ᴛʀᴀɪɴɪɴɢ ᴜɴᴛᴜᴋ ᴍᴇɴɪɴɢᴋᴀᴛᴋᴀɴ ʟᴇᴠᴇʟ ᴘᴇᴛ!",
                "&7• ʟᴇᴠᴇʟ 1: &a-10% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                "&7• ʟᴇᴠᴇʟ 2: &a-20% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                "&7• ʟᴇᴠᴇʟ 3: &a-30% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&e▶ ɢᴜɴᴀᴋᴀɴ &f/pet altar &eᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ ᴀʟᴛᴀʀ!"));

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
                    plugin.getPetManager().setSessionDismissed(player.getUniqueId(), true);
                    ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("pet-dismissed"));
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.4f);
                } else {
                    plugin.getPetManager().summonPet(player);
                }
                open(player, plugin);
            }
            case 12 -> { // Rename Info
                player.closeInventory();
                ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#43e97b:#38f9d7>ɢᴜɴᴀᴋᴀɴ ᴘᴇʀɪɴᴛᴀʜ: <yellow>/pet rename [nama baru]</yellow> (ᴍᴀᴋs. 15 ᴋᴀʀᴀᴋᴛᴇʀ)!</gradient>");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            }
            case 13 -> { // Level Roadmap
                player.closeInventory();
                PetRoadmapMenu.open(player, plugin);
            }
            case 14 -> { // Cosmetics
                player.closeInventory();
                CosmeticMenu.open(player, plugin);
            }
            case 16 -> { // Cycle Class
                PetClass[] classes = PetClass.values();
                int nextIndex = (data.getPetClass().ordinal() + 1) % classes.length;
                data.setPetClass(classes[nextIndex]);
                ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (pet != null) {
                    pet.updateNameTag();
                }
                ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") +
                        "<green>ᴋᴇʟᴀs ᴘᴇᴛ ᴅɪᴜʙᴀʜ ᴋᴇ: </green>" + data.getPetClass().getDisplayName());
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.4f);
                open(player, plugin);
            }
            case 22 -> { // Altar Info
                player.closeInventory();
                ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("prefix") +
                        "<yellow>ɢᴜɴᴀᴋᴀɴ ᴘᴇʀɪɴᴛᴀʜ <aqua>/pet altar</aqua> ᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ ʙʟᴏᴋ ᴀʟᴛᴀʀ 3x3!</yellow>");
            }
        }
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, false, name, lore);
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

    private static ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.component(" "));
            item.setItemMeta(meta);
        }
        return item;
    }
}
