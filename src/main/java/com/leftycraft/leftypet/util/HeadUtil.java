package com.leftycraft.leftypet.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HeadUtil {

    private static final Pattern HASH_PATTERN = Pattern.compile("textures\\.minecraft\\.net/texture/([a-f0-9]+)");

    private HeadUtil() {}

    /**
     * Creates a custom player head with valid texture.
     * Fixes Steve bug by ensuring:
     * 1. Profile username is null (so client does NOT attempt to resolve an offline player name).
     * 2. Texture URL is HTTPS.
     * 3. Both PlayerProfile property and PlayerTextures skin URL are populated.
     */
    public static ItemStack createCustomHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (texture == null || texture.trim().isEmpty()) {
            return head;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        try {
            String hash = null;
            String trimmed = texture.trim();

            if (trimmed.length() == 64 && trimmed.matches("[a-f0-9]+")) {
                hash = trimmed;
            } else {
                String decoded = trimmed;
                try {
                    decoded = new String(Base64.getDecoder().decode(trimmed), StandardCharsets.UTF_8);
                } catch (Exception ignored) {}

                Matcher matcher = HASH_PATTERN.matcher(decoded);
                if (matcher.find()) {
                    hash = matcher.group(1);
                }
            }

            String cleanB64 = trimmed;
            UUID profileUuid;
            if (hash != null) {
                String httpsUrl = "https://textures.minecraft.net/texture/" + hash;
                String cleanJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + httpsUrl + "\"}}}";
                cleanB64 = Base64.getEncoder().encodeToString(cleanJson.getBytes(StandardCharsets.UTF_8));
                profileUuid = UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
            } else {
                profileUuid = UUID.randomUUID();
            }

            // Standard Bukkit PlayerProfile
            org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(profileUuid);

            if (hash != null) {
                try {
                    org.bukkit.profile.PlayerTextures textures = profile.getTextures();
                    URL skinUrl = URI.create("https://textures.minecraft.net/texture/" + hash).toURL();
                    textures.setSkin(skinUrl);
                    profile.setTextures(textures); // CRITICAL: Must assign back to profile
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[LeftyPet] Error setting skin url: " + e.getMessage());
                }
            }

            // Set both standard Bukkit owner profile and Paper player profile property
            meta.setOwnerProfile(profile);

            if (profile instanceof com.destroystokyo.paper.profile.PlayerProfile paperProfile) {
                paperProfile.setProperty(new ProfileProperty("textures", cleanB64));
                meta.setPlayerProfile(paperProfile);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LeftyPet] Error creating custom head: " + e.getMessage());
        }

        head.setItemMeta(meta);
        return head;
    }
}
