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

    /**
     * Checks if the 3x3x4 volume around the center lodestone can be built on.
     * Y=0 is the floor level (dirt, stone, grass, etc. can be replaced, but not bedrock, chests, or another altar).
     * Y=1..3 is the chamber & roof (must be clear of solid blocks).
     */
    public boolean canPlaceStructure(Location center) {
        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block b = center.clone().add(x, y, z).getBlock();

                    // Check if block overlaps with an existing registered altar
                    for (Location aLoc : plugin.getAltarManager().getAltarsMap().keySet()) {
                        if (isPartOfStructure(aLoc, b.getLocation())) {
                            return false;
                        }
                    }

                    if (y == 0) {
                        // Base layer: cannot replace bedrock, barrier, end portal, chests, or shulker boxes
                        Material m = b.getType();
                        if (m == Material.BEDROCK || m == Material.BARRIER || m == Material.END_PORTAL
                                || m == Material.END_PORTAL_FRAME || m == Material.CHEST || m == Material.TRAPPED_CHEST
                                || m.name().contains("SHULKER_BOX")) {
                            return false;
                        }
                    } else {
                        // Upper layers (chamber Y=1, Y=2 and roof Y=3): must be clear / air / passable
                        if (!isClearBlock(b)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isClearBlock(Block b) {
        Material mat = b.getType();
        return mat.isAir() || b.isLiquid() || mat == Material.SHORT_GRASS || mat == Material.TALL_GRASS
                || mat == Material.SNOW || mat.name().contains("FLOWER") || mat.name().contains("FERN")
                || mat == Material.VINE || mat == Material.GLOW_LICHEN || mat == Material.HANGING_ROOTS;
    }

    /**
     * Builds the Altar structure layer by layer with 1 second delay between each layer and sound effects.
     */
    public void buildStructureAnimated(Location center, int level, Runnable onComplete) {
        // Layer 0: Base (Y=0) immediately
        buildLayerBase(center, level);
        center.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        center.getWorld().playSound(center, Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.2f);
        center.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(0.5, 0.5, 0.5), 18, 0.8, 0.1, 0.8, 0.05);

        // Layer 1: Glass Lower (Y=1) after 1 second (20 ticks)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            buildGlassLevel(center, level, 1);
            center.getWorld().playSound(center, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.0f);
            center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.0f);
            center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0.5, 1.5, 0.5), 15, 0.8, 0.2, 0.8, 0.03);

            // Layer 2: Glass Upper (Y=2) after 2 seconds (40 ticks from start)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                buildGlassLevel(center, level, 2);
                center.getWorld().playSound(center, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.2f);
                center.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.3f);
                center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0.5, 2.5, 0.5), 18, 0.8, 0.2, 0.8, 0.03);

                // Layer 3: Roof Slabs (Y=3) after 3 seconds (60 ticks from start)
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    buildLayerRoof(center, level);
                    center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.4f);
                    center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
                    center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center.clone().add(0.5, 3.2, 0.5), 30, 0.8, 0.2, 0.8, 0.1);

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }, 20L);
            }, 20L);
        }, 20L);
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
            case 4 -> Particle.TOTEM_OF_UNDYING;
            case 3 -> Particle.PORTAL;
            case 2 -> Particle.END_ROD;
            default -> Particle.ENCHANT;
        };
        center.getWorld().spawnParticle(p, center.clone().add(0.5, 1.5, 0.5), 45, 1.2, 1.2, 1.2, 0.05);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.0f + (level * 0.2f));
    }

    private void buildLayerBase(Location center, int level) {
        center.getBlock().setType(Material.LODESTONE);

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
