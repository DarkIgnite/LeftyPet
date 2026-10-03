package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CombatManager {

    private final LeftyPetPlugin plugin;
    private boolean isProcessingAttack = false;
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, Integer> consecutiveHits = new HashMap<>();
    private final Map<UUID, Long> shatteredEntities = new ConcurrentHashMap<>();

    public CombatManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        startCombatTask();
    }

    public boolean isProcessingAttack() {
        return isProcessingAttack;
    }

    public boolean isEntityShattered(LivingEntity entity) {
        if (entity == null) return false;
        Long until = shatteredEntities.get(entity.getUniqueId());
        return until != null && System.currentTimeMillis() < until;
    }

    private void startCombatTask() {
        // Runs every 25 ticks (~1.25 seconds) to scan and auto-target hostile mobs
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (pet == null || !pet.isValid()) continue;

                PetData data = pet.getData();
                if (data.isFainted() || data.getEnergy() < 2.0) continue;

                LivingEntity target = findTarget(player, pet);
                if (target != null) {
                    performAttack(player, pet, target);
                }
            }
        }, 20L, 25L);
    }

    private LivingEntity findTarget(Player player, ActivePet pet) {
        Location loc = pet.getDisplayEntity().getLocation();
        Collection<LivingEntity> nearby = loc.getNearbyLivingEntities(9.0);

        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : nearby) {
            if (entity.equals(player)) continue;
            if (entity instanceof Player || entity instanceof Villager || entity instanceof ArmorStand || entity instanceof Display) continue;
            if (entity.hasMetadata("NPC") || entity.hasMetadata("shopkeeper")) continue;
            if (entity instanceof Tameable tameable && tameable.isTamed()) continue;
            if (entity.isDead() || !entity.isValid()) continue;

            // Target hostile mobs
            if (entity instanceof Monster) {
                // If mob is directly attacking owner, give highest priority
                if (entity instanceof Mob mob && mob.getTarget() != null && mob.getTarget().equals(player)) {
                    return mob;
                }
                double dist = entity.getLocation().distanceSquared(loc);
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
            }
        }
        return bestTarget;
    }

    public void performAttack(Player player, ActivePet pet, LivingEntity target) {
        if (target == null || target.isDead() || !target.isValid()) return;
        if (isProcessingAttack) return;

        // Rate limit pet attacks (at most once per 800ms)
        long now = System.currentTimeMillis();
        long last = lastAttackTime.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 800L) {
            return;
        }
        lastAttackTime.put(player.getUniqueId(), now);

        PetData data = pet.getData();
        double baseDrain = plugin.getConfigManager().getEnergyDrainPerAttack();

        // Skill VII: Overdrive (Lv 70+) reduces energy consumption by 50%
        if (data.getLevel() >= 70) {
            baseDrain *= 0.5;
        }

        if (data.isFainted() || data.getEnergy() < baseDrain) {
            return;
        }

        double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
        double damage = data.getAttackDamage(classMult);

        boolean isCrit = false;
        if (data.getPetClass() == PetClass.FIGHTER && ThreadLocalRandom.current().nextInt(100) < 30) {
            damage *= 1.5;
            isCrit = true;
        }

        Location start = pet.getDisplayEntity().getLocation().add(0, 0.2, 0);
        Location end = target.getEyeLocation();

        // 1. Spawn Tier-Specific Beam Particles (Requirement 2)
        spawnTierBeam(start, end, data.getLevel(), isCrit);

        // 2. Skill IX: Spectral Clone (Lv 90+) - fires a twin spirit beam from opposite side
        if (data.getLevel() >= 90) {
            Vector dir = end.toVector().subtract(start.toVector()).normalize();
            Vector side = new Vector(-dir.getZ(), 0, dir.getX());
            Location spiritStart = start.clone().add(side.multiply(1.5));
            spawnTierBeam(spiritStart, end, data.getLevel(), isCrit);
        }

        // Apply damage attributed to the owner player (guarded against recursion)
        try {
            isProcessingAttack = true;
            target.damage(damage, player);
        } finally {
            isProcessingAttack = false;
        }

        // 3. Spawn Tier-Specific Impact Particles (Requirement 2)
        spawnTierImpact(end, data.getLevel(), isCrit);

        // Play combat sound
        player.getWorld().playSound(start, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1.6f);
        com.leftycraft.leftypet.util.PetSoundUtil.playCombatSound(start, data.getSkinKey());
        if (isCrit) {
            player.getWorld().playSound(end, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
        }

        // 4. Skill I: Swift Strike (Lv 10+) - 25% chance for instant double hit
        if (data.getLevel() >= 10 && ThreadLocalRandom.current().nextInt(100) < 25) {
            final double followUpDamage = damage * 0.75;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (target.isValid() && !target.isDead()) {
                    try {
                        isProcessingAttack = true;
                        target.damage(followUpDamage, player);
                        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.4f);
                        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 0.5, 0), 1);
                    } finally {
                        isProcessingAttack = false;
                    }
                }
            }, 2L);
        }

        // 5. Skill II: Vampiric Link (Lv 20+) - 15% life steal to owner
        if (data.getLevel() >= 20) {
            double heal = damage * 0.15;
            AttributeInstance maxAttr = player.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = (maxAttr != null) ? maxAttr.getValue() : 20.0;
            if (player.getHealth() < maxHp) {
                player.setHealth(Math.min(maxHp, player.getHealth() + heal));
                player.getWorld().spawnParticle(Particle.HEART, player.getEyeLocation().add(0, 0.3, 0), 2, 0.2, 0.2, 0.2, 0.05);
            } else if (player.getFoodLevel() < 20 && ThreadLocalRandom.current().nextInt(100) < 30) {
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
            }
        }

        // 6. Skill III: Arcane Chain (Lv 30+) - splash damage up to 2 other nearby entities
        if (data.getLevel() >= 30) {
            int chained = 0;
            for (LivingEntity nearby : target.getLocation().getNearbyLivingEntities(5.0)) {
                if (nearby.equals(target) || nearby.equals(player)) continue;
                if (nearby instanceof Player || nearby instanceof Villager || nearby instanceof ArmorStand || nearby instanceof Display) continue;
                if (nearby instanceof Tameable t && t.isTamed()) continue;
                if (nearby.isDead() || !nearby.isValid()) continue;

                spawnChainLine(target.getEyeLocation(), nearby.getEyeLocation());
                try {
                    isProcessingAttack = true;
                    nearby.damage(damage * 0.50, player);
                } finally {
                    isProcessingAttack = false;
                }
                chained++;
                if (chained >= 2) break;
            }
        }

        // 7. Skill V: Armor Shatter (Lv 50+) - vulnerability, slowness, and weakness
        if (data.getLevel() >= 50) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0, false, false, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false, true));
            shatteredEntities.put(target.getUniqueId(), System.currentTimeMillis() + 4000L);
            target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, Material.ANVIL.createBlockData());
        }

        // 8. Skill VIII: Celestial Smite (Lv 80+) - holy lightning every 5 consecutive hits
        if (data.getLevel() >= 80) {
            int hits = consecutiveHits.getOrDefault(player.getUniqueId(), 0) + 1;
            consecutiveHits.put(player.getUniqueId(), hits);
            if (hits % 5 == 0) {
                target.getWorld().strikeLightningEffect(target.getLocation());
                try {
                    isProcessingAttack = true;
                    target.damage(15.0, player);
                } finally {
                    isProcessingAttack = false;
                }
                target.getWorld().spawnParticle(Particle.FLASH, target.getLocation(), 1);
                target.getWorld().spawnParticle(Particle.FIREWORK, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
                player.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
            }
        }

        // Drain energy
        data.drainEnergy(baseDrain);
        pet.updateNameTag();

        if (data.isFainted()) {
            pet.faint();
        }
    }

    /**
     * Spawns distinct magic beam particles corresponding to the pet's 10-level tier (Requirement 2).
     */
    private void spawnTierBeam(Location start, Location end, int level, boolean isCrit) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        dir.normalize();

        int tier = Math.min(10, Math.max(1, (level - 1) / 10 + 1));
        Particle beamParticle = switch (tier) {
            case 1 -> Particle.CRIT;
            case 2 -> Particle.FLAME;
            case 3 -> Particle.SOUL_FIRE_FLAME;
            case 4 -> Particle.ELECTRIC_SPARK;
            case 5 -> Particle.DRAGON_BREATH;
            case 6 -> Particle.TRIAL_OMEN;
            case 7 -> Particle.HAPPY_VILLAGER;
            case 8 -> Particle.END_ROD;
            case 9 -> Particle.FIREWORK;
            default -> Particle.REVERSE_PORTAL;
        };

        for (double d = 0; d < length; d += 0.4) {
            Location point = start.clone().add(dir.clone().multiply(d));
            point.getWorld().spawnParticle(beamParticle, point, 1, 0, 0, 0, 0);
            if (isCrit) {
                point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    /**
     * Spawns distinct impact particles at the target corresponding to the pet's 10-level tier (Requirement 2).
     */
    private void spawnTierImpact(Location loc, int level, boolean isCrit) {
        int tier = Math.min(10, Math.max(1, (level - 1) / 10 + 1));
        switch (tier) {
            case 1 -> {
                loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1);
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 4, 0.2, 0.2, 0.2, 0.1);
            }
            case 2 -> {
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, 0.1, 0.1, 0.1, 0);
                loc.getWorld().spawnParticle(Particle.SMOKE, loc, 4, 0.2, 0.2, 0.2, 0.05);
            }
            case 3 -> {
                loc.getWorld().spawnParticle(Particle.WITCH, loc, 5, 0.2, 0.2, 0.2, 0.05);
                loc.getWorld().spawnParticle(Particle.SOUL, loc, 2, 0.1, 0.1, 0.1, 0.02);
            }
            case 4 -> {
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 8, 0.3, 0.3, 0.3, 0.1);
                loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 4, 0.2, 0.2, 0.2, 0.05);
            }
            case 5 -> {
                loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 6, 0.3, 0.3, 0.3, 0.05);
                loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 8, 0.3, 0.3, 0.3, 0.1);
            }
            case 6 -> {
                loc.getWorld().spawnParticle(Particle.TRIAL_OMEN, loc, 4, 0.2, 0.2, 0.2, 0.05);
                loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 3, 0.2, 0.2, 0.2, 0.05);
            }
            case 7 -> {
                loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 8, 0.3, 0.3, 0.3, 0.1);
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 4, 0.2, 0.2, 0.2, 0.05);
            }
            case 8 -> {
                loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 6, 0.2, 0.2, 0.2, 0.08);
            }
            case 9 -> {
                loc.getWorld().spawnParticle(Particle.FLASH, loc, 1);
                loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 12, 0.3, 0.3, 0.3, 0.1);
            }
            default -> { // Tier 10 (Lv 90 - 100)
                loc.getWorld().spawnParticle(Particle.CHERRY_LEAVES, loc, 12, 0.4, 0.4, 0.4, 0.05);
                loc.getWorld().spawnParticle(Particle.GLOW, loc, 8, 0.3, 0.3, 0.3, 0.05);
                loc.getWorld().spawnParticle(Particle.FLASH, loc, 1);
            }
        }
    }

    private void spawnChainLine(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        double len = dir.length();
        dir.normalize();
        for (double d = 0; d < len; d += 0.5) {
            Location pt = start.clone().add(dir.clone().multiply(d));
            pt.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, pt, 1, 0, 0, 0, 0);
        }
    }
}
