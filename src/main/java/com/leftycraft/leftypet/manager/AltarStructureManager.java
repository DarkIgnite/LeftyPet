package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

public class AltarStructureManager {

    private final LeftyPetPlugin plugin;

    public AltarStructureManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    public record PlacementCheck(boolean success, String reason, Location obstacleLoc, Material obstacleMat) {
        public static PlacementCheck ok() {
            return new PlacementCheck(true, null, null, null);
        }
        public static PlacementCheck failed(String reason, Location obstacleLoc, Material obstacleMat) {
            return new PlacementCheck(false, reason, obstacleLoc, obstacleMat);
        }
    }

    /**
     * Checks if the 3x3x4 volume around the center lodestone can be built on (like pet kitchen).
     * Includes land protection check, collision check with other altars/kitchens, and block clearance check.
     */
    public PlacementCheck checkPlacement(org.bukkit.entity.Player player, Location center) {
        if (center == null || center.getWorld() == null) {
            return PlacementCheck.failed("Lokasi tidak valid!", null, null);
        }

        // 1. Land protection check (RedProtect & WorldGuard)
        if (player != null) {
            com.leftycraft.leftypet.hook.ProtectionHookManager.ProtectionResult protRes =
                    plugin.getProtectionHookManager().canPlaceAltar(player, center);
            if (!protRes.allowed()) {
                return PlacementCheck.failed("ᴛᴇʀʟɪɴᴅᴜɴɢɪ " + protRes.pluginName() + " (" + protRes.claimInfo() + ")! Bukan land milikmu.", center, Material.BARRIER);
            }
        }

        int ox = center.getBlockX();
        int oy = center.getBlockY();
        int oz = center.getBlockZ();

        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location worldLoc = center.clone().add(x, y, z);

                    // Check if block overlaps with an existing registered altar
                    if (plugin.getAltarManager().isAltarAreaOrBuilding(worldLoc)) {
                        return PlacementCheck.failed("ᴛᴇʀʟᴀʟᴜ ᴅᴇᴋᴀᴛ ᴅᴇɴɢᴀɴ ᴀʟᴛᴀʀ ᴘᴇᴛ ʟᴀɪɴ!", worldLoc, worldLoc.getBlock().getType());
                    }

                    // Check if block overlaps with a kitchen
                    if (plugin.getKitchenManager().isKitchenAreaOrBuilding(worldLoc)) {
                        return PlacementCheck.failed("ᴛᴇʀʟᴀʟᴜ ᴅᴇᴋᴀᴛ ᴅᴇɴɢᴀɴ ᴅᴀᴘᴜʀ ᴍʙɢ!", worldLoc, worldLoc.getBlock().getType());
                    }

                    // Check if this location is the placement origin itself (the lodestone item being placed)
                    boolean isOrigin = (x == 0 && y == 0 && z == 0);
                    if (!isOrigin) {
                        Block b = worldLoc.getBlock();
                        Material mat = b.getType();
                        if (!isPassableOrAir(mat)) {
                            return PlacementCheck.failed("ᴛᴇʀʜᴀʟᴀɴɢ ʙʟᴏᴋ " + formatMaterial(mat) + " ᴅɪ X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ(), worldLoc, mat);
                        }
                    }
                }
            }
        }
        return PlacementCheck.ok();
    }

    public PlacementCheck checkPlacement(Location center) {
        return checkPlacement(null, center);
    }

    public boolean canPlaceStructure(Location center) {
        return checkPlacement(center).success();
    }

    public boolean isPassableOrAir(Material mat) {
        if (mat == null || mat.isAir()) return true;
        if (mat == Material.SHORT_GRASS || mat == Material.TALL_GRASS) return true;
        if (mat == Material.FERN || mat == Material.LARGE_FERN) return true;
        if (mat == Material.DEAD_BUSH || mat == Material.SNOW || mat == Material.LIGHT) return true;
        if (mat == Material.VINE || mat == Material.GLOW_LICHEN || mat == Material.HANGING_ROOTS) return true;
        try {
            if (org.bukkit.Tag.FLOWERS.isTagged(mat)) return true;
        } catch (Throwable ignored) {}
        return mat.name().contains("FLOWER") || mat == Material.POPPY || mat == Material.DANDELION;
    }

    public String formatMaterial(Material mat) {
        if (mat == null) return "Unknown";
        String name = mat.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Builds the Altar structure layer by layer with 0.5 second delay (10 ticks) between each layer and sound effects.
     */
    public void buildStructureAnimated(Location center, int level, Runnable onComplete) {
        plugin.getAltarManager().setBuilding(center, true);

        // Layer 0: Base (Y=0) immediately
        buildLayerBase(center, level);
        center.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.2f);
        center.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(0.5, 0.5, 0.5), 18, 0.8, 0.1, 0.8, 0.05);

        // Layer 1: Glass Lower (Y=1) after 0.5 second (10 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            buildGlassLevel(center, level, 1);
            center.getWorld().playSound(center, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.0f);
            center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.0f);
            center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0.5, 1.5, 0.5), 15, 0.8, 0.2, 0.8, 0.03);

            // Layer 2: Glass Upper (Y=2) after 1.0 second (10 ticks later)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                buildGlassLevel(center, level, 2);
                center.getWorld().playSound(center, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.2f);
                center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.3f);
                center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0.5, 2.5, 0.5), 18, 0.8, 0.2, 0.8, 0.03);

                // Layer 3: Roof Slabs (Y=3) after 1.5 seconds (10 ticks later)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    buildLayerRoof(center, level);
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.4f);
                    center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
                    Particle finishP = (level == 6) ? Particle.WAX_ON :
                            (level == 5 ? Particle.SOUL_FIRE_FLAME : Particle.TOTEM_OF_UNDYING);
                    center.getWorld().spawnParticle(finishP, center.clone().add(0.5, 3.2, 0.5), 30, 0.8, 0.2, 0.8, 0.1);

                    plugin.getAltarManager().setBuilding(center, false);

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }, 10L);
            }, 10L);
        }, 10L);
    }

    /**
     * Instant build or transformation of the 3x3x4 Altar structure.
     */
    public void buildStructure(Location center, int level) {
        buildLayerBase(center, level);
        buildGlassLevel(center, level, 1);
        buildGlassLevel(center, level, 2);
        buildLayerRoof(center, level);

        Particle p = switch (level) {
            case 6 -> Particle.WAX_ON;
            case 5 -> Particle.SOUL_FIRE_FLAME;
            case 4 -> Particle.TOTEM_OF_UNDYING;
            case 3 -> Particle.PORTAL;
            case 2 -> Particle.END_ROD;
            default -> Particle.ENCHANT;
        };
        center.getWorld().spawnParticle(p, center.clone().add(0.5, 1.5, 0.5), 45, 1.2, 1.2, 1.2, 0.05);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.0f + (level * 0.15f));
    }

    private void buildLayerBase(Location center, int level) {
        center.getBlock().setType(Material.LODESTONE);

        if (level == 6) {
            // Level 6 (level6.schem yellow glazed terracotta & yellow concrete pattern)
            setBlockData(center.clone().add(-1, 0, -1).getBlock(), "minecraft:yellow_glazed_terracotta[facing=west]");
            setBlockData(center.clone().add(0, 0, -1).getBlock(), "minecraft:yellow_concrete");
            setBlockData(center.clone().add(1, 0, -1).getBlock(), "minecraft:yellow_glazed_terracotta[facing=north]");

            setBlockData(center.clone().add(-1, 0, 0).getBlock(), "minecraft:yellow_concrete");
            setBlockData(center.clone().add(1, 0, 0).getBlock(), "minecraft:yellow_concrete");

            setBlockData(center.clone().add(-1, 0, 1).getBlock(), "minecraft:yellow_glazed_terracotta[facing=south]");
            setBlockData(center.clone().add(0, 0, 1).getBlock(), "minecraft:yellow_concrete");
            setBlockData(center.clone().add(1, 0, 1).getBlock(), "minecraft:yellow_glazed_terracotta[facing=east]");
            return;
        }

        if (level == 5) {
            // Level 5 (level5.schem light blue glazed terracotta & light blue concrete pattern)
            setBlockData(center.clone().add(-1, 0, -1).getBlock(), "minecraft:light_blue_glazed_terracotta[facing=north]");
            setBlockData(center.clone().add(0, 0, -1).getBlock(), "minecraft:light_blue_concrete");
            setBlockData(center.clone().add(1, 0, -1).getBlock(), "minecraft:light_blue_glazed_terracotta[facing=east]");

            setBlockData(center.clone().add(-1, 0, 0).getBlock(), "minecraft:light_blue_concrete");
            setBlockData(center.clone().add(1, 0, 0).getBlock(), "minecraft:light_blue_concrete");

            setBlockData(center.clone().add(-1, 0, 1).getBlock(), "minecraft:light_blue_glazed_terracotta[facing=north]");
            setBlockData(center.clone().add(0, 0, 1).getBlock(), "minecraft:light_blue_concrete");
            setBlockData(center.clone().add(1, 0, 1).getBlock(), "minecraft:light_blue_glazed_terracotta[facing=east]");
            return;
        }

        if (level == 4) {
            // Level 4 (level4.schem exact terracotta pattern)
            setBlockData(center.clone().add(-1, 0, -1).getBlock(), "minecraft:pink_glazed_terracotta[facing=north]");
            setBlockData(center.clone().add(0, 0, -1).getBlock(), "minecraft:magenta_glazed_terracotta[facing=west]");
            setBlockData(center.clone().add(1, 0, -1).getBlock(), "minecraft:pink_glazed_terracotta[facing=east]");

            setBlockData(center.clone().add(-1, 0, 0).getBlock(), "minecraft:magenta_glazed_terracotta[facing=west]");
            setBlockData(center.clone().add(1, 0, 0).getBlock(), "minecraft:magenta_glazed_terracotta[facing=east]");

            setBlockData(center.clone().add(-1, 0, 1).getBlock(), "minecraft:pink_glazed_terracotta[facing=west]");
            setBlockData(center.clone().add(0, 0, 1).getBlock(), "minecraft:magenta_glazed_terracotta[facing=south]");
            setBlockData(center.clone().add(1, 0, 1).getBlock(), "minecraft:pink_glazed_terracotta[facing=east]");
            return;
        }

        Material cornerMat = switch (level) {
            case 3 -> Material.PURPUR_PILLAR;
            case 2 -> Material.END_STONE_BRICKS;
            default -> Material.CHISELED_STONE_BRICKS;
        };

        Material stairsMat = switch (level) {
            case 3 -> Material.PURPUR_STAIRS;
            case 2 -> Material.END_STONE_BRICK_STAIRS;
            default -> Material.STONE_STAIRS;
        };

        setCorner(center.clone().add(-1, 0, -1).getBlock(), cornerMat);
        setCorner(center.clone().add(1, 0, -1).getBlock(), cornerMat);
        setCorner(center.clone().add(-1, 0, 1).getBlock(), cornerMat);
        setCorner(center.clone().add(1, 0, 1).getBlock(), cornerMat);

        setStairs(center.clone().add(0, 0, -1).getBlock(), stairsMat, BlockFace.NORTH);
        setStairs(center.clone().add(0, 0, 1).getBlock(), stairsMat, BlockFace.SOUTH);
        setStairs(center.clone().add(1, 0, 0).getBlock(), stairsMat, BlockFace.EAST);
        setStairs(center.clone().add(-1, 0, 0).getBlock(), stairsMat, BlockFace.WEST);
    }

    private void buildGlassLevel(Location center, int level, int y) {
        Material glassMat = switch (level) {
            case 6 -> Material.YELLOW_STAINED_GLASS;
            case 5 -> Material.BLUE_STAINED_GLASS;
            case 4 -> Material.PINK_STAINED_GLASS;
            case 3 -> Material.MAGENTA_STAINED_GLASS;
            case 2 -> Material.LIGHT_BLUE_STAINED_GLASS;
            default -> Material.WHITE_STAINED_GLASS;
        };

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = center.clone().add(x, y, z).getBlock();
                if (x == 0 && z == 0) {
                    b.setType(Material.AIR);
                } else {
                    b.setType(glassMat);
                }
            }
        }
    }

    private void buildLayerRoof(Location center, int level) {
        if (level == 6) {
            // Level 6 (level6.schem bamboo mosaic slab)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = center.clone().add(x, 3, z).getBlock();
                    setBlockData(b, "minecraft:bamboo_mosaic_slab[type=bottom,waterlogged=false]");
                }
            }
            return;
        }

        if (level == 5) {
            // Level 5 (level5.schem prismarine slab corners + prismarine brick slab cross)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = center.clone().add(x, 3, z).getBlock();
                    boolean isCorner = (x != 0 && z != 0);
                    if (isCorner) {
                        setBlockData(b, "minecraft:prismarine_slab[type=bottom,waterlogged=false]");
                    } else {
                        setBlockData(b, "minecraft:prismarine_brick_slab[type=bottom,waterlogged=false]");
                    }
                }
            }
            return;
        }

        if (level == 4) {
            // Level 4 (level4.schem exact cherry slab)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = center.clone().add(x, 3, z).getBlock();
                    setBlockData(b, "minecraft:cherry_slab[type=bottom,waterlogged=false]");
                }
            }
            return;
        }

        Material slabMat = switch (level) {
            case 3 -> Material.PURPUR_SLAB;
            case 2 -> Material.END_STONE_BRICK_SLAB;
            default -> Material.POLISHED_DIORITE_SLAB;
        };

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = center.clone().add(x, 3, z).getBlock();
                setBottomSlab(b, slabMat);
            }
        }
    }

    private void setBlockData(Block block, String dataString) {
        try {
            BlockData data = Bukkit.createBlockData(dataString);
            block.setBlockData(data, false);
        } catch (Exception e) {
            plugin.getLogger().warning("[LeftyPet] Error setting block data " + dataString + ": " + e.getMessage());
        }
    }

    private void setCorner(Block block, Material mat) {
        block.setType(mat);
        if (mat == Material.PURPUR_PILLAR && block.getBlockData() instanceof Orientable orientable) {
            orientable.setAxis(Axis.Y);
            block.setBlockData(orientable);
        }
    }

    private void setStairs(Block block, Material mat, BlockFace facing) {
        block.setType(mat);
        if (block.getBlockData() instanceof Stairs stairs) {
            stairs.setFacing(facing);
            stairs.setHalf(Bisected.Half.TOP);
            block.setBlockData(stairs);
        }
    }

    private void setBottomSlab(Block block, Material mat) {
        block.setType(mat);
        if (block.getBlockData() instanceof Slab slab) {
            slab.setType(Slab.Type.BOTTOM);
            block.setBlockData(slab);
        }
    }

    public void removeStructure(Location center) {
        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    center.clone().add(x, y, z).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    public boolean isPartOfStructure(Location center, Location target) {
        if (!center.getWorld().equals(target.getWorld())) return false;
        int dx = target.getBlockX() - center.getBlockX();
        int dy = target.getBlockY() - center.getBlockY();
        int dz = target.getBlockZ() - center.getBlockZ();
        return (dx >= -1 && dx <= 1) && (dz >= -1 && dz <= 1) && (dy >= 0 && dy <= 3);
    }
}
