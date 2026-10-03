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

public class PetCommand implements CommandExecutor, TabCompleter {

    private final LeftyPetPlugin plugin;
    private final List<String> subCommands = Arrays.asList("menu", "summon", "dismiss", "rename", "class", "altar", "roadmap", "help");

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
                player.getInventory().addItem(plugin.getAltarManager().createAltarItem());
                player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<gradient:#43e97b:#38f9d7>ᴋᴀᴍᴜ ᴍᴇɴᴇʀɪᴍᴀ 1x ᴘᴇᴛ ᴛʀᴀɪɴɪɴɢ ᴀʟᴛᴀʀ (3x3)! ʟᴇᴛᴀᴋᴋᴀɴ ᴅɪ ᴀʀᴇᴀ 3x3 ᴛᴇʀʙᴜᴋᴀ.</gradient>"));
            }
            case "roadmap" -> {
                PetRoadmapMenu.open(player, plugin);
            }
            case "help" -> {
                player.sendMessage(ColorUtil.component("<gradient:#00f2fe:#4facfe><b>---------------- [ʟᴇғᴛʏᴘᴇᴛ ᴄᴏᴍᴍᴀɴᴅs] ----------------</b></gradient>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet</aqua> <gray>- ʙᴜᴋᴀ ᴍᴇɴᴜ ɢᴜɪ ᴘᴇᴛ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet summon</aqua> <gray>- ᴘᴀɴɢɢɪʟ ᴘᴇᴛ ᴋᴇ sᴀᴍᴘɪɴɢ ʙᴀʜᴜᴍᴜ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet dismiss</aqua> <gray>- sɪᴍᴘᴀɴ ᴘᴇᴛ ᴋᴇ ᴀʟᴀᴍ sᴘɪʀɪᴛᴜᴀʟ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet rename [nama]</aqua> <gray>- ʙᴇʀɪ ɴᴀᴍᴀ ᴘᴇᴛ ᴋᴀᴍᴜ (ᴍᴀᴋs. 15 ᴋᴀʀᴀᴋᴛᴇʀ)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet roadmap</aqua> <gray>- ʟɪʜᴀᴛ ᴘᴏʜᴏɴ ᴘʀᴏɢʀᴇsɪ ʟᴇᴠᴇʟ ᴘᴇᴛ (ᴀᴜʀᴀsᴋɪʟʟs sᴛʏʟᴇ)</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet class [class]</aqua> <gray>- ᴘɪʟɪʜ ᴋᴇʟᴀs sᴘᴇsɪᴀʟɪsᴀsɪ ᴘᴇᴛ</gray>"));
                player.sendMessage(ColorUtil.component("<aqua>/pet altar</aqua> <gray>- ᴅᴀᴘᴀᴛᴋᴀɴ ᴀʟᴛᴀʀ ᴛʀᴀɪɴɪɴɢ ᴀғᴋ (3x3)</gray>"));
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
