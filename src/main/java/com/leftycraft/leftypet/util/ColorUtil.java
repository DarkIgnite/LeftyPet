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
     * Converts a string to modern small-caps font.
     */
    public static String toSmallCaps(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(switch (Character.toLowerCase(c)) {
                case 'a' -> 'ᴀ';
                case 'b' -> 'ʙ';
                case 'c' -> 'ᴄ';
                case 'd' -> 'ᴅ';
                case 'e' -> 'ᴇ';
                case 'f' -> 'ғ';
                case 'g' -> 'ɢ';
                case 'h' -> 'ʜ';
                case 'i' -> 'ɪ';
                case 'j' -> 'ᴊ';
                case 'k' -> 'ᴋ';
                case 'l' -> 'ʟ';
                case 'm' -> 'ᴍ';
                case 'n' -> 'ɴ';
                case 'o' -> 'ᴏ';
                case 'p' -> 'ᴘ';
                case 'q' -> 'ǫ';
                case 'r' -> 'ʀ';
                case 's' -> 's';
                case 't' -> 'ᴛ';
                case 'u' -> 'ᴜ';
                case 'v' -> 'ᴠ';
                case 'w' -> 'ᴡ';
                case 'x' -> 'x';
                case 'y' -> 'ʏ';
                case 'z' -> 'ᴢ';
                default -> c;
            });
        }
        return sb.toString();
    }

    /**
     * Parses text supporting MiniMessage tags (<gradient>, <rainbow>, etc.), legacy & codes, and § codes.
     */
    public static Component component(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String converted = convertToMiniMessage(text);
        try {
            return MINI_MESSAGE.deserialize(converted).decoration(TextDecoration.ITALIC, false);
        } catch (Exception e) {
            // Fallback to legacy serializer if MiniMessage fails
            String safeLegacy = text.replace('§', '&');
            return LEGACY_SERIALIZER.deserialize(safeLegacy).decoration(TextDecoration.ITALIC, false);
        }
    }

    /**
     * Converts legacy ampersand (&) and section (§) color/style codes into valid MiniMessage tags.
     */
    public static String convertToMiniMessage(String input) {
        if (input == null || input.isEmpty()) return "";

        // First convert HTML entities &lt; and &gt; so &l is not mistakenly parsed as <b>
        String s = input.replace("&lt;", "[").replace("&gt;", "]");

        // Strip Variation Selectors (U+FE0F / U+FE0E) which cause "VS 16" square boxes in Minecraft
        s = s.replace("\uFE0F", "").replace("\uFE0E", "");

        // Sanitize any duplicated dollar signs like "$$500" or "$ $500" everywhere
        s = s.replaceAll("\\${2,}", "\\$").replace("$ ", "$");

        // 1. Hex codes: &#123456 or §#123456 -> <#123456>
        s = s.replaceAll("[&§]#([0-9a-fA-F]{6})", "<#$1>");

        // 2. Spigot hex format: &x&r&r&g&g&b&b or §x§r§r§g§g§b§b -> <#rrggbb>
        s = s.replaceAll("[&§]x[&§]([0-9a-fA-F])[&§]([0-9a-fA-F])[&§]([0-9a-fA-F])[&§]([0-9a-fA-F])[&§]([0-9a-fA-F])[&§]([0-9a-fA-F])", "<#$1$2$3$4$5$6>");

        // 3. Standard Minecraft color codes
        s = s.replaceAll("[&§]0", "<black>")
                .replaceAll("[&§]1", "<dark_blue>")
                .replaceAll("[&§]2", "<dark_green>")
                .replaceAll("[&§]3", "<dark_aqua>")
                .replaceAll("[&§]4", "<dark_red>")
                .replaceAll("[&§]5", "<dark_purple>")
                .replaceAll("[&§]6", "<gold>")
                .replaceAll("[&§]7", "<gray>")
                .replaceAll("[&§]8", "<dark_gray>")
                .replaceAll("[&§]9", "<blue>")
                .replaceAll("[&§][aA]", "<green>")
                .replaceAll("[&§][bB]", "<aqua>")
                .replaceAll("[&§][cC]", "<red>")
                .replaceAll("[&§][dD]", "<light_purple>")
                .replaceAll("[&§][eE]", "<yellow>")
                .replaceAll("[&§][fF]", "<white>")
                .replaceAll("[&§][kK]", "<obfuscated>")
                .replaceAll("[&§][lL]", "<b>")
                .replaceAll("[&§][mM]", "<strikethrough>")
                .replaceAll("[&§][nN]", "<u>")
                .replaceAll("[&§][oO]", "<i>")
                .replaceAll("[&§][rR]", "<reset>");

        // 4. Strip any remaining rogue section symbols so MiniMessage parser never throws ParsingException
        s = s.replace("§", "");

        return s;
    }

    /**
     * Returns rich, vibrant gradient & bold level tags across different tiers (Revisi 4).
     */
    public static String getLevelTag(int level) {
        if (level >= 100) {
            return "<gradient:#ff007f:#7928ca:#00dfd8><b>[ʟᴠ.100 ᴍʏᴛʜɪᴄ]</b></gradient>";
        } else if (level >= 90) {
            return "<gradient:#ff0844:#ffb199><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 80) {
            return "<gradient:#f857a6:#ff5858><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 70) {
            return "<gradient:#6a11cb:#2575fc><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 60) {
            return "<gradient:#11998e:#38ef7d><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 50) {
            return "<gradient:#f7971e:#ffd200><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 40) {
            return "<gradient:#fa709a:#fee140><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 30) {
            return "<gradient:#4facfe:#00f2fe><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 20) {
            return "<gradient:#a18cd1:#fbc2eb><b>[ʟᴠ." + level + "]</b></gradient>";
        } else if (level >= 10) {
            return "<gradient:#43e97b:#38f9d7><b>[ʟᴠ." + level + "]</b></gradient>";
        } else {
            return "<gradient:#e0eafc:#cfdef3><b>[ʟᴠ." + level + "]</b></gradient>";
        }
    }

    /**
     * Strips both MiniMessage tags and legacy color/formatting codes to get raw visible text.
     */
    public static String stripFormatting(String input) {
        if (input == null || input.isEmpty()) return "";
        // Remove MiniMessage tags like <gradient:...>, </gradient>, <color:...>, <b>, etc.
        String stripped = input.replaceAll("<[^>]*>", "");
        // Remove legacy ampersand and section codes (&a, §a, &#123456, &x...)
        stripped = stripped.replaceAll("(?i)[&§][0-9a-fk-or]", "");
        stripped = stripped.replaceAll("(?i)[&§]#([0-9a-f]{6})", "");
        stripped = stripped.replaceAll("(?i)[&§]x([&§][0-9a-f]){6}", "");
        return stripped.trim();
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
