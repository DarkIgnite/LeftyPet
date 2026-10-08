package com.leftycraft.leftypet.gui.holder;

import com.leftycraft.leftypet.model.PetKitchen;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class KitchenMenuHolder implements InventoryHolder {
    private final PetKitchen kitchen;
    private Inventory inventory;

    public KitchenMenuHolder(PetKitchen kitchen) {
        this.kitchen = kitchen;
    }

    public PetKitchen getKitchen() {
        return kitchen;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
