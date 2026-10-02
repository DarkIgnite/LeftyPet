package com.leftycraft.leftypet.entity;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.model.PetSkin;
import com.leftycraft.leftypet.util.ColorUtil;
import com.leftycraft.leftypet.util.HeadUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collection;

public class ActivePet {

    private final LeftyPetPlugin plugin;
    private final Player owner;
    private final PetData data;

    private ItemDisplay displayEntity;
    private TextDisplay nameTagDisplay;
    private Interaction interactionEntity;

    private int ticksLived = 0;
    private float smoothedYaw = 0f;
    private long lastSupportHeal = 0;
    private long lastLooterPickup = 0;

    public ActivePet(LeftyPetPlugin plugin, Player owner, PetData data) {
        this.plugin = plugin;
        this.owner = owner;
        this.data = data;
        this.smoothedYaw = owner.getLocation().getYaw();
        spawn();
    }

    public void spawn() {
        Location spawnLoc = owner.getLocation().add(0, 1.5, 0);

        // 1. Spawn ItemDisplay (Floating Head)
        displayEntity = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.FIXED); // FIXED so pet has real world yaw/facing direction
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
            display.setInterpolationDuration(plugin.getConfigManager().getInterpolationDuration());
            display.setTeleportDuration(plugin.getConfigManager().getInterpolationDuration());

