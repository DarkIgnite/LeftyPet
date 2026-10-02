package com.leftycraft.leftypet.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private ColorUtil() {}

    /**
     * Parses text supporting both MiniMessage tags (<gradient>, <rainbow>, etc.) and legacy & codes.
     */
    public static Component component(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // If message contains MiniMessage tags (<tag>)
        if (text.contains("<") && text.contains(">")) {
            // Convert any remaining legacy & codes into MiniMessage compatible format if needed
            try {
                return MINI_MESSAGE.deserialize(text).decoration(TextDecoration.ITALIC, false);
            } catch (Exception e) {
                // fallback to legacy
            }
        }

        return LEGACY_SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Translates into legacy color code string (§) with gradient/hex support.
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";
        Component comp = component(text);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    /**
     * Sends formatted message to sender.
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(component(message));
        }
    }
}
