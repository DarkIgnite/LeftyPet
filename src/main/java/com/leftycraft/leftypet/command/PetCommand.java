package com.leftycraft.leftypet.command;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.PetMenu;
import com.leftycraft.leftypet.gui.PetRoadmapMenu;
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

import org.bukkit.Bukkit;

public class PetCommand implements CommandExecutor, TabCompleter {

    private final LeftyPetPlugin plugin;
    private final List<String> subCommands = Arrays.asList("menu", "summon", "dismiss", "rename", "class", "altar", "roadmap", "duel", "help");

    public PetCommand(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371>ᴘᴇʀɪɴᴛᴀʜ ɪɴɪ ʜᴀɴʏᴀ ʙɪsᴀ ᴅɪᴊᴀʟᴀɴᴋᴀɴ ᴏʟᴇʜ ᴘʟᴀʏᴇʀ ᴅɪ ᴅᴀʟᴀᴍ ɢᴀᴍᴇ!</gradient>"));
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
                    plugin.getPetManager().setSessionDismissed(player.getUniqueId(), true);
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-dismissed")));
                } else {
                    plugin.getPetManager().setSessionDismissed(player.getUniqueId(), true);
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>ᴘᴇᴛ ᴋᴀᴍᴜ ᴍᴇᴍᴀɴɢ sᴇᴅᴀɴɢ ᴛɪᴅᴀᴋ ᴅɪᴘᴀɴɢɢɪʟ.</yellow>"));
                }
            }
            case "rename" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɢᴜɴᴀᴋᴀɴ: /pet rename [nama baru]</red>"));
                    return true;
                }
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    nameBuilder.append(args[i]).append(" ");
                }
                String newName = nameBuilder.toString().trim();
                String stripped = ColorUtil.stripFormatting(newName);
                if (stripped.isEmpty()) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɴᴀᴍᴀ ᴘᴇᴛ ᴛɪᴅᴀᴋ ʙᴏʟᴇʜ ᴋᴏsᴏɴɢ!</red>"));
                    return true;
                }
                if (stripped.length() > 15) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ɴᴀᴍᴀ ᴘᴇᴛ ᴍᴀᴋsɪᴍᴀʟ 15 ᴋᴀʀᴀᴋᴛᴇʀ! (ᴛᴇʀᴅᴇᴛᴇᴋsɪ: " + stripped.length() + " ᴋᴀʀᴀᴋᴛᴇʀ)</red>"));
                    return true;
                }

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
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>ᴘɪʟɪʜ ᴋᴇʟᴀs: FIGHTER, SUPPORT, LOOTER, TRAVELER</yellow>"));
                    return true;
                }
                try {
                    PetClass pc = PetClass.valueOf(args[1].toUpperCase());
                    PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                    data.setPetClass(pc);
                    ActivePet active = plugin.getPetManager().getActivePet(player.getUniqueId());
                    if (active != null) {
                        active.updateNameTag();
                    }
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<green>ᴋᴇʟᴀs ᴘᴇᴛ ᴅɪᴜʙᴀʜ ᴋᴇ: </green>" + pc.getDisplayName()));
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ᴋᴇʟᴀs ᴛɪᴅᴀᴋ ᴠᴀʟɪᴅ! ᴘɪʟɪʜᴀɴ: FIGHTER, SUPPORT, LOOTER, TRAVELER</red>"));
                }
            }
            case "altar" -> {
                // 1. Cek apakah inventory penuh
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<red>ɪɴᴠᴇɴᴛᴏʀʏ ᴋᴀᴍᴜ ᴘᴇɴᴜʜ! ᴋᴏsᴏɴɢᴋᴀɴ sᴇᴛɪᴅᴀᴋɴʏᴀ 1 sʟᴏᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ.</red>"));
                    return true;
                }

                // 2. Cek limit harian (maksimal 3x per hari)
                PetData data = plugin.getPetManager().getPetData(player.getUniqueId());
                String today = java.time.LocalDate.now().toString();
                if (!today.equals(data.getLastAltarClaimDate())) {
                    data.setLastAltarClaimDate(today);
                    data.setDailyAltarClaims(0);
                }

                int maxDaily = 3;
                boolean isAdmin = player.hasPermission("leftypet.admin");
                if (!isAdmin && data.getDailyAltarClaims() >= maxDaily) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                            "<red>ᴋᴀᴍᴜ sᴜᴅᴀʜ ᴍᴇɴᴄᴀᴘᴀɪ ʙᴀᴛᴀs ᴋʟᴀɪᴍ ᴀʟᴛᴀʀ ʜᴀʀɪ ɪɴɪ (" + maxDaily + "/" + maxDaily + ")! sɪʟᴀᴋᴀɴ ᴄᴏʙᴀ ʟᴀɢɪ ʙᴇsᴏᴋ.</red>"));
                    return true;
                }

                data.setDailyAltarClaims(data.getDailyAltarClaims() + 1);
                plugin.getPetManager().savePetData(player.getUniqueId());

                player.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                int claims = data.getDailyAltarClaims();
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                        "<gradient:#43e97b:#38f9d7>ᴋᴀᴍᴜ ᴍᴇɴᴇʀɪᴍᴀ 1x ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)!</gradient> <gray>(" + claims + "/" + maxDaily + " ʜᴀʀɪ ɪɴɪ)</gray>"));
            }
            case "roadmap" -> {
                PetRoadmapMenu.open(player, plugin);
            }
            case "duel" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>ɢᴜɴᴀᴋᴀɴ: /pet duel <pemain> [taruhan] atau /pet duel accept/decline</yellow>"));
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("accept")) {
                    plugin.getPetDuelManager().acceptChallenge(player);
                    return true;
                }
                if (action.equals("decline")) {
                    plugin.getPetDuelManager().declineChallenge(player);
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ᴘᴇᴍᴀɪɴ '" + args[1] + "' ᴛɪᴅᴀᴋ ᴅɪᴛᴇᴍᴜᴋᴀɴ ᴀᴛᴀᴜ sᴇᴅᴀɴɢ ᴏғғʟɪɴᴇ!</red>"));
                    return true;
                }
                double bet = 0.0;
                if (args.length >= 3) {
                    try {
                        bet = Double.parseDouble(args[2]);
                        if (bet < 0) bet = 0.0;
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ᴊᴜᴍʟᴀʜ ᴛᴀʀᴜʜᴀɴ ʜᴀʀᴜs ʙᴇʀᴜᴘᴀ ᴀɴɢᴋᴀ ᴠᴀʟɪᴅ!</red>"));
                        return true;
                    }
                }
                plugin.getPetDuelManager().sendChallenge(player, target, bet);
            }
            case "help" -> {
                player.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>---------------- [ʟᴇғᴛʏᴘᴇᴛ ᴄᴏᴍᴍᴀɴᴅs] ----------------</b></gradient>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet</aqua> <gray>- ʙᴜᴋᴀ ᴍᴇɴᴜ ɢᴜɪ ᴘᴇᴛ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet summon</aqua> <gray>- ᴘᴀɴɢɢɪʟ ᴘᴇᴛ ᴋᴇ sᴀᴍᴘɪɴɢ ʙᴀʜᴜᴍᴜ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet dismiss</aqua> <gray>- sɪᴍᴘᴀɴ ᴘᴇᴛ ᴋᴇ ᴀʟᴀᴍ sᴘɪʀɪᴛᴜᴀʟ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet rename [nama]</aqua> <gray>- ʙᴇʀɪ ɴᴀᴍᴀ ᴘᴇᴛ ᴋᴀᴍᴜ (ᴍᴀᴋs. 15 ᴋᴀʀᴀᴋᴛᴇʀ)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet roadmap</aqua> <gray>- ʟɪʜᴀᴛ ᴘᴏʜᴏɴ ᴘʀᴏɢʀᴇsɪ ʟᴇᴠᴇʟ ᴘᴇᴛ (ᴀᴜʀᴀsᴋɪʟʟs sᴛʏʟᴇ)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet class [class]</aqua> <gray>- ᴘɪʟɪʜ ᴋᴇʟᴀs sᴘᴇsɪᴀʟɪsᴀsɪ ᴘᴇᴛ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet altar</aqua> <gray>- ᴅᴀᴘᴀᴛᴋᴀɴ ᴀʟᴛᴀʀ ᴛʀᴀɪɴɪɴɢ ᴀғᴋ (3x3) (ᴍᴀᴋs. 3x/ʜᴀʀɪ)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet duel [pemain] [taruhan]</aqua> <gray>- ᴛᴀɴᴛᴀɴɢ ᴘᴇᴛ ᴘᴇᴍᴀɪɴ ʟᴀɪɴ ʙᴇʀᴅᴜᴇʟ</gray>"));
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
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("class")) {
                String input = args[1].toUpperCase();
                return Arrays.stream(PetClass.values()).map(Enum::name).filter(s -> s.startsWith(input)).toList();
            } else if (args[0].equalsIgnoreCase("duel")) {
                String input = args[1].toLowerCase();
                List<String> suggestions = new ArrayList<>();
                if ("accept".startsWith(input)) suggestions.add("accept");
                if ("decline".startsWith(input)) suggestions.add("decline");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player sp && sp.getUniqueId().equals(p.getUniqueId())) continue;
                    if (p.getName().toLowerCase().startsWith(input)) {
                        suggestions.add(p.getName());
                    }
                }
                return suggestions;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("duel")) {
            if (!args[1].equalsIgnoreCase("accept") && !args[1].equalsIgnoreCase("decline")) {
                return Arrays.asList("0", "100", "500", "1000", "5000");
            }
        }
        return new ArrayList<>();
    }
}
