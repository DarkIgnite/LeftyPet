package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.PetMenu;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CombatListener implements Listener {

    private final LeftyPetPlugin plugin;
    private final Map<UUID, Long> aegisCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> totemCooldown = new ConcurrentHashMap<>();

    public CombatListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPetEntityDamage(EntityDamageByEntityEvent event) {
        // Prevent pets from taking damage
        if (event.getEntity() instanceof Display || event.getEntity() instanceof ArmorStand) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
                if (pet != null) {
                    if (event.getEntity().equals(pet.getDisplayEntity()) ||
                            event.getEntity().equals(pet.getNameTagDisplay()) ||
                            event.getEntity().equals(pet.getInteractionEntity())) {
                        event.setCancelled(true);

                        // If owner shifts + punches pet -> open PetMenu (Revisi 7)
                        if (event.getDamager() instanceof Player damager && damager.equals(player) && damager.isSneaking()) {
                            PetMenu.open(damager, plugin);
                        }
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (plugin.getCombatManager().isProcessingAttack()) {
            return;
        }

        // Requirement 1: Pet attacks ANY mob or player that the owner hits!
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (target.equals(player) || target instanceof ArmorStand || target instanceof Display) {
                return;
            }
            if (target instanceof Tameable tameable && tameable.isTamed() && player.equals(tameable.getOwner())) {
                return;
            }

            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null && pet.isValid() && !pet.getData().isFainted()) {
                plugin.getCombatManager().performAttack(player, pet, target);
            }
        } else if (event.getEntity() instanceof Player player && event.getDamager() instanceof LivingEntity living) {
            // Retaliate if owner is attacked
            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null && pet.isValid() && !pet.getData().isFainted()) {
                plugin.getCombatManager().performAttack(player, pet, living);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHitShatteredTarget(EntityDamageByEntityEvent event) {
        // Skill V: Armor Shatter - +25% bonus damage when owner attacks shattered target
        if (plugin.getCombatManager().isProcessingAttack()) return;

        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (plugin.getCombatManager().isEntityShattered(target)) {
                event.setDamage(event.getDamage() * 1.25);
                target.getWorld().spawnParticle(Particle.CRIT, target.getEyeLocation(), 8, 0.2, 0.2, 0.2, 0.1);
                player.playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 1.4f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet == null || !pet.isValid() || pet.getData().isFainted()) return;

        PetData data = pet.getData();
        double currentHealth = player.getHealth();
        double finalDamage = event.getFinalDamage();
        AttributeInstance maxAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHp = (maxAttr != null) ? maxAttr.getValue() : 20.0;

        // Skill X: Mythic Transcendence (Lv 100) - Totem Savior on fatal damage
        if (data.getLevel() >= 100 && (currentHealth - finalDamage) <= 0.0) {
            long now = System.currentTimeMillis();
            long lastTotem = totemCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (now - lastTotem >= 600_000L) { // 10 minutes cooldown
                totemCooldown.put(player.getUniqueId(), now);
                event.setCancelled(true);
                player.setHealth(maxHp);
                data.setEnergy(0.0); // Pet sacrifices all energy
                pet.updateNameTag();

                player.playEffect(EntityEffect.TOTEM_RESURRECT);
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                ColorUtil.sendMessage(player, "<gradient:#ff007f:#7928ca><b>[ʟᴇғᴛʏᴘᴇᴛ]</b></gradient> <light_purple>👑 <b>Mythic Transcendence!</b> Pet kamu mengorbankan seluruh energinya untuk menyelamatkanmu dari kematian!</light_purple>");
                return;
            }
        }

        // Skill IV: Guardian Aegis (Lv 40+) - Emergency shield when HP < 30%
        if (data.getLevel() >= 40 && (currentHealth - finalDamage) <= (maxHp * 0.30)) {
            long now = System.currentTimeMillis();
            long lastAegis = aegisCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (now - lastAegis >= 60_000L) { // 60 seconds cooldown
                aegisCooldown.put(player.getUniqueId(), now);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1, false, false, true)); // Resistance II (5s)
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1, false, false, true)); // Absorption II (10s)
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
                player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
                ColorUtil.sendMessage(player, "<gradient:#00f2fe:#4facfe><b>[ʟᴇғᴛʏᴘᴇᴛ]</b></gradient> <yellow>🛡️ <b>Guardian Aegis</b> aktif! Perisai darurat melindungi kamu dari maut!</yellow>");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ActivePet pet = plugin.getPetManager().getActivePet(killer.getUniqueId());
        if (pet == null || !pet.isValid() || pet.getData().isFainted()) return;

        int petLevel = pet.getData().getLevel();

        // Skill VI: Fortune's Favor (Lv 60+) - 35% chance to double EXP and duplicate 1 drop
        if (petLevel >= 60 && ThreadLocalRandom.current().nextInt(100) < 35) {
            event.setDroppedExp((int) Math.round(event.getDroppedExp() * 2.0));
            if (!event.getDrops().isEmpty()) {
                ItemStack randomDrop = event.getDrops().get(ThreadLocalRandom.current().nextInt(event.getDrops().size()));
                if (randomDrop != null && randomDrop.getType() != org.bukkit.Material.AIR) {
                    event.getDrops().add(randomDrop.clone());
                }
            }
            killer.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, event.getEntity().getLocation().add(0, 0.5, 0), 8, 0.3, 0.3, 0.3, 0.05);
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);
        }

        // MythicMobs 2x Drop System (Lv 20+)
        if (!isMythicMob(event.getEntity())) return;
        int chance = getMythicDoubleDropChance(petLevel);
        if (chance <= 0) return;

        if (ThreadLocalRandom.current().nextInt(100) < chance) {
            // Duplicate all drops
            List<ItemStack> bonusDrops = new java.util.ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (drop != null && drop.getType() != org.bukkit.Material.AIR) {
                    bonusDrops.add(drop.clone());
                }
            }
            event.getDrops().addAll(bonusDrops);
            // Double exp
            event.setDroppedExp(event.getDroppedExp() * 2);

            // Particles & actionbar
            killer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, event.getEntity().getLocation().add(0, 0.5, 0), 20, 0.4, 0.4, 0.4, 0.15);
            killer.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, event.getEntity().getLocation().add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.05);
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f);

            killer.sendActionBar(ColorUtil.component(
                "<gradient:#ffd200:#ff9500><b>⚔ MYTHIC 2x DROP!</b></gradient> <gray>(" + chance + "% chance | ᴘᴇᴛ ʟᴠ." + petLevel + ")</gray>"
            ));
        }
    }

    private boolean isMythicMob(org.bukkit.entity.Entity entity) {
        if (entity == null) return false;
        // Check metadata keys set by MythicMobs
        if (entity.hasMetadata("MYTHIC_TYPE") || entity.hasMetadata("MYTHIC_MOB")) return true;
        // Try MythicMobs API via reflection (no hard compile dependency needed)
        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            try {
                Class<?> mbClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Object inst = mbClass.getMethod("inst").invoke(null);
                Object helper = inst.getClass().getMethod("getAPIHelper").invoke(inst);
                return (boolean) helper.getClass().getMethod("isMythicMob", org.bukkit.entity.Entity.class).invoke(helper, entity);
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Returns the 2x drop chance (0-90) for MythicMobs based on pet level.
     * Lv 20 = 10%, Lv 30 = 20%, ... Lv 100 = 90%
     */
    private int getMythicDoubleDropChance(int level) {
        if (level < 20) return 0;
        if (level >= 100) return 90;
        return ((level / 10) - 1) * 10;
    }
}

