package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.gui.CosmeticMenu;
import com.leftycraft.leftypet.gui.PetMenu;
import com.leftycraft.leftypet.util.ColorUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PetInteractListener implements Listener {

    private final LeftyPetPlugin plugin;

    public PetInteractListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        ActivePet pet = plugin.getPetManager().getActivePet(player.getUniqueId());
        if (pet == null || !pet.isValid()) return;

        // Check if player clicked their own pet display or seat
        if (clicked.equals(pet.getDisplayEntity()) || clicked.equals(pet.getSeatEntity())) {
            event.setCancelled(true);

            ItemStack hand = player.getInventory().getItemInMainHand();
            // Try feeding
            if (plugin.getConfigManager().getFoodRestore(hand.getType()) > 0) {
                plugin.getPetManager().feedPet(player, hand);
                return;
            }

            // If sneaking -> open GUI
            if (player.isSneaking()) {
                PetMenu.open(player, plugin);
                return;
            }

            // If empty hand and can mount -> mount, otherwise open menu
            if (plugin.getMountManager().canMount(pet.getData())) {
                if (pet.isMounting()) {
                    plugin.getMountManager().stopMount(player);
                } else {
                    plugin.getMountManager().startMount(player);
                }
            } else {
                PetMenu.open(player, plugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.contains("Pet Dashboard")) {
            PetMenu.handleClick(event, plugin);
        } else if (title.contains("Pilih Kosmetik")) {
            CosmeticMenu.handleClick(event, plugin);
        }
    }
}
