package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetKitchen;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class KitchenStructureManager {

    private final LeftyPetPlugin plugin;

    public KitchenStructureManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the 3x3x4 footprint can accommodate the Dapur MBG structure.
     */
    public boolean canPlaceStructure(Location center) {
        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // center placement block
                    Block b = center.clone().add(x, y, z).getBlock();
                    Material mat = b.getType();
                    if (!mat.isAir() && mat != Material.WATER && mat != Material.SHORT_GRASS && mat != Material.TALL_GRASS) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Builds the 3x3x4 Dapur MBG with smooth phased block animations.
     */
    public void buildStructureAnimated(Location center, int level, Runnable onComplete) {
        plugin.getKitchenManager().setBuilding(center, true);

        // Layer 0: Kitchen Tile Floor (Y=0)
        buildLayerFloor(center, level);
        center.getWorld().playSound(center, Sound.BLOCK_STONE_PLACE, 1.0f, 0.9f);
        center.getWorld().playSound(center, Sound.BLOCK_WOOD_PLACE, 0.8f, 1.1f);
        center.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(0.5, 0.5, 0.5), 18, 0.8, 0.1, 0.8, 0.05);

        // Layer 1: Workstations (Y=1) after 10 ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            buildLayerStations(center, level);
            center.getWorld().playSound(center, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
            center.getWorld().playSound(center, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.8f, 1.0f);
            center.getWorld().spawnParticle(Particle.FLAME, center.clone().add(0.5, 1.5, 0.5), 15, 0.8, 0.2, 0.8, 0.03);

            // Layer 2: Pillars (Y=2) after 20 ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                buildLayerPillars(center, level);
                center.getWorld().playSound(center, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.2f);

                // Layer 3: Red-White Canopy Awning (Y=3) after 30 ticks
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    buildLayerCanopy(center, level);
                    center.getWorld().playSound(center, Sound.BLOCK_WOOL_PLACE, 1.0f, 1.0f);
                    center.getWorld().playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
                    center.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, center.clone().add(0.5, 3.2, 0.5), 35, 0.8, 0.2, 0.8, 0.1);

                    plugin.getKitchenManager().setBuilding(center, false);

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }, 10L);
            }, 10L);
        }, 10L);
    }

    public void buildStructure(Location center, int level) {
        buildLayerFloor(center, level);
        buildLayerStations(center, level);
        buildLayerPillars(center, level);
        buildLayerCanopy(center, level);
        center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center.clone().add(0.5, 1.5, 0.5), 30, 0.8, 0.5, 0.8, 0.05);
    }

    private void buildLayerFloor(Location center, int level) {
        // Center Controller Block: SMOKER
        center.getBlock().setType(Material.SMOKER);

        // Checkerboard restaurant kitchen tiles
        Material tileA = Material.SMOOTH_QUARTZ;
        Material tileB = Material.POLISHED_BLACKSTONE;

        center.clone().add(-1, 0, -1).getBlock().setType(tileA);
        center.clone().add(0, 0, -1).getBlock().setType(tileB);
        center.clone().add(1, 0, -1).getBlock().setType(tileA);

        center.clone().add(-1, 0, 0).getBlock().setType(tileB);
        center.clone().add(1, 0, 0).getBlock().setType(tileB);

        center.clone().add(-1, 0, 1).getBlock().setType(tileA);
        center.clone().add(0, 0, 1).getBlock().setType(tileB);
        center.clone().add(1, 0, 1).getBlock().setType(tileA);
    }

    private void buildLayerStations(Location center, int level) {
        // Station 1: Bahan & Racik (Left: -1, 1, 0)
        center.clone().add(-1, 1, 0).getBlock().setType(Material.BARREL);

        // Station 2: Kompor Memasak (Back: 0, 1, -1)
        center.clone().add(0, 1, -1).getBlock().setType(Material.SMOKER);

        // Station 3: Meja Packing & Distribusi Box (Right: 1, 1, 0)
        center.clone().add(1, 1, 0).getBlock().setType(Material.BARREL);

        // Corner counter pillars
        center.clone().add(-1, 1, -1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(1, 1, -1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(-1, 1, 1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(1, 1, 1).getBlock().setType(Material.SPRUCE_FENCE);

        // Front Entrance (0, 1, 1) and Center (0, 1, 0) are clear air for interaction & chef flight
        center.clone().add(0, 1, 1).getBlock().setType(Material.AIR);
        center.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
    }

    private void buildLayerPillars(Location center, int level) {
        center.clone().add(-1, 2, -1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(1, 2, -1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(-1, 2, 1).getBlock().setType(Material.SPRUCE_FENCE);
        center.clone().add(1, 2, 1).getBlock().setType(Material.SPRUCE_FENCE);

        // Clear air for middle viewing
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) == 1 && Math.abs(z) == 1) continue; // corners
                center.clone().add(x, 2, z).getBlock().setType(Material.AIR);
            }
        }
    }

    private void buildLayerCanopy(Location center, int level) {
        // Red-White patriotic awning stripes:
        // Z = -1 (Red)
        // Z = 0  (White)
        // Z = 1  (Red)
        for (int x = -1; x <= 1; x++) {
            center.clone().add(x, 3, -1).getBlock().setType(Material.RED_WOOL);
            center.clone().add(x, 3, 0).getBlock().setType(Material.WHITE_WOOL);
            center.clone().add(x, 3, 1).getBlock().setType(Material.RED_WOOL);
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

    public Location getStationLocation(Location center, PetKitchen.KitchenStation station) {
        return switch (station) {
            case PREPARING -> center.clone().add(-1.0 + 0.5, 1.6, 0.0 + 0.5); // At Station 1: Bahan
            case COOKING -> center.clone().add(0.0 + 0.5, 1.6, -1.0 + 0.5);   // At Station 2: Kompor
            case PACKING -> center.clone().add(1.0 + 0.5, 1.6, 0.0 + 0.5);    // At Station 3: Meja Box
        };
    }
}
