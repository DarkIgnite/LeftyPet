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
            sender.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371>Perintah ini hanya bisa dijalankan oleh player di dalam game!</gradient>"));
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
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-dismissed")));
                } else {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>Pet kamu memang sedang tidak dipanggil.</yellow>"));
                }
            }
            case "mount", "ride" -> {
                plugin.getMountManager().startMount(player);
            }
            case "rename" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Gunakan: /pet rename <nama baru></red>"));
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
                player.sendMessage(ColorUtil.component(msg));
            }
            case "class" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>Pilih kelas: FIGHTER, SUPPORT, LOOTER, TRAVELER</yellow>"));
                    return true;
                }
                try {
                    PetClass pc = PetClass.valueOf(args[1].toUpperCase());
                    PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                    data.setPetClass(pc);
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<green>Kelas pet diubah ke: </green>" + pc.getDisplayName()));
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Kelas tidak valid! Pilihan: FIGHTER, SUPPORT, LOOTER, TRAVELER</red>"));
                }
            }
            case "altar" -> {
                player.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>Kamu menerima 1x Pet Training Altar (3x3)! Letakkan di area 3x3 terbuka.</gradient>"));
            }
            case "help" -> {
                player.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>---------------- [LeftyPet Commands] ----------------</b></gradient>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet</aqua> <gray>- Buka menu GUI Pet</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet summon</aqua> <gray>- Panggil pet ke samping bahumu</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet dismiss</aqua> <gray>- Simpan pet ke alam spiritual</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet mount</aqua> <gray>- Naiki pet (unlocked Lv 3+)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet rename &lt;nama&gt;</aqua> <gray>- Beri nama pet kamu</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet class &lt;class&gt;</aqua> <gray>- Pilih class spesialisasi pet</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet altar</aqua> <gray>- Dapatkan Altar training AFK (3x3)</gray>"));
                player.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>-----------------------------------------------------</b></gradient>"));
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
