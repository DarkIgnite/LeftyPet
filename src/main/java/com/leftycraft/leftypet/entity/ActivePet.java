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
    private ArmorStand seatEntity;

    private int ticksLived = 0;
    private boolean isMounting = false;
    private long lastSupportHeal = 0;
    private long lastLooterPickup = 0;

    public ActivePet(LeftyPetPlugin plugin, Player owner, PetData data) {
        this.plugin = plugin;
        this.owner = owner;
        this.data = data;
        spawn();
    }

    public void spawn() {
        Location spawnLoc = owner.getLocation().add(0, 1.5, 0);

        // 1. Spawn ItemDisplay (Floating Head)
        displayEntity = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.CENTER);
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

        // 2. Spawn TextDisplay (Nametag & Energy bar)
        nameTagDisplay = spawnLoc.getWorld().spawn(spawnLoc.clone().add(0, 0.65, 0), TextDisplay.class, text -> {
            text.setPersistent(false);
            text.setBillboard(Display.Billboard.CENTER);
            text.setDefaultBackground(false);
            text.setSeeThrough(false);
            text.setShadowed(true);
            text.setInterpolationDuration(plugin.getConfigManager().getInterpolationDuration());
            text.setTeleportDuration(plugin.getConfigManager().getInterpolationDuration());
        });

        // 3. Spawn Seat Entity (ArmorStand for mounting)
        seatEntity = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setPersistent(false);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setInvulnerable(true);
            stand.setMarker(false);
            stand.setBasePlate(false);
            stand.setArms(false);
        });

        updateNameTag();
    }

    public void tick() {
        if (!isValid()) return;
        ticksLived++;

        // Update Nametag text every 10 ticks
        if (ticksLived % 10 == 0) {
            updateNameTag();
        }

        // Spawn particle trail
        spawnParticleTrail();

        if (isMounting && seatEntity != null && seatEntity.isValid()) {
            // Mounting mode: Pet stays with seat entity
            Location seatLoc = seatEntity.getLocation();
            displayEntity.teleport(seatLoc.clone().add(0, 0.4, 0));
            nameTagDisplay.teleport(seatLoc.clone().add(0, 1.25, 0));

            // Riding energy drain check
            if (ticksLived % 200 == 0) { // every 10 seconds
                data.drainEnergy(plugin.getConfigManager().getEnergyDrainPerRiding());
                if (data.isFainted()) {
                    dismount();
                    owner.sendMessage(plugin.getConfigManager().getMessage("pet-fainted"));
                }
            }
            return;
        }

        // Following mode: Hover over owner's shoulder
        double bobbing = Math.sin((ticksLived + owner.getEntityId()) * 0.15) * 0.12;

        Vector dir = owner.getLocation().getDirection().setY(0);
        if (dir.lengthSquared() > 0.001) {
            dir.normalize();
        } else {
            dir = new Vector(1, 0, 0);
        }

        Vector side = new Vector(-dir.getZ(), 0, dir.getX()); // Perpendicular
        Location targetLoc = owner.getLocation()
                .add(side.multiply(0.85))
                .subtract(dir.multiply(0.3))
                .add(0, 1.35 + bobbing, 0);

        double distSq = displayEntity.getLocation().distanceSquared(targetLoc);
        if (distSq > 576.0) { // > 24 blocks -> teleport instantly
            displayEntity.teleport(targetLoc);
            nameTagDisplay.teleport(targetLoc.clone().add(0, 0.55, 0));
        } else if (distSq > 0.04) {
            // Smooth lerp movement towards target
            Location current = displayEntity.getLocation();
            Vector moveVec = targetLoc.toVector().subtract(current.toVector()).multiply(0.35);
            Location newLoc = current.add(moveVec);
            newLoc.setDirection(owner.getLocation().getDirection());
            displayEntity.teleport(newLoc);
            nameTagDisplay.teleport(newLoc.clone().add(0, 0.55, 0));
        }

        // Passive Support Class check
        if (data.getPetClass() == PetClass.SUPPORT && !data.isFainted()) {
            handleSupportAura();
        }

        // Passive Looter Class check
        if (data.getPetClass() == PetClass.LOOTER && !data.isFainted()) {
            handleLooterVacuum();
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
                    break; // one stack at a time
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

        String faintedTag = data.isFainted() ? " &c[Pingsan]" : "";
        String line1 = "&e[Lv." + data.getLevel() + "] " + data.getName() + faintedTag;
        String line2 = "&aEnergi: " + data.getEnergyProgressBar() + " &f" + (int) data.getEnergy() + "%";

        Component comp = ColorUtil.component(line1 + "\n" + line2);
        nameTagDisplay.text(comp);
    }

    public void updateSkin() {
        if (displayEntity == null || !displayEntity.isValid()) return;
        PetSkin skin = plugin.getConfigManager().getSkin(data.getSkinKey());
        ItemStack head = (skin != null) ? HeadUtil.createCustomHead(skin.getTexture()) : new ItemStack(Material.PLAYER_HEAD);
        displayEntity.setItemStack(head);
    }

    public void mount() {
        if (isMounting || seatEntity == null || !seatEntity.isValid()) return;
        isMounting = true;
        seatEntity.teleport(owner.getLocation());
        seatEntity.addPassenger(owner);
    }

    public void dismount() {
        if (!isMounting) return;
        isMounting = false;
        if (seatEntity != null && seatEntity.isValid()) {
            seatEntity.removePassenger(owner);
        }
    }

    public boolean isMounting() {
        return isMounting;
    }

    public void setMounting(boolean mounting) {
        this.isMounting = mounting;
    }

    public boolean isValid() {
        return displayEntity != null && displayEntity.isValid() &&
                nameTagDisplay != null && nameTagDisplay.isValid();
    }

    public void despawn() {
        dismount();
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.remove();
        }
        if (nameTagDisplay != null && nameTagDisplay.isValid()) {
            nameTagDisplay.remove();
        }
        if (seatEntity != null && seatEntity.isValid()) {
            seatEntity.remove();
        }
        displayEntity = null;
        nameTagDisplay = null;
        seatEntity = null;
    }

    public ItemDisplay getDisplayEntity() {
        return displayEntity;
    }

    public TextDisplay getNameTagDisplay() {
        return nameTagDisplay;
    }

    public ArmorStand getSeatEntity() {
        return seatEntity;
    }

    public Player getOwner() {
        return owner;
    }

    public PetData getData() {
        return data;
    }
}
