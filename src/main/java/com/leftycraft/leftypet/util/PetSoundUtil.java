package com.leftycraft.leftypet.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Provides thematic, immersive sound personalities for each Pet Skin.
 */
public final class PetSoundUtil {

    private PetSoundUtil() {}

    public static void playSummonSound(Player player, String skinKey) {
        if (player == null || !player.isOnline()) return;
        Location loc = player.getLocation();
        String key = (skinKey != null) ? skinKey.toLowerCase() : "spirit";

        switch (key) {
            case "celestial_gojo" -> {
                player.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 0.9f);
                player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
            }
            case "celestial_tanjiro" -> {
                player.playSound(loc, Sound.ITEM_TRIDENT_RIPTIDE_2, 0.7f, 1.3f);
                player.playSound(loc, Sound.ITEM_FLINTANDSTEEL_USE, 0.8f, 0.9f);
            }
            case "celestial_zenitsu" -> {
                player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.8f);
                player.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 0.6f, 1.6f);
            }
            case "celestial_nezuko" -> {
                player.playSound(loc, Sound.ENTITY_FOX_AMBIENT, 0.8f, 1.6f);
                player.playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_STEP, 0.9f, 1.4f);
            }
            case "celestial_kakashi" -> {
                player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.5f);
                player.playSound(loc, Sound.ITEM_CROSSBOW_SHOOT, 0.7f, 1.6f);
            }
            case "celestial_kitsune" -> {
                player.playSound(loc, Sound.ENTITY_FOX_SCREECH, 0.8f, 1.7f);
                player.playSound(loc, Sound.BLOCK_BELL_USE, 0.5f, 1.8f);
            }
            case "celestial_samurai" -> {
                player.playSound(loc, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 0.9f, 1.2f);
                player.playSound(loc, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.6f);
            }
            case "dragon" -> {
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.8f);
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.4f);
            }
            case "panda" -> player.playSound(loc, Sound.ENTITY_PANDA_PRE_SNEEZE, 0.8f, 1.2f);
            case "fox" -> player.playSound(loc, Sound.ENTITY_FOX_SCREECH, 0.7f, 1.4f);
            case "cat" -> player.playSound(loc, Sound.ENTITY_CAT_PURREOW, 0.8f, 1.2f);
            case "demon" -> {
                player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 0.8f);
                player.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.5f, 1.4f);
            }
            case "angel" -> {
                player.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);
                player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.4f);
            }
            default -> player.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.7f, 1.5f);
        }
    }

    public static void playCombatSound(Location loc, String skinKey) {
        if (loc == null || loc.getWorld() == null) return;
        String key = (skinKey != null) ? skinKey.toLowerCase() : "spirit";

        switch (key) {
            case "celestial_gojo" -> {
                loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.8f, 0.6f);
                loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.35f, 1.9f);
            }
            case "celestial_tanjiro" -> {
                loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_THROW, 0.7f, 1.4f);
                loc.getWorld().playSound(loc, Sound.ITEM_FLINTANDSTEEL_USE, 0.6f, 1.2f);
            }
            case "celestial_zenitsu" -> {
                loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 0.7f, 1.8f);
                loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.6f, 1.9f);
            }
            case "celestial_nezuko" -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 0.7f, 1.5f);
                loc.getWorld().playSound(loc, Sound.ENTITY_PANDA_BITE, 0.6f, 1.4f);
            }
            case "celestial_kakashi" -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.6f);
                loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.4f, 1.9f);
            }
            case "celestial_kitsune" -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_FOX_BITE, 0.8f, 1.4f);
                loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.6f);
            }
            case "celestial_samurai" -> {
                loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.9f, 1.3f);
                loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_HIT, 0.4f, 1.8f);
            }
            case "dragon" -> loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.5f);
            case "panda" -> loc.getWorld().playSound(loc, Sound.ENTITY_PANDA_BITE, 0.8f, 1.3f);
            case "fox" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_BITE, 0.8f, 1.3f);
            case "cat" -> loc.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 0.7f, 1.4f);
            case "demon" -> loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_SCREAM, 0.6f, 1.3f);
            case "angel" -> loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);
            default -> loc.getWorld().playSound(loc, Sound.ENTITY_VEX_HURT, 0.6f, 1.5f);
        }
    }

    public static void playHappySound(Player player, Location loc, String skinKey) {
        if (loc == null || loc.getWorld() == null) return;
        String key = (skinKey != null) ? skinKey.toLowerCase() : "spirit";

        switch (key) {
            case "celestial_gojo" -> loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.8f);
            case "celestial_tanjiro" -> loc.getWorld().playSound(loc, Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 0.7f, 1.5f);
            case "celestial_zenitsu" -> loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.8f);
            case "celestial_nezuko" -> loc.getWorld().playSound(loc, Sound.ENTITY_PANDA_AMBIENT, 0.7f, 1.7f);
            case "celestial_kakashi" -> loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
            case "celestial_kitsune" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_SNIFF, 0.8f, 1.5f);
            case "celestial_samurai" -> loc.getWorld().playSound(loc, Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.8f);
            case "dragon" -> loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_SWOOP, 0.6f, 1.8f);
            case "panda" -> loc.getWorld().playSound(loc, Sound.ENTITY_PANDA_SNEEZE, 0.8f, 1.3f);
            case "fox" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_SNIFF, 0.8f, 1.5f);
            case "cat" -> loc.getWorld().playSound(loc, Sound.ENTITY_CAT_PURR, 0.9f, 1.4f);
            case "demon" -> loc.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.6f, 1.6f);
            case "angel" -> loc.getWorld().playSound(loc, Sound.BLOCK_BELL_USE, 0.7f, 1.8f);
            default -> loc.getWorld().playSound(loc, Sound.ENTITY_ALLAY_ITEM_TAKEN, 0.8f, 1.6f);
        }
    }

    public static void playHungrySound(Player player, Location loc, String skinKey) {
        if (loc == null || loc.getWorld() == null) return;
        String key = (skinKey != null) ? skinKey.toLowerCase() : "spirit";

        switch (key) {
            case "celestial_gojo" -> loc.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 0.7f);
            case "celestial_tanjiro" -> loc.getWorld().playSound(loc, Sound.ENTITY_HORSE_BREATHE, 0.6f, 1.3f);
            case "celestial_zenitsu" -> loc.getWorld().playSound(loc, Sound.ENTITY_PARROT_IMITATE_CREEPER, 0.4f, 1.6f);
            case "celestial_nezuko" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_SLEEP, 0.7f, 1.2f);
            case "celestial_kakashi" -> loc.getWorld().playSound(loc, Sound.ENTITY_WOLF_WHINE, 0.6f, 1.3f);
            case "celestial_kitsune" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_SPIT, 0.6f, 1.3f);
            case "celestial_samurai" -> loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SKELETON_HURT, 0.5f, 0.8f);
            case "dragon" -> loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_AMBIENT, 0.6f, 1.5f);
            case "panda" -> loc.getWorld().playSound(loc, Sound.ENTITY_PANDA_WORRIED_AMBIENT, 0.7f, 1.2f);
            case "fox" -> loc.getWorld().playSound(loc, Sound.ENTITY_FOX_SLEEP, 0.7f, 1.2f);
            case "cat" -> loc.getWorld().playSound(loc, Sound.ENTITY_CAT_BEG_FOR_FOOD, 0.8f, 1.2f);
            case "demon" -> loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_DEATH, 0.4f, 0.6f);
            case "angel" -> loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_STEP, 0.7f, 0.8f);
            default -> loc.getWorld().playSound(loc, Sound.ENTITY_VEX_AMBIENT, 0.6f, 0.8f);
        }
    }
}
