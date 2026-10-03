package com.leftycraft.leftypet.util;

import org.bukkit.entity.Player;
import java.util.UUID;

public final class BedrockUtil {

    private BedrockUtil() {}

    public static boolean isBedrockPlayer(Player player) {
        if (player == null) return false;
        return isBedrockPlayer(player.getUniqueId(), player.getName());
    }

    public static boolean isBedrockPlayer(UUID uuid, String name) {
        // 1. Floodgate API check (if Floodgate is installed)
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object instance = floodgateApiClass.getMethod("getInstance").invoke(null);
            Object result = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class).invoke(instance, uuid);
            if (result instanceof Boolean b && b) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 2. Geyser name prefix check (Bedrock usernames typically start with '.' or '*')
        if (name != null) {
            if (name.startsWith(".") || name.startsWith("*")) {
                return true;
            }
        }

        // 3. Floodgate UUID format check (Floodgate UUIDs have 0L for most significant bits)
        if (uuid != null && uuid.getMostSignificantBits() == 0L) {
            return true;
        }

        return false;
    }
}
