package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MountManager {

    private final LeftyPetPlugin plugin;
    private final Map<UUID, Long> doubleJumpCooldown = new ConcurrentHashMap<>();

    public MountManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canMount(PetData data) {
        int requiredLevel = plugin.getConfigManager().getMountUnlockLevel();
        return data.getLevel() >= requiredLevel;
    }

    public void startMount(Player player) {
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet == null || !pet.isValid()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cKamu harus memanggil pet terlebih dahulu!");
            return;
        }

        PetData data = pet.getData();
        if (data.isFainted()) {
            player.sendMessage(plugin.getConfigManager().getMessage("pet-fainted"));
            return;
        }

        int requiredLevel = plugin.getConfigManager().getMountUnlockLevel();
        if (data.getLevel() < requiredLevel) {
            String msg = plugin.getConfigManager().getMessage("mount-level-too-low")
                    .replace("{level}", String.valueOf(requiredLevel));
            player.sendMessage(msg);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        pet.mount();
        player.sendMessage(plugin.getConfigManager().getMessage("pet-mounted"));
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 0.7f, 1.2f);
    }

    public void stopMount(Player player) {
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet != null && pet.isMounting()) {
            pet.dismount();
            player.sendMessage(plugin.getConfigManager().getMessage("pet-dismounted"));
        }
    }

    public void handleInput(Player player, Input input) {
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet == null || !pet.isMounting()) return;

        ArmorStand seat = pet.getSeatEntity();
        if (seat == null || !seat.isValid()) return;

        // Dismount on Sneak
        if (input.isSneak()) {
            stopMount(player);
            return;
        }

        PetData data = pet.getData();
        double classMult = plugin.getConfigManager().getClassSpeedMultiplier(data.getPetClass());
        double baseSpeed = 0.38 * classMult;
        if (input.isSprint()) {
            baseSpeed *= 1.25;
        }

        Vector dir = player.getLocation().getDirection().setY(0);
        if (dir.lengthSquared() > 0.001) {
            dir.normalize();
        }

        Vector side = new Vector(-dir.getZ(), 0, dir.getX()); // Left side
        Vector moveVec = new Vector(0, 0, 0);

        if (input.isForward()) {
            moveVec.add(dir);
        }
        if (input.isBackward()) {
            moveVec.subtract(dir);
        }
        if (input.isLeft()) {
            moveVec.add(side);
        }
        if (input.isRight()) {
            moveVec.subtract(side);
        }

        if (moveVec.lengthSquared() > 0.001) {
            moveVec.normalize().multiply(baseSpeed);
        }

        // Jump / Double Jump handling
        if (input.isJump()) {
            boolean canDoubleJump = data.getLevel() >= plugin.getConfigManager().getDoubleJumpUnlockLevel();
            long now = System.currentTimeMillis();
            long lastJump = doubleJumpCooldown.getOrDefault(player.getUniqueId(), 0L);

            if (seat.isOnGround()) {
                moveVec.setY(0.48);
            } else if (canDoubleJump && (now - lastJump > 2500)) {
                // Boost Dash / Double Jump in air!
                doubleJumpCooldown.put(player.getUniqueId(), now);
                moveVec.add(player.getLocation().getDirection().multiply(0.75)).setY(0.55);
                player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 15, 0.2, 0.2, 0.2, 0.08);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.2f);
            }
        } else if (!seat.isOnGround()) {
            // Apply slight gravity so pet doesn't get stuck in air
            moveVec.setY(seat.getVelocity().getY() - 0.06);
        }

        // Apply calculated velocity
        seat.setVelocity(moveVec);
        Location newLoc = seat.getLocation();
        newLoc.setYaw(player.getLocation().getYaw());
        seat.setRotation(player.getLocation().getYaw(), 0f);
    }
}
