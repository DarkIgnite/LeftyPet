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
     * Creates a custom player head with HTTPS texture URL and Paper PlayerProfile API.
     * Fixes the Steve head bug caused by http:// and modern Minecraft 1.20.5+ / 1.21 profile format.
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

            if (hash != null) {
                String httpsUrl = "https://textures.minecraft.net/texture/" + hash;
                UUID profileUuid = UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));

                PlayerProfile profile = Bukkit.createProfile(profileUuid, "PetSkin");
                try {
                    URL skinUrl = URI.create(httpsUrl).toURL();
                    profile.getTextures().setSkin(skinUrl);
                } catch (Exception ignored) {}

                // Also attach clean Base64 property with https URL for clients checking property list
                String cleanJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + httpsUrl + "\"}}}";
                String cleanBase64 = Base64.getEncoder().encodeToString(cleanJson.getBytes(StandardCharsets.UTF_8));
                profile.setProperty(new ProfileProperty("textures", cleanBase64));

                meta.setPlayerProfile(profile);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[LeftyPet] Error creating custom head: " + e.getMessage());
        }

        head.setItemMeta(meta);
        return head;
    }
}
