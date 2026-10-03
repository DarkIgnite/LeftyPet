package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.AltarCancelMenu;
import com.leftycraft.leftypet.gui.AltarMenu;
import com.leftycraft.leftypet.gui.CosmeticMenu;
import com.leftycraft.leftypet.gui.PetMenu;
import com.leftycraft.leftypet.gui.PetRoadmapMenu;
import com.leftycraft.leftypet.gui.holder.AltarCancelMenuHolder;
import com.leftycraft.leftypet.gui.holder.AltarMenuHolder;
import com.leftycraft.leftypet.gui.holder.CosmeticMenuHolder;
import com.leftycraft.leftypet.gui.holder.PetMenuHolder;
import com.leftycraft.leftypet.gui.holder.PetRoadmapMenuHolder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PetInteractListener implements Listener {

    private final LeftyPetPlugin plugin;

    public PetInteractListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteractAt(PlayerInteractAtEntityEvent event) {
        handleInteract(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        handleInteract(event.getPlayer(), event.getRightClicked(), event);
    }

    private void handleInteract(Player player, Entity clicked, org.bukkit.event.Cancellable event) {
        for (ActivePet pet : plugin.getPetManager().getActivePets().values()) {
            if (clicked.equals(pet.getInteractionEntity()) || clicked.equals(pet.getDisplayEntity()) ||
                    clicked.equals(pet.getNameTagDisplay()) || (pet.getBedrockStand() != null && clicked.equals(pet.getBedrockStand()))) {
                event.setCancelled(true);
                if (player.getUniqueId().equals(pet.getOwner().getUniqueId())) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    // 1. Petting: sneaking or empty hand
                    if (player.isSneaking() || hand.getType() == Material.AIR) {
                        pet.pet(player);
                        return;
                    }
                    // 2. Try feeding if holding food
                    if (plugin.getConfigManager().getFoodRestore(hand.getType()) > 0) {
                        plugin.getPetManager().feedPet(player, hand);
                        return;
                    }
                    // 3. Open Pet Dashboard GUI
                    PetMenu.open(player, plugin);
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Prevent damage to any pet display or interaction entity
        for (ActivePet pet : plugin.getPetManager().getActivePets().values()) {
            if (event.getEntity().equals(pet.getInteractionEntity()) || event.getEntity().equals(pet.getDisplayEntity()) ||
                    event.getEntity().equals(pet.getNameTagDisplay()) || (pet.getBedrockStand() != null && event.getEntity().equals(pet.getBedrockStand()))) {
                event.setCancelled(true);
                if (event.getDamager() instanceof Player player && player.getUniqueId().equals(pet.getOwner().getUniqueId())) {
                    if (player.isSneaking()) {
                        PetMenu.open(player, plugin);
                    }
                    return;
                }

                // If attacked by monster or hostile projectile
                if (event.getDamager() instanceof org.bukkit.entity.Monster ||
                        (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof org.bukkit.entity.Monster)) {
                    var data = pet.getData();
                    if (!data.isFainted() && !data.isTraining()) {
                        double drain = Math.max(12.0, event.getDamage() * 2.0);
                        data.drainEnergy(drain);
                        pet.updateNameTag();

                        if (pet.getDisplayEntity() != null && pet.getDisplayEntity().isValid()) {
                            org.bukkit.Location pLoc = pet.getDisplayEntity().getLocation().add(0, 0.3, 0);
                            pLoc.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, pLoc, 3, 0.2, 0.2, 0.2, 0.05);
                            pLoc.getWorld().playSound(pLoc, org.bukkit.Sound.ENTITY_VILLAGER_HURT, 0.6f, 1.4f);
                        }

                        if (data.isFainted()) {
                            pet.faint();
                        }
                    }
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PetMenuHolder) {
            event.setCancelled(true);
            PetMenu.handleClick(event, plugin);
            return;
        }

        if (holder instanceof CosmeticMenuHolder) {
            event.setCancelled(true);
            CosmeticMenu.handleClick(event, plugin);
            return;
        }

        if (holder instanceof AltarMenuHolder) {
            event.setCancelled(true);
            AltarMenu.handleClick(event, plugin);
            return;
        }

        if (holder instanceof AltarCancelMenuHolder) {
            event.setCancelled(true);
            AltarCancelMenu.handleClick(event, plugin);
            return;
        }

        if (holder instanceof PetRoadmapMenuHolder) {
            event.setCancelled(true);
            PetRoadmapMenu.handleClick(event, plugin);
            return;
        }

        // Secondary fallback by title
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("ʟᴇғᴛʏᴘᴇᴛ") || title.contains("LeftyPet") || title.contains("ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ") || title.contains("Pet Dashboard")) {
            event.setCancelled(true);
            if (title.contains("ᴘᴇᴛ ᴅᴀsʜʙᴏᴀʀᴅ") || title.contains("Pet Dashboard")) {
                PetMenu.handleClick(event, plugin);
            } else if (title.contains("ᴘɪʟɪʜ ᴋᴏsᴍᴇᴛɪᴋ") || title.contains("Pilih Kosmetik")) {
                CosmeticMenu.handleClick(event, plugin);
            } else if (title.contains("ʟᴇᴠᴇʟ ʀᴏᴀᴅᴍᴀᴘ") || title.contains("Level Roadmap")) {
                PetRoadmapMenu.handleClick(event, plugin);
            } else if (title.contains("ʙᴀᴛᴀʟᴋᴀɴ") || title.contains("Batalkan")) {
                AltarCancelMenu.handleClick(event, plugin);
            } else if (title.contains("ᴘᴇɴɢᴀᴛᴜʀᴀɴ ᴀʟᴛᴀʀ") || title.contains("Pengaturan Altar")) {
                AltarMenu.handleClick(event, plugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof PetMenuHolder || holder instanceof CosmeticMenuHolder
                || holder instanceof AltarMenuHolder || holder instanceof AltarCancelMenuHolder
                || holder instanceof PetRoadmapMenuHolder) {
            event.setCancelled(true);
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("ʟᴇғᴛʏᴘᴇᴛ") || title.contains("LeftyPet")) {
            event.setCancelled(true);
        }
    }
}