            float scale = plugin.getConfigManager().getPetScale();
            Transformation transformation = new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 1f, 0f),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0f, 0f, 1f, 0f)
            );
            display.setTransformation(transformation);

            PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
            ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
            display.setItemStack(head);
        });

        // 2. Spawn TextDisplay (Nametag, Class, Energy bar)
        nameTagDisplay = spawnLoc.getWorld().spawn(spawnLoc.clone().add(0, 0.75, 0), TextDisplay.class, text -> {
            text.setPersistent(false);
            text.setBillboard(Display.Billboard.CENTER);
            text.setDefaultBackground(false);
            text.setSeeThrough(false);
            text.setShadowed(true);
            text.setInterpolationDuration(plugin.getConfigManager().getInterpolationDuration());
            text.setTeleportDuration(plugin.getConfigManager().getInterpolationDuration());
        });

        // 3. Spawn Interaction Entity (Hitbox for click & punch)
        interactionEntity = spawnLoc.getWorld().spawn(spawnLoc.clone().subtract(0, 0.45, 0), Interaction.class, inter -> {
            inter.setPersistent(false);
            inter.setInteractionWidth(0.9f);
            inter.setInteractionHeight(0.9f);
            inter.setResponsive(true);
        });

        updateNameTag();
    }

    public void tick() {
        if (!isValid()) return;
        ticksLived++;

        if (ticksLived % 10 == 0) {
            updateNameTag();
        }

        // Milestone Passive Effects: Every 40 ticks apply cumulative potion effects based on level
        if (ticksLived % 40 == 0 && !data.isFainted() && !data.isTraining()) {
            int lvl = data.getLevel();
            // Lv 10+: Speed I
            if (lvl >= 10) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false, true));
            }
            // Lv 20+: Haste I
            if (lvl >= 20) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 80, 0, false, false, true));
            }
            // Lv 30+: Night Vision
            if (lvl >= 30) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false, true));
            }
            // Lv 40+: Resistance I
            if (lvl >= 40) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0, false, false, true));
            }
            // Lv 50+: Strength I
            if (lvl >= 50) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 0, false, false, true));
            }
            // Lv 60+: Luck
            if (lvl >= 60) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 80, 0, false, false, true));
            }
            // Lv 70+: Fire Resistance
            if (lvl >= 70) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0, false, false, true));
            }
            // Lv 80+: Regeneration I
            if (lvl >= 80) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false, true));
            }
            // Lv 90+: Speed II (upgrade from I)
            if (lvl >= 90) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false, true));
            }
            // Lv 100: Strength II + Absorption (Mythic Transcendence full aura)
            if (lvl >= 100) {
                owner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 1, false, false, true));
                owner.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 80, 1, false, false, true));
            }
        }

        // Skill VII: Overdrive (Lv 70+) - Passive +1% energy regen every 10 seconds (200 ticks)
        if (ticksLived % 200 == 0 && data.getLevel() >= 70 && !data.isFainted() && !data.isTraining()) {
            if (data.getEnergy() < 100.0) {
                data.addEnergy(1.0);
                displayEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, displayEntity.getLocation().add(0, 0.4, 0), 2, 0.2, 0.2, 0.2, 0.02);
            }
        }


        spawnParticleTrail();

        // Following mode: Smoothed orbit so player can turn to look at pet without pet fleeing
        float playerYaw = owner.getLocation().getYaw();
        float diff = (playerYaw - smoothedYaw) % 360f;
        if (diff > 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;

        // Slow down orbit when player is looking at the pet so they can easily interact
        Vector eyeToPet = displayEntity.getLocation().toVector().subtract(owner.getEyeLocation().toVector()).normalize();
        double dot = owner.getEyeLocation().getDirection().normalize().dot(eyeToPet);
        float followRate = (dot > 0.45) ? 0.02f : 0.12f;
        smoothedYaw += diff * followRate;

        double rad = Math.toRadians(smoothedYaw);
        Vector dir = new Vector(-Math.sin(rad), 0, Math.cos(rad));
        Vector side = new Vector(-dir.getZ(), 0, dir.getX());

        double bobbing = Math.sin((ticksLived + owner.getEntityId()) * 0.15) * 0.12;

        Location targetLoc = owner.getLocation()
                .add(side.multiply(1.80))
                .add(dir.multiply(-0.15))
                .add(0, 1.30 + bobbing, 0);

        double distSq = displayEntity.getLocation().distanceSquared(targetLoc);

        // Calculate target facing direction:
        // When player looks towards pet (dot > 0.45), pet looks directly at player (eye contact)
        // Otherwise, pet faces forward in player's POV
        float targetYaw;
        if (dot > 0.45) {
            Vector petToPlayer = owner.getEyeLocation().toVector().subtract(displayEntity.getLocation().toVector());
            float angleToPlayer = (float) Math.toDegrees(Math.atan2(-petToPlayer.getX(), petToPlayer.getZ()));
            targetYaw = (angleToPlayer + 180f + 360f) % 360f;
        } else {
            targetYaw = (owner.getLocation().getYaw() + 180f + 360f) % 360f;
        }

        float currentYaw = displayEntity.getLocation().getYaw();
        float yawDiff = Math.abs((currentYaw - targetYaw + 540f) % 360f - 180f);

        if (distSq > 576.0) {
            targetLoc.setYaw(targetYaw);
            targetLoc.setPitch(0f);
            displayEntity.teleport(targetLoc);
            nameTagDisplay.teleport(targetLoc.clone().add(0, 0.70, 0));
            if (interactionEntity != null && interactionEntity.isValid()) {
                interactionEntity.teleport(targetLoc.clone().subtract(0, 0.45, 0));
            }
        } else if (distSq > 0.001 || yawDiff > 1.0f) {
            Location current = displayEntity.getLocation();
            Vector moveVec = targetLoc.toVector().subtract(current.toVector()).multiply(0.35);
            Location newLoc = current.add(moveVec);
            newLoc.setYaw(targetYaw);
            newLoc.setPitch(0f);
            displayEntity.teleport(newLoc);
            nameTagDisplay.teleport(newLoc.clone().add(0, 0.70, 0));
            if (interactionEntity != null && interactionEntity.isValid()) {
                interactionEntity.teleport(newLoc.clone().subtract(0, 0.45, 0));
            }
        }

        if (data.getPetClass() == PetClass.SUPPORT && !data.isFainted()) {
            handleSupportAura();
        }

        if (data.getPetClass() == PetClass.LOOTER && !data.isFainted()) {
            handleLooterVacuum();
        }

        if (data.getPetClass() == PetClass.TRAVELER && !data.isFainted()) {
            owner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false, true));
        }
    }

    private void handleSupportAura() {
        if (System.currentTimeMillis() - lastSupportHeal > 5000) {
            if (owner.getHealth() < 10.0 && data.getEnergy() > 5.0) {
                lastSupportHeal = System.currentTimeMillis();
                owner.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false, true));
                data.drainEnergy(1.0);
                owner.getWorld().spawnParticle(Particle.HEART, owner.getLocation().add(0, 1.5, 0), 4, 0.3, 0.3, 0.3, 0.05);
                owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
            }
        }
    }

    private void handleLooterVacuum() {
        if (System.currentTimeMillis() - lastLooterPickup > 1000) {
            lastLooterPickup = System.currentTimeMillis();
            Collection<Item> nearbyItems = displayEntity.getLocation().getNearbyEntitiesByType(Item.class, 4.0);
            for (Item item : nearbyItems) {
                if (!item.isValid() || item.isDead() || item.getPickupDelay() > 0) continue;
                ItemStack stack = item.getItemStack();
                if (owner.getInventory().firstEmpty() != -1 || owner.getInventory().contains(stack.getType())) {
                    owner.getInventory().addItem(stack);
                    owner.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, item.getLocation(), 3, 0.1, 0.1, 0.1, 0.02);
                    owner.playSound(owner.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.4f, 1.5f);
                    item.remove();
                    break;
                }
            }
        }
    }

    private void spawnParticleTrail() {
        if (data.isFainted()) return;
        try {
            Particle particle = Particle.valueOf(data.getTrailKey());
            Location loc = displayEntity.getLocation().add(0, 0.2, 0);
            loc.getWorld().spawnParticle(particle, loc, 1, 0.05, 0.05, 0.05, 0.01);
        } catch (Exception ignored) {}
    }

    public void updateNameTag() {
        if (nameTagDisplay == null || !nameTagDisplay.isValid()) return;

        String faintedTag = data.isFainted() ? " <red>[ᴘɪɴɢsᴀɴ]</red>" : "";
        String line1 = ColorUtil.getLevelTag(data.getLevel()) + " <white>" + data.getName() + "</white>" + faintedTag;
        String line2 = "<gray>ᴋᴇʟᴀs: </gray>" + data.getPetClass().getDisplayName();
        String line3 = "<green>ᴇɴᴇʀɢɪ: </green>" + data.getEnergyProgressBar() + " <white>" + (int) data.getEnergy() + "%</white>";

        Component comp = ColorUtil.component(line1 + "\n" + line2 + "\n" + line3);
        nameTagDisplay.text(comp);
    }

    public void updateSkin() {
        if (displayEntity == null || !displayEntity.isValid()) return;
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        displayEntity.setItemStack(head);
    }

    public boolean isValid() {
        return displayEntity != null && displayEntity.isValid() &&
                nameTagDisplay != null && nameTagDisplay.isValid();
    }

    public void despawn() {
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.remove();
        }
        if (nameTagDisplay != null && nameTagDisplay.isValid()) {
            nameTagDisplay.remove();
        }
        if (interactionEntity != null && interactionEntity.isValid()) {
            interactionEntity.remove();
        }
        displayEntity = null;
        nameTagDisplay = null;
        interactionEntity = null;
    }

    public ItemDisplay getDisplayEntity() {
        return displayEntity;
    }

    public TextDisplay getNameTagDisplay() {
        return nameTagDisplay;
    }

    public Interaction getInteractionEntity() {
        return interactionEntity;
    }

    public Player getOwner() {
        return owner;
    }

    public PetData getData() {
        return data;
    }
}
