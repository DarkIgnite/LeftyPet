package com.leftycraft.leftypet.hook;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.util.SchematicLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles region & claim protection hooks for RedProtect and WorldGuard.
 * Prevents players from placing Altars and Dapur MBG (Kitchens) inside claims/regions belonging to others.
 */
public class ProtectionHookManager {

    public record ProtectionResult(boolean allowed, String pluginName, String claimInfo) {
        public static ProtectionResult ok() {
            return new ProtectionResult(true, null, null);
        }

        public static ProtectionResult denied(String pluginName, String claimInfo) {
            return new ProtectionResult(false, pluginName, claimInfo != null ? claimInfo : "Region");
        }
    }

    private final LeftyPetPlugin plugin;

    // RedProtect
    private boolean redProtectHooked = false;
    private Object redProtectApiInstance = null;
    private Method rpGetRegionMethod = null;
    private Method rpCanBuildMethod = null;
    private Method rpGetNameMethod = null;
    private Method rpGetLeadersStringMethod = null;

    // WorldGuard
    private boolean worldGuardHooked = false;
    private Object worldGuardProtectionQuery = null;
    private Method wgTestBlockPlaceMethod = null;

    public ProtectionHookManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        initHooks();
    }

    public void initHooks() {
        initRedProtect();
        initWorldGuard();
    }

    private void initRedProtect() {
        if (!Bukkit.getPluginManager().isPluginEnabled("RedProtect")) {
            redProtectHooked = false;
            return;
        }
        try {
            Class<?> rpClass = Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect");
            Method getMethod = rpClass.getMethod("get");
            Object rpInstance = getMethod.invoke(null);
            Method getApiMethod = rpInstance.getClass().getMethod("getAPI");
            this.redProtectApiInstance = getApiMethod.invoke(rpInstance);

            Class<?> apiClass = redProtectApiInstance.getClass();
            this.rpGetRegionMethod = apiClass.getMethod("getRegion", Location.class);

            Class<?> regionClass = Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.Region");
            this.rpCanBuildMethod = regionClass.getMethod("canBuild", Player.class);
            try {
                this.rpGetNameMethod = regionClass.getMethod("getName");
                this.rpGetLeadersStringMethod = regionClass.getMethod("getLeadersString");
            } catch (Throwable ignored) {}

            this.redProtectHooked = true;
            plugin.getLogger().info("Successfully hooked into RedProtect for claim protection checks!");
        } catch (Throwable t) {
            this.redProtectHooked = false;
            plugin.getLogger().warning("Failed to initialize RedProtect hook: " + t.getMessage());
        }
    }

    private void initWorldGuard() {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardHooked = false;
            return;
        }
        try {
            Class<?> wgPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            Method instMethod = wgPluginClass.getMethod("inst");
            Object wgInst = instMethod.invoke(null);
            Method createProtectionQueryMethod = wgInst.getClass().getMethod("createProtectionQuery");
            this.worldGuardProtectionQuery = createProtectionQueryMethod.invoke(wgInst);

            Class<?> pqClass = worldGuardProtectionQuery.getClass();
            this.wgTestBlockPlaceMethod = pqClass.getMethod("testBlockPlace", Object.class, Location.class, Material.class);

            this.worldGuardHooked = true;
            plugin.getLogger().info("Successfully hooked into WorldGuard for region protection checks!");
        } catch (Throwable t) {
            this.worldGuardHooked = false;
            plugin.getLogger().warning("Failed to initialize WorldGuard hook: " + t.getMessage());
        }
    }

    public boolean isRedProtectHooked() {
        return redProtectHooked;
    }

    public boolean isWorldGuardHooked() {
        return worldGuardHooked;
    }

    /**
     * Checks if a player can build at a single location according to RedProtect and WorldGuard.
     */
    public ProtectionResult canBuildAt(Player player, Location loc) {
        if (player == null || loc == null) return ProtectionResult.ok();
        if (player.isOp() || player.hasPermission("leftypet.admin")) {
            return ProtectionResult.ok();
        }

        // 1. RedProtect check
        ProtectionResult rp = checkRedProtect(player, loc);
        if (!rp.allowed()) {
            return rp;
        }

        // 2. WorldGuard check
        ProtectionResult wg = checkWorldGuard(player, loc);
        if (!wg.allowed()) {
            return wg;
        }

        return ProtectionResult.ok();
    }

    private ProtectionResult checkRedProtect(Player player, Location loc) {
        if (!redProtectHooked || redProtectApiInstance == null || rpGetRegionMethod == null || rpCanBuildMethod == null) {
            return ProtectionResult.ok();
        }
        try {
            Object region = rpGetRegionMethod.invoke(redProtectApiInstance, loc);
            if (region != null) {
                boolean canBuild = (boolean) rpCanBuildMethod.invoke(region, player);
                if (!canBuild) {
                    String name = "Claim";
                    if (rpGetNameMethod != null) {
                        try {
                            Object n = rpGetNameMethod.invoke(region);
                            if (n != null) name = n.toString();
                        } catch (Throwable ignored) {}
                    }
                    String leaders = "";
                    if (rpGetLeadersStringMethod != null) {
                        try {
                            Object l = rpGetLeadersStringMethod.invoke(region);
                            if (l != null && !l.toString().isEmpty()) leaders = " (" + l + ")";
                        } catch (Throwable ignored) {}
                    }
                    return ProtectionResult.denied("RedProtect", name + leaders);
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().fine("RedProtect check failed: " + t.getMessage());
        }
        return ProtectionResult.ok();
    }

    private ProtectionResult checkWorldGuard(Player player, Location loc) {
        if (!worldGuardHooked || worldGuardProtectionQuery == null || wgTestBlockPlaceMethod == null) {
            return ProtectionResult.ok();
        }
        try {
            boolean allowed = (boolean) wgTestBlockPlaceMethod.invoke(worldGuardProtectionQuery, player, loc, Material.STONE);
            if (!allowed) {
                String regionName = getWGRegionName(loc);
                return ProtectionResult.denied("WorldGuard", regionName != null ? regionName : "Region");
            }
        } catch (Throwable t) {
            plugin.getLogger().fine("WorldGuard check failed: " + t.getMessage());
        }
        return ProtectionResult.ok();
    }

    private String getWGRegionName(Location loc) {
        try {
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstance = wgClass.getMethod("getInstance");
            Object wgInstance = getInstance.invoke(null);
            Method getPlatform = wgInstance.getClass().getMethod("getPlatform");
            Object platform = getPlatform.invoke(wgInstance);
            Method getRegionContainer = platform.getClass().getMethod("getRegionContainer");
            Object container = getRegionContainer.invoke(platform);
            Method createQuery = container.getClass().getMethod("createQuery");
            Object query = createQuery.invoke(container);

            Class<?> baClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Method adaptLoc = baClass.getMethod("adapt", Location.class);
            Object weLoc = adaptLoc.invoke(null, loc);

            Method getApplicable = query.getClass().getMethod("getApplicableRegions", weLoc.getClass());
            Object regionSet = getApplicable.invoke(query, weLoc);

            Method getRegions = regionSet.getClass().getMethod("getRegions");
            java.util.Set<?> regions = (java.util.Set<?>) getRegions.invoke(regionSet);
            StringBuilder sb = new StringBuilder();
            for (Object r : regions) {
                Method getId = r.getClass().getMethod("getId");
                String id = (String) getId.invoke(r);
                if (!"__global__".equalsIgnoreCase(id)) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(id);
                }
            }
            if (!sb.isEmpty()) {
                return sb.toString();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Checks if all blocks in an Altar structure (3x3x4) are allowed to be built by the player.
     */
    public ProtectionResult canPlaceAltar(Player player, Location center) {
        if (player == null || center == null) return ProtectionResult.ok();
        if (player.isOp() || player.hasPermission("leftypet.admin")) return ProtectionResult.ok();

        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location loc = center.clone().add(x, y, z);
                    ProtectionResult res = canBuildAt(player, loc);
                    if (!res.allowed()) {
                        return res;
                    }
                }
            }
        }
        return ProtectionResult.ok();
    }

    /**
     * Checks if all key points and footprint blocks of a Dapur MBG (Kitchen) structure are allowed to be built by the player.
     */
    public ProtectionResult canPlaceKitchen(Player player, Location origin, StructureRotation rotation, SchematicLoader.Schematic schem) {
        if (player == null || origin == null) return ProtectionResult.ok();
        if (player.isOp() || player.hasPermission("leftypet.admin")) return ProtectionResult.ok();
        if (schem == null) return ProtectionResult.ok();

        // 1. Check origin first
        ProtectionResult originRes = canBuildAt(player, origin);
        if (!originRes.allowed()) {
            return originRes;
        }

        int width = schem.getWidth();
        int height = schem.getHeight();
        int length = schem.getLength();

        // 2. Check RedProtect for all floor columns in the footprint (fast in-memory)
        if (redProtectHooked) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    Location worldLoc = schem.transformBlockLocation(origin, x, 0, z, rotation);
                    ProtectionResult rpRes = checkRedProtect(player, worldLoc);
                    if (!rpRes.allowed()) {
                        return rpRes;
                    }
                }
            }
        }

        // 3. Check WorldGuard across corners, perimeter, and center grid points
        if (worldGuardHooked) {
            Set<Location> wgCheckPoints = new HashSet<>();

            // 8 Corners of the cuboid
            wgCheckPoints.add(schem.transformBlockLocation(origin, 0, 0, 0, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width - 1, 0, 0, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, 0, 0, length - 1, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width - 1, 0, length - 1, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, 0, height - 1, 0, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width - 1, height - 1, 0, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, 0, height - 1, length - 1, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width - 1, height - 1, length - 1, rotation));

            // Center points
            wgCheckPoints.add(schem.transformBlockLocation(origin, width / 2, 0, length / 2, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width / 4, 0, length / 4, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, (3 * width) / 4, 0, length / 4, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, width / 4, 0, (3 * length) / 4, rotation));
            wgCheckPoints.add(schem.transformBlockLocation(origin, (3 * width) / 4, 0, (3 * length) / 4, rotation));

            // Perimeter steps along X
            for (int x = 2; x < width - 1; x += 2) {
                wgCheckPoints.add(schem.transformBlockLocation(origin, x, 0, 0, rotation));
                wgCheckPoints.add(schem.transformBlockLocation(origin, x, 0, length - 1, rotation));
            }

            // Perimeter steps along Z
            for (int z = 2; z < length - 1; z += 2) {
                wgCheckPoints.add(schem.transformBlockLocation(origin, 0, 0, z, rotation));
                wgCheckPoints.add(schem.transformBlockLocation(origin, width - 1, 0, z, rotation));
            }

            for (Location loc : wgCheckPoints) {
                ProtectionResult wgRes = checkWorldGuard(player, loc);
                if (!wgRes.allowed()) {
                    return wgRes;
                }
            }
        }

        return ProtectionResult.ok();
    }
}
