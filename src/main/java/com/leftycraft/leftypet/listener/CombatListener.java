package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.PetMenu;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final LeftyPetPlugin plugin;

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
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Monster monster) {
            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null && pet.isValid() && !pet.getData().isFainted()) {
                plugin.getCombatManager().performAttack(player, pet, monster);
            }
        } else if (event.getEntity() instanceof Player player && event.getDamager() instanceof LivingEntity living) {
            ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
            if (pet != null && pet.isValid() && !pet.getData().isFainted()) {
                plugin.getCombatManager().performAttack(player, pet, living);
            }
        }
    }
}
