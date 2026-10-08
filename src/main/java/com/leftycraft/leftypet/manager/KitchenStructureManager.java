package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetKitchen;
import com.leftycraft.leftypet.util.SchematicLoader;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.structure.StructureRotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class KitchenStructureManager {

    private final LeftyPetPlugin plugin;
    private SchematicLoader.Schematic cachedSchematic;

    // Anchor at entrance in schematic: x=11, y=0, z=6
    public static final int ANCHOR_X = 11;
    public static final int ANCHOR_Y = 0;
    public static final int ANCHOR_Z = 6;

    // Base Station Coordinates (in WEST orientation)
    public static final double COOKING_SX = 4.5;
    public static final double COOKING_SY = 2.25;
    public static final double COOKING_SZ = 9.0;

    public static final double PACKING_SX = 3.0;
    public static final double PACKING_SY = 2.25;
    public static final double PACKING_SZ = 4.0;

    public static final double DELIVERY_SX = 7.5;
    public static final double DELIVERY_SY = 2.40;
    public static final double DELIVERY_SZ = 2.0;

    public static final double CASHIER_SX = 6.0;
    public static final double CASHIER_SY = 2.30;
    public static final double CASHIER_SZ = 6.0;

    public KitchenStructureManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        loadSchematic();
    }

    public void loadSchematic() {
        try {
            File customSchem = new File(plugin.getDataFolder(), "schematics/Kitchen.schem");
            InputStream is;
            if (customSchem.exists()) {
                is = new FileInputStream(customSchem);
            } else {
                is = plugin.getResource("schematics/Kitchen.schem");
            }

            if (is == null) {
                plugin.getLogger().warning("Could not find Kitchen.schem resource or file!");
                return;
            }

            this.cachedSchematic = SchematicLoader.loadFromStream(is, ANCHOR_X, ANCHOR_Y, ANCHOR_Z);
            plugin.getLogger().info("Successfully loaded Kitchen.schem with " + cachedSchematic.getBlocks().size() + " blocks.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load Kitchen.schem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SchematicLoader.Schematic getSchematic() {
        if (cachedSchematic == null) {
            loadSchematic();
        }
        return cachedSchematic;
    }

    /**
     * Determines StructureRotation and cardinal facing name from player yaw.
     */
    public StructureRotation getRotationFromYaw(float yaw) {
        float normalized = (yaw % 360 + 360) % 360;
        // In unrotated schematic, looking into door is looking towards -X (WEST).
        if (normalized >= 45 && normalized < 135) {
            return StructureRotation.NONE; // Looking WEST
        } else if (normalized >= 135 && normalized < 225) {
            return StructureRotation.CLOCKWISE_90; // Looking NORTH
        } else if (normalized >= 225 && normalized < 315) {
            return StructureRotation.CLOCKWISE_180; // Looking EAST
        } else {
            return StructureRotation.COUNTERCLOCKWISE_90; // Looking SOUTH
        }
    }

    public String getFacingFromRotation(StructureRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> "NORTH";
            case CLOCKWISE_180 -> "EAST";
            case COUNTERCLOCKWISE_90 -> "SOUTH";
            default -> "WEST";
        };
    }

    public Location getStationLocation(Location origin, PetKitchen.KitchenStation station, StructureRotation rotation) {
        SchematicLoader.Schematic schem = getSchematic();
        if (schem == null || origin == null) return origin;

        return switch (station) {
            case COOKING -> schem.transformLocation(origin, COOKING_SX, COOKING_SY, COOKING_SZ, rotation);
            case PACKING -> schem.transformLocation(origin, PACKING_SX, PACKING_SY, PACKING_SZ, rotation);
            case DELIVERY -> schem.transformLocation(origin, DELIVERY_SX, DELIVERY_SY, DELIVERY_SZ, rotation);
            case TIRED -> schem.transformLocation(origin, PACKING_SX, PACKING_SY + 0.5, PACKING_SZ, rotation);
        };
    }

    public Location getCashierHologramLocation(Location origin, StructureRotation rotation) {
        SchematicLoader.Schematic schem = getSchematic();
        if (schem == null || origin == null) return origin;
        return schem.transformLocation(origin, CASHIER_SX, CASHIER_SY, CASHIER_SZ, rotation);
    }

    public boolean canPlaceKitchen(Location origin, StructureRotation rotation) {
        SchematicLoader.Schematic schem = getSchematic();
        if (schem == null || origin == null || origin.getWorld() == null) return false;

        for (SchematicLoader.SchematicBlock sb : schem.getBlocks()) {
            Location worldLoc = schem.transformLocation(origin, sb.x(), sb.y(), sb.z(), rotation);

            // Check if collides with another kitchen or altar
            if (plugin.getKitchenManager().isKitchenAreaOrBuilding(worldLoc) ||
                plugin.getAltarManager().isAltarAreaOrBuilding(worldLoc)) {
                return false;
            }

            // For above ground space (y >= 1), ensure it's empty
            if (sb.y() >= 1) {
                Block b = worldLoc.getBlock();
                Material mat = b.getType();
                if (!mat.isAir() && mat != Material.CAVE_AIR && mat != Material.VOID_AIR
                        && mat != Material.SHORT_GRASS && mat != Material.TALL_GRASS
                        && mat != Material.SNOW && !mat.name().contains("FLOWER")
                        && mat != Material.DEAD_BUSH && mat != Material.FERN) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Pastes the kitchen building into the world. Returns the set of all world block locations placed.
     */
    public Set<Location> buildKitchen(Location origin, StructureRotation rotation) {
        SchematicLoader.Schematic schem = getSchematic();
        Set<Location> placedLocs = new HashSet<>();
        if (schem == null || origin == null || origin.getWorld() == null) return placedLocs;

        World world = origin.getWorld();
        List<SchematicLoader.SchematicBlock> allBlocks = schem.getBlocks();

        List<BlockPlacement> pass1 = new ArrayList<>();
        List<BlockPlacement> pass2 = new ArrayList<>();

        for (SchematicLoader.SchematicBlock sb : allBlocks) {
            Location worldLoc = schem.transformLocation(origin, sb.x(), sb.y(), sb.z(), rotation);
            placedLocs.add(worldLoc.getBlock().getLocation());

            try {
                BlockData bd = Bukkit.createBlockData(sb.blockDataString());
                if (rotation != StructureRotation.NONE) {
                    bd.rotate(rotation);
                }

                if (isAttachableOrUpper(bd)) {
                    pass2.add(new BlockPlacement(worldLoc, bd));
                } else {
                    pass1.add(new BlockPlacement(worldLoc, bd));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create block data for: " + sb.blockDataString());
            }
        }

        // Pass 1: Solid & Base blocks
        for (BlockPlacement bp : pass1) {
            bp.loc.getBlock().setBlockData(bp.data, false);
        }

        // Pass 2: Attachable, decorations, and upper halves
        for (BlockPlacement bp : pass2) {
            bp.loc.getBlock().setBlockData(bp.data, false);
        }

        // FX
        world.playSound(origin, Sound.BLOCK_ANVIL_USE, 0.8f, 1.2f);
        world.playSound(origin, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
        world.spawnParticle(Particle.POOF, origin.clone().add(0, 1, 0), 30, 1.5, 1.0, 1.5, 0.05);

        return placedLocs;
    }

    private boolean isAttachableOrUpper(BlockData data) {
        Material mat = data.getMaterial();
        if (data instanceof Bisected bisected && bisected.getHalf() == Bisected.Half.TOP) {
            return true;
        }
        String name = mat.name();
        return name.contains("LANTERN") || name.contains("BANNER") || name.contains("LEVER")
                || name.contains("TRAPDOOR") || name.contains("DOOR") || name.contains("PRESSURE_PLATE")
                || name.contains("POTTED") || name.contains("PEONY") || name.contains("ROSE_BUSH")
                || name.contains("LILAC");
    }

    /**
     * Removes all blocks that were part of the kitchen.
     */
    public void removeKitchen(Collection<Location> blockLocations) {
        if (blockLocations == null) return;
        for (Location loc : blockLocations) {
            if (loc != null && loc.getWorld() != null) {
                loc.getBlock().setType(Material.AIR, false);
            }
        }
    }

    private record BlockPlacement(Location loc, BlockData data) {}
}
