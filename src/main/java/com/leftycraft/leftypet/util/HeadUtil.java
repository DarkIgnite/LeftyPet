package com.leftycraft.leftypet.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class HeadUtil {

    private HeadUtil() {}

    /**
     * Creates a custom player head with Base64 texture using Paper's PlayerProfile API.
     */
    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64Texture == null || base64Texture.isEmpty()) {
            return head;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "PetSkin");
            profile.setProperty(new ProfileProperty("textures", base64Texture));
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }
}
