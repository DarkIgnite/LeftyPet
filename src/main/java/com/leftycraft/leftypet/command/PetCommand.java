package com.leftycraft.leftypet.command;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.gui.PetMenu;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
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

public class PetCommand implements CommandExecutor, TabCompleter {

    private final LeftyPetPlugin plugin;
    private final List<String> subCommands = Arrays.asList("menu", "summon", "dismiss", "mount", "rename", "class", "altar", "help");

    public PetCommand(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya bisa dijalankan oleh player di dalam game!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            PetMenu.open(player, plugin);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "summon" -> {
                plugin.getPetManager().summonPet(player);
            }
            case "dismiss", "hide" -> {
                if (plugin.getPetManager().isPetSummoned(player.getUniqueId())) {
                    plugin.getPetManager().despawnPet(player.getUniqueId());
                    player.sendMessage(plugin.getConfigManager().getMessage("pet-dismissed"));
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§ePet kamu memang sedang tidak dipanggil.");
                }
            }
            case "mount", "ride" -> {
                plugin.getMountManager().startMount(player);
            }
            case "rename" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cGunakan: /pet rename <nama baru>");
                    return true;
                }
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    nameBuilder.append(args[i]).append(" ");
                }
                String newName = nameBuilder.toString().trim();
                PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                data.setName(newName);

                var active = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (active != null) {
                    active.updateNameTag();
                }

                String msg = plugin.getConfigManager().getMessage("pet-renamed")
                        .replace("{name}", ColorUtil.colorize(newName));
                player.sendMessage(msg);
            }
            case "class" -> {
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cPilih kelas: FIGHTER, SUPPORT, LOOTER, TRAVELER");
                    return true;
                }
                try {
                    PetClass pc = PetClass.valueOf(args[1].toUpperCase());
                    PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                    data.setPetClass(pc);
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aKelas pet diubah ke: " + pc.getDisplayName());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cKelas tidak valid! Pilihan: FIGHTER, SUPPORT, LOOTER, TRAVELER");
                }
            }
            case "altar" -> {
                player.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aKamu menerima 1x Pet Training Altar! Letakkan di tanah untuk mulai AFK training.");
            }
            case "help" -> {
                player.sendMessage("§8§m----------------§r §b§lLeftyPet Commands §8§m----------------");
                player.sendMessage("§b/pet §7- Buka menu GUI Pet");
                player.sendMessage("§b/pet summon §7- Panggil pet ke samping bahumu");
                player.sendMessage("§b/pet dismiss §7- Simpan pet ke alam spiritual");
                player.sendMessage("§b/pet mount §7- Naiki pet (unlocked Lv 3+)");
                player.sendMessage("§b/pet rename <nama> §7- Beri nama pet kamu");
                player.sendMessage("§b/pet class <class> §7- Pilih class spesialisasi pet");
                player.sendMessage("§b/pet altar §7- Dapatkan Altar training AFK");
                player.sendMessage("§8§m--------------------------------------------------");
            }
            default -> {
                PetMenu.open(player, plugin);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.stream().filter(s -> s.startsWith(input)).toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase("class")) {
            String input = args[1].toUpperCase();
            return Arrays.stream(PetClass.values()).map(Enum::name).filter(s -> s.startsWith(input)).toList();
        }
        return new ArrayList<>();
    }
}
