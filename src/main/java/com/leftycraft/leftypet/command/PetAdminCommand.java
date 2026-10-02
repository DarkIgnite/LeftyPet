package com.leftycraft.leftypet.command;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetData;
import org.bukkit.Bukkit;
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
    private final List<String> subCommands = Arrays.asList("reload", "setlevel", "setenergy", "givealtar");

    public PetAdminCommand(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("leftypet.admin")) {
            sender.sendMessage("§cKamu tidak memiliki izin untuk perintah ini!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§b§lLeftyPet Admin Commands:");
            sender.sendMessage("§7/leftypet reload - Reload konfigurasi plugin");
            sender.sendMessage("§7/leftypet setlevel <player> <level> - Atur level pet player");
            sender.sendMessage("§7/leftypet setenergy <player> <amount> - Atur energi pet player");
            sender.sendMessage("§7/leftypet givealtar <player> - Berikan Altar ke player");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aKonfigurasi berhasil dimuat ulang!");
            }
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cGunakan: /leftypet setlevel <player> <level>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer tidak ditemukan atau sedang offline!");
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
                    sender.sendMessage("§aBerhasil mengatur level pet milik §e" + target.getName() + " §ake Level §e" + lvl + "§a!");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLevel harus berupa angka bulat!");
                }
            }
            case "setenergy" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cGunakan: /leftypet setenergy <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer tidak ditemukan!");
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
                    sender.sendMessage("§aBerhasil mengatur energi pet milik §e" + target.getName() + " §ake §e" + energy + "%§a!");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cEnergi harus berupa angka!");
                }
            }
            case "givealtar" -> {
                Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage("§cTentukan target player!");
                    return true;
                }
                target.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                sender.sendMessage("§aBerhasil memberikan Altar kepada §e" + target.getName() + "§a!");
            }
            default -> {
                sender.sendMessage("§cSubcommand tidak diketahui! Ketik /leftypet untuk melihat bantuan.");
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
