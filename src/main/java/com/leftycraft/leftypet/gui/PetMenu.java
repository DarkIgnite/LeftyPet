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

    public static final String TITLE = "§8[§bʟᴇғᴛʏᴘᴇᴛ§8] §0ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ";

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
            lore.add(ColorUtil.component("&7ʟᴇᴠᴇʟ: &e" + data.getLevel() + " &7/ &e" + plugin.getConfigManager().getMaxLevel()));
            lore.add(ColorUtil.component("&7ᴇɴᴇʀɢɪ: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%"));
            lore.add(ColorUtil.component("&7ᴋᴇʟᴀs: " + data.getPetClass().getDisplayName()));
            double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
            lore.add(ColorUtil.component("&7ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ: &c" + String.format("%.1f", data.getAttackDamage(classMult))));
            lore.add(ColorUtil.component("&7sᴛᴀᴛᴜs: " + (data.isTraining() ? "&eᴛʀᴀɪɴɪɴɢ ᴅɪ ᴀʟᴛᴀʀ" : (isSummoned ? "&aᴅɪᴘᴀɴɢɢɪʟ" : "&7ᴅɪsɪᴍᴘᴀɴ"))));
            lore.add(ColorUtil.component(""));
            lore.add(ColorUtil.component("&eᴋʟɪᴋ ᴋᴀɴᴀɴ ᴘᴇᴛ sᴀᴍʙɪʟ ʙᴀᴡᴀ ᴍᴀᴋᴀɴᴀɴ"));
            lore.add(ColorUtil.component("&7ᴜɴᴛᴜᴋ ᴍᴇɴɢɪsɪ ᴇɴᴇʀɢɪ ᴘᴇᴛ!"));
            headMeta.lore(lore);
            head.setItemMeta(headMeta);
        }
        inv.setItem(4, head);

        // Slot 10: Summon / Dismiss
        if (isSummoned) {
            inv.setItem(10, createItem(Material.REDSTONE_BLOCK, "&c&lsɪᴍᴘᴀɴ ᴘᴇᴛ",
                    "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴʏᴇᴍʙᴜɴʏɪᴋᴀɴ ᴘᴇᴛ", "&7ᴋᴇ ᴀʟᴀᴍ sᴘɪʀɪᴛᴜᴀʟ."));
        } else {
            inv.setItem(10, createItem(Material.EMERALD_BLOCK, "&a&lᴘᴀɴɢɢɪʟ ᴘᴇᴛ",
                    "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇᴍᴜɴᴄᴜʟᴋᴀɴ ᴘᴇᴛ", "&7ᴅɪ sᴀᴍᴘɪɴɢ ʙᴀʜᴜᴍᴜ!"));
        }

        // Slot 12: Mount (Saddle)
        boolean canMount = plugin.getMountManager().canMount(data);
        if (canMount) {
            boolean isRiding = isSummoned && activePet.isMounting();
            inv.setItem(12, createItem(Material.SADDLE, isRiding ? "&e&lᴛᴜʀᴜɴ ᴅᴀʀɪ ᴘᴇᴛ" : "&a&lɴᴀɪᴋɪ ᴘᴇᴛ (ᴍᴏᴜɴᴛ)",
                    "&7ɢᴜɴᴀᴋᴀɴ &eᴡ-ᴀ-s-ᴅ &7ᴜɴᴛᴜᴋ ʙᴇʀᴊᴀʟᴀɴ!",
                    "&7ᴛᴇᴋᴀɴ &esʜɪғᴛ &7ᴜɴᴛᴜᴋ ᴛᴜʀᴜɴ."));
        } else {
            inv.setItem(12, createItem(Material.BARRIER, "&c&lᴍᴏᴜɴᴛ ᴛᴇʀᴋᴜɴᴄɪ",
                    "&7ʙɪsᴀ ᴅɪɴᴀɪᴋɪ ᴍᴜʟᴀɪ &eʟᴇᴠᴇʟ " + plugin.getConfigManager().getMountUnlockLevel() + "&7!",
                    "&7ʟᴇᴠᴇʟ ᴘᴇᴛ ᴋᴀᴍᴜ: &e" + data.getLevel()));
        }

        // Slot 14: Cosmetic Selector
        inv.setItem(14, createItem(Material.PAINTING, "&d&lᴋᴜsᴛᴏᴍɪsᴀsɪ ᴋᴏsᴍᴇᴛɪᴋ",
                "&7ᴘɪʟɪʜ sᴋɪɴ ᴋᴇᴘᴀʟᴀ ᴄᴜsᴛᴏᴍ", "&7ᴅᴀɴ ᴇғᴇᴋ ᴘᴀʀᴛɪᴋᴇʟ ᴛʀᴀɪʟ!"));

        // Slot 16: Class Selector
        inv.setItem(16, createItem(Material.NETHER_STAR, "&6&lɢᴀɴᴛɪ ᴋᴇʟᴀs (" + data.getPetClass().name() + ")",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢɢᴀɴᴛɪ sᴘᴇsɪᴀʟɪsᴀsɪ:",
                "&c• Fighter &7(+Damage)",
                "&a• Support &7(Healing Aura)",
                "&6• Looter &7(Auto Pickup & EXP)",
                "&b• Traveler &7(+Mount Speed)"));

        // Slot 19: Rename Pet (Revisi 9)
        inv.setItem(19, createItem(Material.NAME_TAG, "&e&lɢᴀɴᴛɪ ɴᴀᴍᴀ ᴘᴇᴛ",
                "&7ᴋʟɪᴋ ᴜɴᴛᴜᴋ ᴍᴇɴɢᴜʙᴀʜ ɴᴀᴍᴀ ᴘᴇᴛ ᴋᴀᴍᴜ!",
                "&eᴘᴇʀɪɴᴛᴀʜ: &f/pet rename <nama baru>"));

        // Slot 22: Altar Info
        inv.setItem(22, createItem(Material.LODESTONE, "&6&lᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)",
                "&7ɢᴜɴᴀᴋᴀɴ &e/pet altar &7ᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ",
                "&7ʙʟᴏᴋ ᴀʟᴛᴀʀ ᴛᴇᴍᴘᴀᴛ ᴀғᴋ ᴛʀᴀɪɴɪɴɢ ᴘᴇᴛ!"));

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
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-dismissed")));
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.4f);
                } else {
                    plugin.getPetManager().summonPet(player);
                }
                open(player, plugin);
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
                ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (pet != null) {
                    pet.updateNameTag();
                }
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<green>ᴋᴇʟᴀs ᴘᴇᴛ ᴅɪᴜʙᴀʜ ᴋᴇ: </green>" + data.getPetClass().getDisplayName()));
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.4f);
                open(player, plugin);
            }
            case 19 -> { // Rename
                player.closeInventory();
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#43e97b:#38f9d7>ɢᴜɴᴀᴋᴀɴ ᴘᴇʀɪɴᴛᴀʜ: <yellow>/pet rename &lt;nama baru&gt;</yellow> ᴜɴᴛᴜᴋ ᴍᴇɴɢᴜʙᴀʜ ɴᴀᴍᴀ ᴘᴇᴛ ᴋᴀᴍᴜ!</gradient>"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            }
            case 22 -> { // Altar Info
                player.closeInventory();
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<yellow>ɢᴜɴᴀᴋᴀɴ ᴘᴇʀɪɴᴛᴀʜ <aqua>/pet altar</aqua> ᴜɴᴛᴜᴋ ᴍᴇɴᴅᴀᴘᴀᴛᴋᴀɴ ʙʟᴏᴋ ᴀʟᴛᴀʀ 3x3!</yellow>"));
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
