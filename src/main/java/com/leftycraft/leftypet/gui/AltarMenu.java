package com.leftycraft.leftypet.gui;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AltarMenu {

    public static final String TITLE = "§8[§bʟᴇғᴛʏᴘᴇᴛ§8] §0ᴘᴇɴɢᴀᴛᴜʀᴀɴ ᴀʟᴛᴀʀ";
    private static final Map<UUID, PetAltar> OPEN_ALTARS = new HashMap<>();

    public static void open(Player player, PetAltar altar, LeftyPetPlugin plugin) {
        OPEN_ALTARS.put(player.getUniqueId(), altar);
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.component(TITLE));

        ItemStack filler = PetMenu.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        ActivePet activePet = plugin.getPetManager().getActivePet(player.getUniqueId());
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());

        // Slot 11: Mulai Training Pet
        if (activePet != null && activePet.isValid()) {
            int currentLvl = data.getLevel();
            int maxLvl = plugin.getConfigManager().getMaxLevel();

            if (currentLvl >= maxLvl) {
                inv.setItem(11, PetMenu.createItem(Material.BARRIER, "&c&lᴘᴇᴛ sᴜᴅᴀʜ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ",
                        "&7ʟᴇᴠᴇʟ ᴘᴇᴛ ᴋᴀᴍᴜ: &e" + maxLvl));
            } else {
                int targetLvl = currentLvl + 1;
                int baseSec = plugin.getConfigManager().getUpgradeDuration(currentLvl);
                int finalSec = (int) Math.round(baseSec * altar.getTimeMultiplier());

                inv.setItem(11, PetMenu.createItem(Material.EMERALD_BLOCK, "&a&lᴍᴜʟᴀɪ ᴛʀᴀɪɴɪɴɢ ᴘᴇᴛ",
                        "&7ᴛᴀʀɢᴇᴛ: &eʟᴇᴠᴇʟ " + targetLvl,
                        "&7ᴅᴜʀᴀsɪ sᴛᴀɴᴅᴀʀ: &e" + formatSec(baseSec),
                        "&bᴅɪsᴋᴏɴ ᴀʟᴛᴀʀ (ʟᴠ." + altar.getAltarLevel() + "): &a-" + (int) altar.getTimeReductionPercent() + "%",
                        "&6ᴡᴀᴋᴛᴜ ᴛʀᴀɪɴɪɴɢ: &e" + formatSec(finalSec),
                        "",
                        "&eᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀsᴜᴋᴋᴀɴ ᴘᴇᴛ ᴋᴇ ᴀʟᴛᴀʀ!"));
            }
        } else {
            inv.setItem(11, PetMenu.createItem(Material.REDSTONE_BLOCK, "&c&lᴘᴇᴛ ʙᴇʟᴜᴍ ᴅɪᴘᴀɴɢɢɪʟ",
                    "&7ᴘᴀɴɢɢɪʟ ᴘᴇᴛ ᴋᴀᴍᴜ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ",
                    "&7ᴅᴇɴɢᴀɴ ᴘᴇʀɪɴᴛᴀʜ &e/pet summon&7!"));
        }

        // Slot 13: Altar Info
        Material altarIcon = switch (altar.getAltarLevel()) {
            case 2 -> Material.END_STONE_BRICK_SLAB;
            case 3 -> Material.PURPUR_SLAB;
            default -> Material.POLISHED_DIORITE_SLAB;
        };

        inv.setItem(13, PetMenu.createItem(altarIcon,
                "&6&lᴀʟᴛᴀʀ ʟᴇᴠᴇʟ &e" + altar.getAltarLevel() + " &7/ &e3",
                "&7ᴇғɪsɪᴇɴsɪ ᴡᴀᴋᴛᴜ: &a-" + (int) altar.getTimeReductionPercent() + "%",
                "&7sᴛʀᴜᴋᴛᴜʀ: &f3x3 Glass Chamber",
                "",
                "&7ᴀʟᴛᴀʀ ᴍᴇᴍᴘᴇʀᴄᴇᴘᴀᴛ ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                "&7ᴘᴇᴛ ᴋᴀᴍᴜ sᴇᴄᴀʀᴀ ᴏᴛᴏᴍᴀᴛɪs!"));

        // Slot 15: Upgrade Altar
        int nextAltarLvl = altar.getAltarLevel() + 1;
        if (nextAltarLvl <= 3) {
            String costLore = (nextAltarLvl == 2)
                    ? "&7ʙɪᴀʏᴀ: &e16x End Stone Bricks & 4x Diamonds"
                    : "&7ʙɪᴀʏᴀ: &e16x Purpur Blocks & 8x Diamonds";

            inv.setItem(15, PetMenu.createItem(Material.ANVIL,
                    "&b&lᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ ᴋᴇ ʟᴇᴠᴇʟ &e" + nextAltarLvl,
                    "&7ᴍᴇɴɪɴɢᴋᴀᴛᴋᴀɴ ᴅɪsᴋᴏɴ ᴡᴀᴋᴛᴜ ᴍᴇɴᴊᴀᴅɪ: &a-" + (nextAltarLvl * 10) + "%",
                    costLore,
                    "",
                    "&eᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ!"));
        } else {
            inv.setItem(15, PetMenu.createItem(Material.NETHER_STAR,
                    "&a&lᴀʟᴛᴀʀ sᴜᴅᴀʜ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ!",
                    "&7ᴅɪsᴋᴏɴ ᴍᴀᴋsɪᴍᴀʟ &a-30% &7ᴛᴇʟᴀʜ ᴀᴋᴛɪғ."));
        }

        // Slot 22: Bongkar Altar
        inv.setItem(22, PetMenu.createItem(Material.LODESTONE,
                "&c&lʙᴏɴɢᴋᴀʀ ᴀʟᴛᴀʀ",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢᴀᴍʙɪʟ ᴋᴇᴍʙᴀʟɪ ʙʟᴏᴋ ᴀʟᴛᴀʀ",
                "&7ᴅᴀɴ ᴍᴇɴɢʜᴀᴘᴜs sᴛʀᴜᴋᴛᴜʀ 3x3 ɪɴɪ."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.2f);
    }

    public static void handleClick(InventoryClickEvent event, LeftyPetPlugin plugin) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        PetAltar altar = OPEN_ALTARS.get(player.getUniqueId());
        if (altar == null) return;

        int slot = event.getRawSlot();
        switch (slot) {
            case 11 -> {
                player.closeInventory();
                plugin.getAltarManager().startTraining(player, altar.getLocation());
            }
            case 15 -> {
                int nextLvl = altar.getAltarLevel() + 1;
                if (nextLvl > 3) return;

                if (hasUpgradeMaterials(player, nextLvl)) {
                    takeUpgradeMaterials(player, nextLvl);
                    altar.setAltarLevel(nextLvl);
                    plugin.getAltarManager().getStructureManager().buildStructure(altar.getLocation(), nextLvl);
                    plugin.getAltarManager().saveAltars();

                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#43e97b:#38f9d7>ᴀʟᴛᴀʀ ʙᴇʀʜᴀsɪʟ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ ᴋᴇ <b>ʟᴇᴠᴇʟ " + nextLvl + "</b>! ᴅɪsᴋᴏɴ ᴡᴀᴋᴛᴜ: <b>-" + (nextLvl * 10) + "%</b>!</gradient>"));
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
                    open(player, altar, plugin);
                } else {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>ᴍᴀᴛᴇʀɪᴀʟ ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ᴄᴜᴋᴜᴘ ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ!</gradient>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                }
            }
            case 22 -> {
                player.closeInventory();
                plugin.getAltarManager().dismantleAltar(player, altar);
            }
        }
    }

    private static boolean hasUpgradeMaterials(Player player, int targetLvl) {
        if (targetLvl == 2) {
            return player.getInventory().containsAtLeast(new ItemStack(Material.END_STONE_BRICKS), 16) &&
                    player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 4);
        } else if (targetLvl == 3) {
            return player.getInventory().containsAtLeast(new ItemStack(Material.PURPUR_BLOCK), 16) &&
                    player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 8);
        }
        return false;
    }

    private static void takeUpgradeMaterials(Player player, int targetLvl) {
        if (targetLvl == 2) {
            player.getInventory().removeItem(new ItemStack(Material.END_STONE_BRICKS, 16));
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 4));
        } else if (targetLvl == 3) {
            player.getInventory().removeItem(new ItemStack(Material.PURPUR_BLOCK, 16));
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 8));
        }
    }

    private static String formatSec(int seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;
        if (min > 0) return min + "m " + sec + "s";
        return sec + "s";
    }
}
