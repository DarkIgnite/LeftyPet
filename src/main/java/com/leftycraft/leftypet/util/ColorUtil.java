package com.leftycraft.leftypet.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public final class ColorUtil {

    private ColorUtil() {}

    /**
     * Translates alternate color codes (&) into legacy string.
     */
    @SuppressWarnings("deprecation")
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Translates alternate color codes (&) into Adventure Component without italic default.
     */
    public static Component component(String message) {
        if (message == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message)
                .decoration(TextDecoration.ITALIC, false);
    }
}
