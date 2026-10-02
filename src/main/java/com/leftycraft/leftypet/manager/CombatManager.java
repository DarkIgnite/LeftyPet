package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class CombatManager {

    private final LeftyPetPlugin plugin;

    public CombatManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        startCombatTask();
    }

    private void startCombatTask() {
        // Runs every 25 ticks (~1.25 seconds)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (pet == null || !pet.isValid()) continue;
                if (pet.isMounting()) continue;

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
        PetData data = pet.getData();
        double classMult = plugin.getConfigManager().getClassDamageMultiplier(data.getPetClass());
        double damage = data.getAttackDamage(classMult);

        boolean isCrit = false;
        if (data.getPetClass() == PetClass.FIGHTER && ThreadLocalRandom.current().nextInt(100) < 30) {
            damage *= 1.5;
            isCrit = true;
        }

        Location start = pet.getDisplayEntity().getLocation().add(0, 0.2, 0);
        Location end = target.getEyeLocation();

        // Spawn magic beam particles
        spawnBeam(start, end, data.getPetClass(), isCrit);

        // Apply damage attributed to the owner player
        target.damage(damage, player);

        // Play combat sound
        player.getWorld().playSound(start, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1.6f);
        if (isCrit) {
            player.getWorld().playSound(end, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.7f, 1.2f);
        }

        // Drain energy
        data.drainEnergy(plugin.getConfigManager().getEnergyDrainPerAttack());
        pet.updateNameTag();

        if (data.isFainted()) {
            ColorUtil.sendMessage(player, plugin.getConfigManager().getMessage("pet-fainted"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 0.8f);
        }
    }

    private void spawnBeam(Location start, Location end, PetClass petClass, boolean isCrit) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        dir.normalize();

        Particle p = switch (petClass) {
            case FIGHTER -> isCrit ? Particle.SOUL_FIRE_FLAME : Particle.FLAME;
            case SUPPORT -> Particle.HEART;
            case LOOTER -> Particle.HAPPY_VILLAGER;
            case TRAVELER -> Particle.SONIC_BOOM;
        };

        for (double d = 0; d < length; d += 0.4) {
            Location point = start.clone().add(dir.clone().multiply(d));
            point.getWorld().spawnParticle(p, point, 1, 0, 0, 0, 0);
            if (isCrit) {
                point.getWorld().spawnParticle(Particle.CRIT, point, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }
}
