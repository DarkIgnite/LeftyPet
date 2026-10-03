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
    private ArmorStand bedrockStand; // Bedrock (GeyserMC) fallback entity
    private TextDisplay nameTagDisplay;
    private Interaction interactionEntity;

    private int ticksLived = 0;
    private float smoothedYaw = 0f;
    private long lastSupportHeal = 0;
    private long lastLooterPickup = 0;
    private long lastPetTime = 0;
    private boolean isSleeping = false;
    private Location lastOwnerLocation = null;
    private int afkTimer = 0;
    private int celebratingTicks = 0;

    public ActivePet(LeftyPetPlugin plugin, Player owner, PetData data) {
        this.plugin = plugin;
        this.owner = owner;
        this.data = data;
        this.smoothedYaw = owner.getLocation().getYaw();
        this.lastOwnerLocation = owner.getLocation().clone();
        spawn();
    }

    public void spawn() {
        Location spawnLoc = owner.getLocation().add(0, 1.5, 0);
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);

        // 1. Spawn ItemDisplay (Floating Head for Java Edition)
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
            display.setItemStack(head);
        });

        // 1b. Spawn Bedrock ArmorStand Fallback (for Bedrock Edition via GeyserMC)
        Location standLoc = spawnLoc.clone().subtract(0, 0.70, 0);
        bedrockStand = spawnLoc.getWorld().spawn(standLoc, ArmorStand.class, stand -> {
            stand.setPersistent(false);
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCollidable(false);
            stand.setSilent(true);
            stand.setBasePlate(false);
            stand.setArms(false);
            stand.getEquipment().setHelmet(head);
        });

        // 2. Spawn TextDisplay (Nametag, Class, Energy bar)
        nameTagDisplay = spawnLoc.getWorld().spawn(spawnLoc.clone().add(0, 0.75, 0), TextDisplay.class, text -> {
            text.setPersistent(false);
            text.setBillboard(Display.Billboard.CENTER);
            text.setDefaultBackground(false);
            text.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0)); // 100% transparent background
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
        updateVisibilityForAll();
    }

    public void tick() {
        if (!owner.isOnline()) return;
        if (!isValid()) {
            despawn();
            spawn();
            return;
        }
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

        // AFK / Sleep Mood Detection
        Location curOwnerLoc = owner.getLocation();
        if (lastOwnerLocation == null || curOwnerLoc.distanceSquared(lastOwnerLocation) < 0.04) {
            afkTimer++;
            if (afkTimer > 450 && !isSleeping) { // ~45 seconds idle
                isSleeping = true;
                updateNameTag();
            }
        } else {
            if (isSleeping) {
                isSleeping = false;
                updateNameTag();
                // Happy wake up jump!
                displayEntity.getWorld().spawnParticle(Particle.HEART, displayEntity.getLocation().add(0, 0.4, 0), 2, 0.2, 0.2, 0.2, 0.02);
            }
            afkTimer = 0;
            lastOwnerLocation = curOwnerLoc.clone();
        }

        // Sleep particles: cute zZz cloud
        if (isSleeping && ticksLived % 30 == 0) {
            displayEntity.getWorld().spawnParticle(Particle.CLOUD, displayEntity.getLocation().add(0, 0.35, 0), 2, 0.1, 0.1, 0.1, 0.01);
        }

        // Hunger whimpers & smoke when energy < 15%
        if (data.getEnergy() < 15.0 && !data.isFainted() && !data.isTraining() && ticksLived % 100 == 0) {
            displayEntity.getWorld().spawnParticle(Particle.SMOKE, displayEntity.getLocation().add(0, 0.3, 0), 3, 0.1, 0.1, 0.1, 0.01);
            com.leftycraft.leftypet.util.PetSoundUtil.playHungrySound(owner, displayEntity.getLocation(), data.getSkinKey());
        }

        // Celebration Spin Animation (Level up / Duel Victory)
        if (celebratingTicks > 0) {
            celebratingTicks--;
            smoothedYaw += 36f;
            displayEntity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, displayEntity.getLocation().add(0, 0.3, 0), 3, 0.2, 0.2, 0.2, 0.05);
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

        double bobbing = data.isFainted() ? 0.0 : Math.sin((ticksLived + owner.getEntityId()) * 0.15) * 0.12;
        double stateOffset = data.isFainted() ? -0.65 : (isSleeping ? -0.25 : 0.0);

        Location targetLoc = owner.getLocation()
                .add(side.multiply(1.80))
                .add(dir.multiply(-0.15))
                .add(0, 1.30 + bobbing + stateOffset, 0);

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
            if (bedrockStand != null && bedrockStand.isValid()) {
                bedrockStand.teleport(targetLoc.clone().subtract(0, 0.70, 0));
            }
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
            if (bedrockStand != null && bedrockStand.isValid()) {
                bedrockStand.teleport(newLoc.clone().subtract(0, 0.70, 0));
            }
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
        String moodTag = isSleeping ? " <gray>[zZz]</gray>" : "";
        String line1 = ColorUtil.getLevelTag(data.getLevel()) + " <white>" + data.getName() + "</white>" + faintedTag + moodTag;
        String line2 = "<gray>ᴋᴇʟᴀs: </gray>" + data.getPetClass().getDisplayName();
        String line3 = "<green>ᴇɴᴇʀɢɪ: </green>" + data.getEnergyProgressBar() + " <white>" + (int) data.getEnergy() + "%</white>";

        Component comp = ColorUtil.component(line1 + "\n" + line2 + "\n" + line3);
        nameTagDisplay.text(comp);
    }

    public void pet(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastPetTime < 3000L) {
            player.sendMessage(ColorUtil.component("<yellow>Pet kamu masih merasa sangat disayangi! ❤</yellow>"));
            return;
        }
        lastPetTime = now;
        if (isSleeping) {
            isSleeping = false;
            afkTimer = 0;
            updateNameTag();
        }

        if (displayEntity != null && displayEntity.isValid()) {
            Location loc = displayEntity.getLocation().add(0, 0.35, 0);
            loc.getWorld().spawnParticle(Particle.HEART, loc, 5, 0.25, 0.25, 0.25, 0.05);
            com.leftycraft.leftypet.util.PetSoundUtil.playHappySound(player, loc, data.getSkinKey());
        }

        if (data.getEnergy() < 100.0) {
            data.addEnergy(1.0);
            updateNameTag();
        }

        player.sendMessage(ColorUtil.component("<gradient:#ff758c:#ff7eb3><b>❤</b> Kamu mengelus <b>" + data.getName() + "</b>! Pet kamu merasa sangat senang.</gradient>"));
    }

    public void playCelebrationAnimation() {
        this.celebratingTicks = 20;
        if (displayEntity != null && displayEntity.isValid()) {
            Location loc = displayEntity.getLocation().add(0, 0.5, 0);
            loc.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.4, 0.4, 0.4, 0.1);
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.5f);
        }
    }

    public void faint() {
        data.setEnergy(0.0);
        updateNameTag();
        if (displayEntity != null && displayEntity.isValid()) {
            Location loc = displayEntity.getLocation().add(0, 0.3, 0);
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 15, 0.25, 0.25, 0.25, 0.05);
            loc.getWorld().playSound(loc, Sound.ENTITY_VILLAGER_DEATH, 0.7f, 1.2f);
            com.leftycraft.leftypet.util.PetSoundUtil.playHungrySound(owner, loc, data.getSkinKey());
        }
        owner.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<gradient:#ff5f6d:#ffc371><b>Pet kamu kehabisan daya dan pingsan!</b> Beri makan pet untuk membangunkannya.</gradient>"));
    }

    public void updateSkin() {
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.setItemStack(head);
        }
        if (bedrockStand != null && bedrockStand.isValid()) {
            bedrockStand.getEquipment().setHelmet(head);
        }
    }

    public void updateVisibilityFor(Player viewer) {
        if (viewer == null || !viewer.isOnline()) return;
        boolean isBedrock = com.leftycraft.leftypet.util.BedrockUtil.isBedrockPlayer(viewer);

        if (displayEntity != null && displayEntity.isValid()) {
            if (isBedrock) {
                viewer.hideEntity(plugin, displayEntity);
            } else {
                viewer.showEntity(plugin, displayEntity);
            }
        }

        if (bedrockStand != null && bedrockStand.isValid()) {
            if (isBedrock) {
                viewer.showEntity(plugin, bedrockStand);
            } else {
                viewer.hideEntity(plugin, bedrockStand);
            }
        }
    }

    public void updateVisibilityForAll() {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            updateVisibilityFor(p);
        }
    }

    public boolean isValid() {
        boolean headValid = (displayEntity != null && displayEntity.isValid()) ||
                            (bedrockStand != null && bedrockStand.isValid());
        return headValid && nameTagDisplay != null && nameTagDisplay.isValid();
    }

    public void despawn() {
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.remove();
        }
        if (bedrockStand != null && bedrockStand.isValid()) {
            bedrockStand.remove();
        }
        if (nameTagDisplay != null && nameTagDisplay.isValid()) {
            nameTagDisplay.remove();
        }
        if (interactionEntity != null && interactionEntity.isValid()) {
            interactionEntity.remove();
        }
        displayEntity = null;
        bedrockStand = null;
        nameTagDisplay = null;
        interactionEntity = null;
    }

    public ItemDisplay getDisplayEntity() {
        return displayEntity;
    }

    public ArmorStand getBedrockStand() {
        return bedrockStand;
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
