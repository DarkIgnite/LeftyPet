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
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

public class AltarStructureManager {

    private final LeftyPetPlugin plugin;

    public AltarStructureManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the 3x3x4 volume around the center lodestone is clear of solid obstacles.
     */
    public boolean canPlaceStructure(Location center) {
        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block b = center.clone().add(x, y, z).getBlock();
                    if (!isClearBlock(b)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isClearBlock(Block b) {
        Material mat = b.getType();
        return mat.isAir() || b.isLiquid() || mat == Material.SHORT_GRASS || mat == Material.TALL_GRASS
                || mat == Material.SNOW || mat.name().contains("FLOWER") || mat.name().contains("FERN");
    }

    /**
     * Builds the Altar structure with animation layer by layer.
     */
    public void buildStructureAnimated(Location center, int level, Runnable onComplete) {
        // Phase 1: Base (Y=0)
        buildLayerBase(center, level);
        center.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 0.9f, 0.8f);
        center.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(0.5, 0.5, 0.5), 15, 0.8, 0.1, 0.8, 0.05);

        // Phase 2: Glass Walls (Y=1 & Y=2) after 4 ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            buildLayerGlass(center, level);
            center.getWorld().playSound(center, Sound.BLOCK_GLASS_PLACE, 0.9f, 1.2f);
            center.getWorld().spawnParticle(Particle.END_ROD, center.clone().add(0.5, 1.5, 0.5), 15, 0.8, 0.5, 0.8, 0.02);

            // Phase 3: Roof Slabs (Y=3) after another 4 ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                buildLayerRoof(center, level);
                center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.9f, 1.3f);
                center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center.clone().add(0.5, 3.2, 0.5), 25, 0.8, 0.1, 0.8, 0.1);
                if (onComplete != null) {
                    onComplete.run();
                }
            }, 4L);
        }, 4L);
    }

    /**
     * Instant build or transformation of the 3x3x4 Altar structure.
     */
    public void buildStructure(Location center, int level) {
        buildLayerBase(center, level);
        buildLayerGlass(center, level);
        buildLayerRoof(center, level);

        Particle p = (level == 3) ? Particle.PORTAL : (level == 2 ? Particle.END_ROD : Particle.ENCHANT);
        center.getWorld().spawnParticle(p, center.clone().add(0.5, 1.5, 0.5), 45, 1.2, 1.2, 1.2, 0.05);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.0f + (level * 0.2f));
    }

    private void buildLayerBase(Location center, int level) {
        Material cornerMat = switch (level) {
            case 2 -> Material.END_STONE_BRICKS;
            case 3 -> Material.PURPUR_PILLAR;
            default -> Material.CHISELED_STONE_BRICKS;
        };

        Material stairsMat = switch (level) {
            case 2 -> Material.END_STONE_BRICK_STAIRS;
            case 3 -> Material.PURPUR_STAIRS;
            default -> Material.STONE_STAIRS;
        };

        center.getBlock().setType(Material.LODESTONE);

        setCorner(center.clone().add(-1, 0, -1).getBlock(), cornerMat);
        setCorner(center.clone().add(1, 0, -1).getBlock(), cornerMat);
        setCorner(center.clone().add(-1, 0, 1).getBlock(), cornerMat);
        setCorner(center.clone().add(1, 0, 1).getBlock(), cornerMat);

        setStairs(center.clone().add(0, 0, -1).getBlock(), stairsMat, BlockFace.NORTH);
        setStairs(center.clone().add(0, 0, 1).getBlock(), stairsMat, BlockFace.SOUTH);
        setStairs(center.clone().add(1, 0, 0).getBlock(), stairsMat, BlockFace.EAST);
        setStairs(center.clone().add(-1, 0, 0).getBlock(), stairsMat, BlockFace.WEST);
    }

    private void buildLayerGlass(Location center, int level) {
        Material glassMat = switch (level) {
            case 2 -> Material.LIGHT_BLUE_STAINED_GLASS;
            case 3 -> Material.MAGENTA_STAINED_GLASS;
            default -> Material.WHITE_STAINED_GLASS;
        };

        for (int y = 1; y <= 2; y++) {
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
    }

    private void buildLayerRoof(Location center, int level) {
        Material slabMat = switch (level) {
            case 2 -> Material.END_STONE_BRICK_SLAB;
            case 3 -> Material.PURPUR_SLAB;
            default -> Material.POLISHED_DIORITE_SLAB;
        };

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block b = center.clone().add(x, 3, z).getBlock();
                setBottomSlab(b, slabMat);
            }
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
