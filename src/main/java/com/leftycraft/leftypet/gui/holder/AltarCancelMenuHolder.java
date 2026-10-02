package com.leftycraft.leftypet.gui.holder;

import com.leftycraft.leftypet.model.PetAltar;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class AltarCancelMenuHolder implements InventoryHolder {
    private final PetAltar altar;
    private Inventory inventory;

    public AltarCancelMenuHolder(PetAltar altar) {
        this.altar = altar;
    }

    public PetAltar getAltar() {
        return altar;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
