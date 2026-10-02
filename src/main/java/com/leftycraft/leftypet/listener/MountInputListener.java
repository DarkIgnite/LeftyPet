package com.leftycraft.leftypet.listener;

import com.leftycraft.leftypet.LeftyPetPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;

public class MountInputListener implements Listener {

    private final LeftyPetPlugin plugin;

    public MountInputListener(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInput(PlayerInputEvent event) {
        plugin.getMountManager().handleInput(event.getPlayer(), event.getInput());
    }
}
