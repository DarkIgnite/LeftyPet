package com.leftycraft.leftypet.command;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetAltar;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PetAdminCommand implements CommandExecutor, TabCompleter {

    private final LeftyPetPlugin plugin;
    private final List<String> subCommands = Arrays.asList("reload", "setlevel", "setenergy", "givealtar", "removealtar", "tpaltar");

    public PetAdminCommand(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("leftypet.admin")) {
            sender.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371>ᴋᴀᴍᴜ ᴛɪᴅᴀᴋ ᴍᴇᴍɪʟɪᴋɪ ɪᴢɪɴ ᴜɴᴛᴜᴋ ᴘᴇʀɪɴᴛᴀʜ ɪɴɪ!</gradient>"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>---------------- [ʟᴇғᴛʏᴘᴇᴛ ᴀᴅᴍɪɴ] ----------------</b></gradient>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet reload</aqua> <gray>- ʀᴇʟᴏᴀᴅ ᴋᴏɴғɪɢᴜʀᴀsɪ ᴘʟᴜɢɪɴ</gray>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet setlevel [player] [level]</aqua> <gray>- ᴀᴛᴜʀ ʟᴇᴠᴇʟ ᴘᴇᴛ ᴘʟᴀʏᴇʀ</gray>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet setenergy [player] [amount]</aqua> <gray>- ᴀᴛᴜʀ ᴇɴᴇʀɢɪ ᴘᴇᴛ ᴘʟᴀʏᴇʀ</gray>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet givealtar [player]</aqua> <gray>- ʙᴇʀɪᴋᴀɴ ᴀʟᴛᴀʀ ᴋᴇ ᴘʟᴀʏᴇʀ</gray>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet removealtar [player]</aqua> <gray>- ʜᴀᴘᴜs ᴀʟᴛᴀʀ ᴍɪʟɪᴋ ᴘʟᴀʏᴇʀ</gray>"));
            sender.sendMessage(ColorUtil.component("<aqua>/leftypet tpaltar [player]</aqua> <gray>- ᴛᴇʟᴇᴘᴏʀᴛ ᴋᴇ ᴀʟᴛᴀʀ ᴍɪʟɪᴋ ᴘʟᴀʏᴇʀ</gray>"));
            sender.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>--------------------------------------------------</b></gradient>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴋᴏɴғɪɢᴜʀᴀsɪ ʙᴇʀʜᴀsɪʟ ᴅɪᴍᴜᴀᴛ ᴜʟᴀɴɢ!</gradient>"));
            }
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.component("<red>ɢᴜɴᴀᴋᴀɴ: /leftypet setlevel [player] [level]</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("<red>ᴘʟᴀʏᴇʀ ᴛɪᴅᴀᴋ ᴅɪᴛᴇᴍᴜᴋᴀɴ ᴀᴛᴀᴜ sᴇᴅᴀɴɢ ᴏғғʟɪɴᴇ!</red>"));
                    return true;
                }
                try {
                    int lvl = Integer.parseInt(args[2]);
                    PetData data = plugin.getPetManager().getPetData(target.getUniqueId());
                    data.setLevel(lvl);

                    ActivePet pet = plugin.getPetManager().getActivePet(target.getUniqueId());
                    if (pet != null) {
                        pet.updateNameTag();
                    }
                    sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴍᴇɴɢᴀᴛᴜʀ ʟᴇᴠᴇʟ ᴘᴇᴛ ᴍɪʟɪᴋ <yellow>" + target.getName() + "</yellow> ᴋᴇ: </gradient>" + ColorUtil.getLevelTag(lvl) + "!"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("<red>ʟᴇᴠᴇʟ ʜᴀʀᴜs ʙᴇʀᴜᴘᴀ ᴀɴɢᴋᴀ ʙᴜʟᴀᴛ!</red>"));
                }
            }
            case "setenergy" -> {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.component("<red>ɢᴜɴᴀᴋᴀɴ: /leftypet setenergy [player] [amount]</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("<red>ᴘʟᴀʏᴇʀ ᴛɪᴅᴀᴋ ᴅɪᴛᴇᴍᴜᴋᴀɴ!</red>"));
                    return true;
                }
                try {
                    double energy = Double.parseDouble(args[2]);
                    PetData data = plugin.getPetManager().getPetData(target.getUniqueId());
                    data.setEnergy(energy);

                    ActivePet pet = plugin.getPetManager().getActivePet(target.getUniqueId());
                    if (pet != null) {
                        pet.updateNameTag();
                    }
                    sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴍᴇɴɢᴀᴛᴜʀ ᴇɴᴇʀɢɪ ᴘᴇᴛ ᴍɪʟɪᴋ <yellow>" + target.getName() + "</yellow> ᴋᴇ <yellow>" + energy + "%</yellow>!</gradient>"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ColorUtil.component("<red>ᴇɴᴇʀɢɪ ʜᴀʀᴜs ʙᴇʀᴜᴘᴀ ᴀɴɢᴋᴀ!</red>"));
                }
            }
            case "givealtar" -> {
                Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(ColorUtil.component("<red>ᴛᴇɴᴛᴜᴋᴀɴ ᴛᴀʀɢᴇᴛ ᴘʟᴀʏᴇʀ!</red>"));
                    return true;
                }
                target.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴍᴇᴍʙᴇʀɪᴋᴀɴ ᴀʟᴛᴀʀ ᴋᴇᴘᴀᴅᴀ <yellow>" + target.getName() + "</yellow>!</gradient>"));
            }
            case "removealtar" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.component("<red>ɢᴜɴᴀᴋᴀɴ: /leftypet removealtar [player]</red>"));
                    return true;
                }
                org.bukkit.OfflinePlayer target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    target = Bukkit.getOfflinePlayer(args[1]);
                }
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    sender.sendMessage(ColorUtil.component("<red>ᴘʟᴀʏᴇʀ ᴛɪᴅᴀᴋ ᴅɪᴛᴇᴍᴜᴋᴀɴ!</red>"));
                    return true;
                }
                boolean success = plugin.getAltarManager().dismantleAltarByAdmin(sender, target);
                if (!success) {
                    sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>ᴘʟᴀʏᴇʀ <yellow>" + (target.getName() != null ? target.getName() : args[1]) + "</yellow> ᴛɪᴅᴀᴋ ᴍᴇᴍɪʟɪᴋɪ ᴀʟᴛᴀʀ ᴀᴋᴛɪғ!</gradient>"));
                }
            }
            case "tpaltar" -> {
                if (!(sender instanceof Player adminPlayer)) {
                    sender.sendMessage(ColorUtil.component("<red>ᴘᴇʀɪɴᴛᴀʜ ɪɴɪ ʜᴀɴʏᴀ ʙɪsᴀ ᴅɪᴊᴀʟᴀɴᴋᴀɴ ᴏʟᴇʜ ᴘʟᴀʏᴇʀ!</red>"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.component("<red>ɢᴜɴᴀᴋᴀɴ: /leftypet tpaltar [player]</red>"));
                    return true;
                }
                org.bukkit.OfflinePlayer target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    target = Bukkit.getOfflinePlayer(args[1]);
                }
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    sender.sendMessage(ColorUtil.component("<red>ᴘʟᴀʏᴇʀ ᴛɪᴅᴀᴋ ᴅɪᴛᴇᴍᴜᴋᴀɴ!</red>"));
                    return true;
                }
                PetAltar altar = null;
                for (PetAltar a : plugin.getAltarManager().getAltars().values()) {
                    if (a.getOwnerUuid().equals(target.getUniqueId())) {
                        altar = a;
                        break;
                    }
                }
                if (altar == null || altar.getLocation().getWorld() == null) {
                    sender.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<gradient:#ff5f6d:#ffc371>ᴘʟᴀʏᴇʀ <yellow>" + (target.getName() != null ? target.getName() : args[1]) + "</yellow> ᴛɪᴅᴀᴋ ᴍᴇᴍɪʟɪᴋɪ ᴀʟᴛᴀʀ ᴀᴋᴛɪғ!</gradient>"));
                    return true;
                }
                Location dest = altar.getLocation().clone().add(0.5, 1.0, 0.5);
                adminPlayer.teleport(dest);
                adminPlayer.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#43e97b:#38f9d7>ʙᴇʀʜᴀsɪʟ ᴛᴇʟᴇᴘᴏʀᴛ ᴋᴇ ᴀʟᴛᴀʀ ᴍɪʟɪᴋ <yellow>" + (target.getName() != null ? target.getName() : args[1]) + "</yellow>!</gradient>"));
                adminPlayer.playSound(adminPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.0f);
            }
            default -> {
                sender.sendMessage(ColorUtil.component("<red>sᴜʙᴄᴏᴍᴍᴀɴᴅ ᴛɪᴅᴀᴋ ᴅɪᴋᴇᴛᴀʜᴜɪ!</red>"));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("leftypet.admin")) return new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream().filter(s -> s.startsWith(input)).toList();
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase().startsWith(input)).toList();
        }
        return new ArrayList<>();
    }
}
