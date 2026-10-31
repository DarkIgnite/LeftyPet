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

    public static final String TITLE = "§8» §b§lʟᴇғᴛʏᴘᴇᴛ §8| §fᴘᴇɴɢᴀᴛᴜʀᴀɴ ᴀʟᴛᴀʀ";
    private static final Map<UUID, PetAltar> OPEN_ALTARS = new HashMap<>();

    public static void open(Player player, PetAltar altar, LeftyPetPlugin plugin) {
        OPEN_ALTARS.put(player.getUniqueId(), altar);
        com.leftycraft.leftypet.gui.holder.AltarMenuHolder holder = new com.leftycraft.leftypet.gui.holder.AltarMenuHolder(altar);
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

        ActivePet activePet = plugin.getPetManager().getActivePet(player.getUniqueId());
        PetData data = plugin.getPetManager().getPetData(player.getUniqueId());

        // Slot 11: Mulai Training Pet
        if (activePet != null && activePet.isValid()) {
            int currentLvl = data.getLevel();
            int maxLvl = plugin.getConfigManager().getMaxLevel();

            if (altar.getAltarLevel() == 6 && !player.hasPermission("leftypet.celestial")) {
                inv.setItem(11, PetMenu.createItem(Material.BARRIER, false,
                        "<gradient:#ffe259:#ffa751><b>🔒 ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ 6 [ᴄᴇʟᴇsᴛɪᴀʟ]</b></gradient>",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&c🔒 ʜᴀɴʏᴀ ʙɪsᴀ ᴅɪɢᴜɴᴀᴋᴀɴ ᴏʟᴇʜ ʀᴀɴᴋ &e&lCELESTIAL!",
                        "&7ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋ ᴋᴀᴍᴜ ᴜɴᴛᴜᴋ ᴍᴇɴɢɢᴜɴᴀᴋᴀɴ",
                        "&7ғᴀsɪʟɪᴛᴀs ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ 6 ɪɴɪ.",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&c▶ ʜᴀɴʏᴀ ᴜɴᴛᴜᴋ ʀᴀɴᴋ CELESTIAL!"));
            } else if (data.isUpgradeOnCooldown()) {
                int remSec = data.getUpgradeCooldownRemainingSeconds();
                inv.setItem(11, PetMenu.createItem(Material.CLOCK, false,
                        "<gradient:#ff5f6d:#ffc371><b>⏳ ᴘᴇᴛ sᴇᴅᴀɴɢ ᴄᴏᴏʟᴅᴏᴡɴ ᴜᴘɢʀᴀᴅᴇ</b></gradient>",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&7ᴘᴇᴛ ᴍᴀsɪʜ ᴅᴀʟᴀᴍ ᴍᴀsᴀ ᴘᴇᴍᴜʟɪʜᴀɴ sᴇᴛᴇʟᴀʜ ᴜᴘɢʀᴀᴅᴇ.",
                        "&7• sɪsᴀ ᴡᴀᴋᴛᴜ: &e" + formatSec(remSec),
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&cᴛᴜɴɢɢᴜ ʜɪɴɢɢᴀ ᴄᴏᴏʟᴅᴏᴡɴ sᴇʟᴇsᴀɪ ᴜɴᴛᴜᴋ ᴛʀᴀɪɴɪɴɢ ʟᴀɢɪ."));
            } else if (currentLvl >= maxLvl) {
                inv.setItem(11, PetMenu.createItem(Material.BARRIER, false, "&c&lᴘᴇᴛ sᴜᴅᴀʜ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&7ʟᴇᴠᴇʟ ᴘᴇᴛ ᴋᴀᴍᴜ: &e" + maxLvl,
                        "&7sᴜᴅᴀʜ ᴍᴇɴᴄᴀᴘᴀɪ ʙᴀᴛᴀs ᴛᴇʀᴛɪɴɢɢɪ!",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
            } else {
                int targetLvl = currentLvl + 1;
                int baseSec = plugin.getConfigManager().getUpgradeDuration(currentLvl);
                int finalSec = (int) Math.round(baseSec * altar.getTimeMultiplier());
                double cost = plugin.getConfigManager().getUpgradeCost(currentLvl);
                String costStr = plugin.getEconomyManager().format(cost);
                boolean canAfford = plugin.getEconomyManager().hasEnough(player, cost);
                String costColor = canAfford ? "&a" : "&c";
                String costLore = (cost > 0) ? ("&7• ʙɪᴀʏᴀ: " + costColor + costStr + (canAfford ? "" : " &c(ᴛɪᴅᴀᴋ ᴄᴜᴋᴜᴘ!)")) : "&7• ʙɪᴀʏᴀ: &aɢʀᴀᴛɪs";

                inv.setItem(11, PetMenu.createItem(Material.EMERALD_BLOCK, true, "<gradient:#a8ff78:#78ffd6><b>ᴍᴜʟᴀɪ ᴛʀᴀɪɴɪɴɢ ᴘᴇᴛ</b></gradient>",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&7• ᴛᴀʀɢᴇᴛ: &eʟᴇᴠᴇʟ " + targetLvl,
                        "&7• ᴅᴜʀᴀsɪ sᴛᴀɴᴅᴀʀ: &7" + formatSec(baseSec),
                        "&7• ᴅɪsᴋᴏɴ ᴀʟᴛᴀʀ: &a-" + (int) altar.getTimeReductionPercent() + "% (ʟᴠ." + altar.getAltarLevel() + ")",
                        "&7• ᴡᴀᴋᴛᴜ ᴛʀᴀɪɴɪɴɢ: &e<b>" + formatSec(finalSec) + "</b>",
                        costLore,
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴀsᴜᴋᴋᴀɴ ᴘᴇᴛ ᴋᴇ ᴀʟᴛᴀʀ!"));
            }
        } else {
            inv.setItem(11, PetMenu.createItem(Material.REDSTONE_BLOCK, false, "&c&lᴘᴇᴛ ʙᴇʟᴜᴍ ᴅɪᴘᴀɴɢɢɪʟ",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴘᴀɴɢɢɪʟ ᴘᴇᴛ ᴋᴀᴍᴜ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ!",
                    "&7ɢᴜɴᴀᴋᴀɴ ᴘᴇʀɪɴᴛᴀʜ: &e/pet summon",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
        }

        // Slot 13: Altar Info
        Material altarIcon = switch (altar.getAltarLevel()) {
            case 6 -> Material.BAMBOO_MOSAIC_SLAB;
            case 5 -> Material.PRISMARINE_BRICK_SLAB;
            case 4 -> Material.CHERRY_SLAB;
            case 3 -> Material.PURPUR_SLAB;
            case 2 -> Material.END_STONE_BRICK_SLAB;
            default -> Material.POLISHED_DIORITE_SLAB;
        };

        String eff = (altar.getTimeReductionPercent() > 0) ? ("&a-" + (int) altar.getTimeReductionPercent() + "%") : "&7sᴛᴀɴᴅᴀʀ (0%)";
        inv.setItem(13, PetMenu.createItem(altarIcon, true,
                "<gradient:#ffe259:#ffa751><b>ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ " + altar.getAltarLevel() + " / 6</b></gradient>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7• ᴇғɪsɪᴇɴsɪ ᴡᴀᴋᴛᴜ: " + eff,
                "&7• sᴛʀᴜᴋᴛᴜʀ: &f3x3 Glass Chamber",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴀʟᴛᴀʀ ᴍᴇᴍᴘᴇʀᴄᴇᴘᴀᴛ ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                "&7ᴘᴇᴛ ᴋᴀᴍᴜ sᴇᴄᴀʀᴀ ᴏᴛᴏᴍᴀᴛɪs!"));

        // Slot 15: Upgrade Altar
        int nextAltarLvl = altar.getAltarLevel() + 1;
        if (nextAltarLvl <= 6) {
            boolean isCelestialTier = (nextAltarLvl == 6);
            boolean hasCelestialPerm = player.hasPermission("leftypet.celestial");

            if (isCelestialTier && !hasCelestialPerm) {
                // Show locked button
                inv.setItem(15, PetMenu.createItem(Material.BARRIER, false,
                        "<gradient:#ffe259:#ffa751><b>🔒 ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ [ᴄᴇʟᴇsᴛɪᴀʟ] (ʟᴇᴠᴇʟ 6)</b></gradient>",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&c🔒 ʜᴀɴʏᴀ ᴜɴᴛᴜᴋ ʀᴀɴᴋ &e&lCELESTIAL!",
                        "&7ᴅɪsᴋᴏɴ ᴍᴀᴋsɪᴍᴀʟ: &a-50% ᴡᴀᴋᴛᴜ ᴜᴘɢʀᴀᴅᴇ",
                        "&7ᴅᴀɴ sᴛʀᴜᴋᴛᴜʀ ᴋᴜɴɪɴɢ ᴇxᴋʟᴜsɪᴠ ᴅᴀʀɪ sᴄʜᴇᴍᴀᴛɪᴄ!",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&c▶ ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋᴍᴜ ᴅɪ /ranks ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴜᴋᴀ!"));
            } else {
                String costLore;
                if (nextAltarLvl == 2) costLore = "&7• ʙɪᴀʏᴀ: &e16x End Stone Bricks & 4x Diamonds";
                else if (nextAltarLvl == 3) costLore = "&7• ʙɪᴀʏᴀ: &e16x Purpur Blocks & 8x Diamonds";
                else if (nextAltarLvl == 4) costLore = "&7• ʙɪᴀʏᴀ: &d16x Amethyst Shard & 2x Netherite Ingot";
                else if (nextAltarLvl == 5) costLore = "&7• ʙɪᴀʏᴀ: &b16x Prismarine Bricks & 4x Netherite Ingot";
                else costLore = "&7• ʙɪᴀʏᴀ: &e16x Bamboo Mosaic & 1x Nether Star";

                String titleColor = isCelestialTier ? "<gradient:#ffe259:#ffa751>" :
                        (nextAltarLvl == 5 ? "<gradient:#00c6ff:#0072ff>" :
                        (nextAltarLvl == 4 ? "<gradient:#ff758c:#ff7eb3>" : "<gradient:#00c6ff:#0072ff>"));
                String titleSuffix = isCelestialTier ? " &e&l[ᴄᴇʟᴇsᴛɪᴀʟ]</gradient>" : "</gradient>";
                int discount = switch (nextAltarLvl) {
                    case 6 -> 50;
                    case 5 -> 40;
                    case 4 -> 30;
                    case 3 -> 20;
                    case 2 -> 10;
                    default -> 0;
                };

                Material iconMat = switch (nextAltarLvl) {
                    case 6 -> Material.NETHER_STAR;
                    case 5 -> Material.PRISMARINE_SHARD;
                    case 4 -> Material.AMETHYST_SHARD;
                    default -> Material.ANVIL;
                };

                inv.setItem(15, PetMenu.createItem(
                        iconMat, true,
                        titleColor + "<b>ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ (ʟᴇᴠᴇʟ " + nextAltarLvl + ")" + titleSuffix,
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&7• ᴅɪsᴋᴏɴ ʙᴀʀᴜ: &a-" + discount + "% ᴡᴀᴋᴛᴜ",
                        costLore,
                        (nextAltarLvl >= 4) ? "&f✦ sᴛʀᴜᴋᴛᴜʀ sᴄʜᴇᴍᴀᴛɪᴄ ᴇxᴋʟᴜsɪᴠ!" : "",
                        "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                        "&e▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ!"));
            }
        } else {
            inv.setItem(15, PetMenu.createItem(Material.NETHER_STAR, true,
                    "<gradient:#a8ff78:#78ffd6><b>ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ ᴍᴀᴋsɪᴍᴀʟ!</b></gradient>",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                    "&7ᴅɪsᴋᴏɴ ᴍᴀᴋsɪᴍᴀʟ &a-50% &7sᴜᴅᴀʜ ᴀᴋᴛɪғ.",
                    "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
        }

        // Slot 22: Bongkar Altar
        inv.setItem(22, PetMenu.createItem(Material.LODESTONE, false,
                "<red><b>ʙᴏɴɢᴋᴀʀ ᴀʟᴛᴀʀ</b></red>",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢᴀᴍʙɪʟ ᴋᴇᴍʙᴀʟɪ ʙʟᴏᴋ ᴀʟᴛᴀʀ",
                "&7ᴅᴀɴ ᴍᴇɴɢʜᴀᴘᴜs sᴛʀᴜᴋᴛᴜʀ 3x3 ɪɴɪ.",
                "<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>",
                "&c▶ ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍʙᴏɴɢᴋᴀʀ"));

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
                if (!plugin.getPetManager().isPetSummoned(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>ᴘᴇᴛ ᴋᴀᴍᴜ ʜᴀʀᴜs ᴅɪᴘᴀɴɢɢɪʟ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ sᴇʙᴇʟᴜᴍ ʙɪsᴀ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ ᴅɪ ᴀʟᴛᴀʀ!</gradient>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                    return;
                }
                plugin.getAltarManager().startTraining(player, altar.getLocation());
            }
            case 15 -> {
                int nextLvl = altar.getAltarLevel() + 1;
                if (nextLvl > 6) return;

                // Celestial gate for lv6
                if (nextLvl == 6 && !player.hasPermission("leftypet.celestial")) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ffe259:#ffa751><b>🔒 ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ ʟᴇᴠᴇʟ 6 ʜᴀɴʏᴀ ᴜɴᴛᴜᴋ ʀᴀɴᴋ &e&lCELESTIAL!</b></gradient> " +
                            "<gray>ᴜᴘɢʀᴀᴅᴇ ʀᴀɴᴋᴍᴜ ᴅɪ <yellow>/ranks</yellow>.</gray>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                    return;
                }

                // Check land protection before upgrading
                com.leftycraft.leftypet.hook.ProtectionHookManager.ProtectionResult protRes =
                        plugin.getProtectionHookManager().canPlaceAltar(player, altar.getLocation());
                if (!protRes.allowed()) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>ʟᴀʜᴀɴ ᴛᴇʀʟɪɴᴅᴜɴɢɪ! ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ʙɪsᴀ ᴍᴇɴɢ-ᴜᴘɢʀᴀᴅᴇ ᴀʟᴛᴀʀ ᴅɪ ʟᴀɴᴅ ᴏʀᴀɴɢ ʟᴀɪɴ (" +
                            protRes.pluginName() + ": " + protRes.claimInfo() + ").</gradient>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                    return;
                }

                if (hasUpgradeMaterials(player, nextLvl)) {
                    takeUpgradeMaterials(player, nextLvl);
                    altar.setAltarLevel(nextLvl);
                    plugin.getAltarManager().saveAltars();

                    int discount = switch (nextLvl) {
                        case 6 -> 50;
                        case 5 -> 40;
                        case 4 -> 30;
                        case 3 -> 20;
                        case 2 -> 10;
                        default -> 0;
                    };
                    plugin.getAltarManager().getStructureManager().buildStructureAnimated(altar.getLocation(), nextLvl, () -> {
                        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                                "<gradient:#43e97b:#38f9d7>ᴀʟᴛᴀʀ ʙᴇʀʜᴀsɪʟ ᴅɪ-ᴜᴘɢʀᴀᴅᴇ ᴋᴇ <b>ʟᴇᴠᴇʟ " + nextLvl + "</b>! ᴅɪsᴋᴏɴ ᴡᴀᴋᴛᴜ: <b>-" + discount + "%</b>!</gradient>"));
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
                    });
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
        } else if (targetLvl == 4) {
            return player.getInventory().containsAtLeast(new ItemStack(Material.AMETHYST_SHARD), 16) &&
                    player.getInventory().containsAtLeast(new ItemStack(Material.NETHERITE_INGOT), 2);
        } else if (targetLvl == 5) {
            return player.getInventory().containsAtLeast(new ItemStack(Material.PRISMARINE_BRICKS), 16) &&
                    player.getInventory().containsAtLeast(new ItemStack(Material.NETHERITE_INGOT), 4);
        } else if (targetLvl == 6) {
            return player.getInventory().containsAtLeast(new ItemStack(Material.BAMBOO_MOSAIC), 16) &&
                    player.getInventory().containsAtLeast(new ItemStack(Material.NETHER_STAR), 1);
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
        } else if (targetLvl == 4) {
            player.getInventory().removeItem(new ItemStack(Material.AMETHYST_SHARD, 16));
            player.getInventory().removeItem(new ItemStack(Material.NETHERITE_INGOT, 2));
        } else if (targetLvl == 5) {
            player.getInventory().removeItem(new ItemStack(Material.PRISMARINE_BRICKS, 16));
            player.getInventory().removeItem(new ItemStack(Material.NETHERITE_INGOT, 4));
        } else if (targetLvl == 6) {
            player.getInventory().removeItem(new ItemStack(Material.BAMBOO_MOSAIC, 16));
            player.getInventory().removeItem(new ItemStack(Material.NETHER_STAR, 1));
        }
    }

    private static String formatSec(int seconds) {
        if (seconds <= 0) return "0d";
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) return h + "j " + m + "m " + s + "d";
        if (m > 0) return m + "m " + s + "d";
        return s + "d";
    }
}
