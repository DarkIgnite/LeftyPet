package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MountManager {

    private final LeftyPetPlugin plugin;
    private final Set<UUID> mountedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Input> playerInputs = new ConcurrentHashMap<>();
    private final Map<UUID, Long> doubleJumpCooldown = new ConcurrentHashMap<>();

    public MountManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
        startMountTickTask();
    }

    public boolean canMount(PetData data) {
        int requiredLevel = plugin.getConfigManager().getMountUnlockLevel();
        return data.getLevel() >= requiredLevel;
    }

    public void startMount(Player player) {
        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet == null || !pet.isValid()) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>ᴘᴀɴɢɢɪʟ ᴘᴇᴛ ᴛᴇʀʟᴇʙɪʜ ᴅᴀʜᴜʟᴜ!</red>"));
            return;
        }

        PetData data = pet.getData();
        if (data.isFainted()) {
            player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-fainted")));
            return;
        }

        int requiredLevel = plugin.getConfigManager().getMountUnlockLevel();
        if (data.getLevel() < requiredLevel) {
            String msg = plugin.getConfigManager().getMessage("mount-level-too-low")
                    .replace("{level}", String.valueOf(requiredLevel));
            player.sendMessage(ColorUtil.component(msg));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
            return;
        }

        pet.mount();
        mountedPlayers.add(player.getUniqueId());
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-mounted")));
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 0.7f, 1.2f);
    }

    public void stopMount(Player player) {
        UUID uuid = player.getUniqueId();
        mountedPlayers.remove(uuid);
        playerInputs.remove(uuid);

        ActivePet pet = plugin.getPetManager().getActivePet(uuid);
        if (pet != null) {
            pet.dismount();
        }
        player.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("pet-dismounted")));
    }

    public void handleInput(Player player, Input input) {
        if (!mountedPlayers.contains(player.getUniqueId())) return;

        if (input.isSneak()) {
            stopMount(player);
            return;
        }

        playerInputs.put(player.getUniqueId(), input);
    }

    private void startMountTickTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : mountedPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    mountedPlayers.remove(uuid);
                    playerInputs.remove(uuid);
                    continue;
                }

                ActivePet pet = plugin.getPetManager().getActivePet(uuid);
                if (pet == null || !pet.isValid() || !pet.isMounting()) {
                    mountedPlayers.remove(uuid);
                    playerInputs.remove(uuid);
                    continue;
                }

                ArmorStand seat = pet.getSeatEntity();
                if (seat == null || !seat.isValid() || !seat.getPassengers().contains(player)) {
                    pet.dismount();
                    mountedPlayers.remove(uuid);
                    playerInputs.remove(uuid);
                    continue;
                }

                Input input = playerInputs.get(uuid);
                if (input == null) continue;

                PetData data = pet.getData();
                double classMult = plugin.getConfigManager().getClassSpeedMultiplier(data.getPetClass());
                double speed = 0.32 * classMult;
                if (input.isSprint()) {
                    speed *= 1.3;
                }

                Vector dir = player.getLocation().getDirection().setY(0);
                if (dir.lengthSquared() > 0.001) dir.normalize();

                Vector side = new Vector(-dir.getZ(), 0, dir.getX()); // Left
                Vector moveVec = new Vector(0, 0, 0);

                if (input.isForward()) moveVec.add(dir);
                if (input.isBackward()) moveVec.subtract(dir);
                if (input.isLeft()) moveVec.add(side);
                if (input.isRight()) moveVec.subtract(side);

                if (moveVec.lengthSquared() > 0.001) {
                    moveVec.normalize().multiply(speed);
                }

                Location currentLoc = seat.getLocation();
                double newY = currentLoc.getY();

                // Jump / Double Jump handling
                if (input.isJump()) {
                    boolean canDoubleJump = data.getLevel() >= plugin.getConfigManager().getDoubleJumpUnlockLevel();
                    long now = System.currentTimeMillis();
                    long lastJump = doubleJumpCooldown.getOrDefault(uuid, 0L);

                    if (seat.isOnGround()) {
                        moveVec.setY(0.42);
                    } else if (canDoubleJump && (now - lastJump > 2500)) {
                        doubleJumpCooldown.put(uuid, now);
                        moveVec.add(player.getLocation().getDirection().multiply(0.65)).setY(0.55);
                        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 15, 0.2, 0.2, 0.2, 0.08);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.2f);
                    }
                }

                // Check step-up (0.5 or 1 block in front)
                if (moveVec.lengthSquared() > 0.001) {
                    Location inFront = currentLoc.clone().add(moveVec.clone().normalize().multiply(0.7));
                    Block footBlock = inFront.getBlock();
                    Block headBlock = inFront.clone().add(0, 1, 0).getBlock();

                    if (!footBlock.isPassable() && headBlock.isPassable()) {
                        // Step up smoothly
                        moveVec.setY(0.5);
                    }
                }

                Location targetLoc = currentLoc.add(moveVec);
                targetLoc.setYaw(player.getLocation().getYaw());
                seat.teleport(targetLoc);

                // Keep pet display with seat
                pet.getDisplayEntity().teleport(targetLoc.clone().add(0, 0.35, 0));
                pet.getNameTagDisplay().teleport(targetLoc.clone().add(0, 1.25, 0));
            }
        }, 1L, 1L); // Every tick
    }
}
