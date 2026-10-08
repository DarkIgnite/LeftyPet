package com.leftycraft.leftypet.model;

import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PetKitchen {

    public enum KitchenStation {
        COOKING("Memasak di Furnace", 30),
        PACKING("Mengemas di Meja", 30),
        DELIVERY("Menyerahkan di Jendela", 5),
        TIRED("Istirahat (Lapar / Habis Energi)", 0);

        private final String displayName;
        private final int durationSeconds;

        KitchenStation(String displayName, int durationSeconds) {
            this.displayName = displayName;
            this.durationSeconds = durationSeconds;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }
    }

    private final UUID kitchenId;
    private final UUID ownerUuid;
    private String cachedOwnerName;
    private final Location location; // Entrance anchor block
    private StructureRotation rotation;
    private String facing;
    private final Set<Location> allBlockLocations = new HashSet<>();

    private boolean isPetAssigned;
    private double storedEarnings;
    private int completedOrders;
    private int currentProgressSeconds;
    private KitchenStation currentStation;

    // Transient Display Entities
    private transient ItemDisplay chefDisplay;
    private transient ArmorStand bedrockStand;
    private transient Interaction interactionEntity;
    private transient TextDisplay hologramDisplay;
    private transient TextDisplay cashierDisplay;
    private transient String lastRenderedText;
    private transient boolean isGliding;

    public PetKitchen(UUID kitchenId, UUID ownerUuid, String cachedOwnerName, Location location,
                      StructureRotation rotation, String facing, boolean isPetAssigned,
                      double storedEarnings, int completedOrders, int currentProgressSeconds,
                      KitchenStation currentStation) {
        this.kitchenId = kitchenId;
        this.ownerUuid = ownerUuid;
        this.cachedOwnerName = cachedOwnerName;
        this.location = location;
        this.rotation = (rotation != null) ? rotation : StructureRotation.NONE;
        this.facing = (facing != null) ? facing : "WEST";
        this.isPetAssigned = isPetAssigned;
        this.storedEarnings = Math.max(0.0, storedEarnings);
        this.completedOrders = Math.max(0, completedOrders);
        this.currentProgressSeconds = Math.max(0, currentProgressSeconds);
        this.currentStation = (currentStation != null) ? currentStation : KitchenStation.COOKING;
    }

    public PetKitchen(UUID kitchenId, UUID ownerUuid, String cachedOwnerName, Location location,
                      StructureRotation rotation, String facing) {
        this(kitchenId, ownerUuid, cachedOwnerName, location, rotation, facing, false, 0.0, 0, 0, KitchenStation.COOKING);
    }

    public UUID getKitchenId() {
        return kitchenId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getCachedOwnerName() {
        return cachedOwnerName;
    }

    public void setCachedOwnerName(String cachedOwnerName) {
        this.cachedOwnerName = cachedOwnerName;
    }

    public Location getLocation() {
        return location;
    }

    public StructureRotation getRotation() {
        return rotation;
    }

    public void setRotation(StructureRotation rotation) {
        this.rotation = rotation;
    }

    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }

    public Set<Location> getAllBlockLocations() {
        return allBlockLocations;
    }

    public void setAllBlockLocations(Set<Location> locs) {
        allBlockLocations.clear();
        if (locs != null) {
            allBlockLocations.addAll(locs);
        }
    }

    public boolean isPetAssigned() {
        return isPetAssigned;
    }

    public void setPetAssigned(boolean petAssigned) {
        isPetAssigned = petAssigned;
    }

    public double getStoredEarnings() {
        return storedEarnings;
    }

    public void setStoredEarnings(double storedEarnings) {
        this.storedEarnings = Math.max(0.0, storedEarnings);
    }

    public void addEarnings(double amount) {
        this.storedEarnings += Math.max(0.0, amount);
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = Math.max(0, completedOrders);
    }

    public void incrementCompletedOrders() {
        this.completedOrders++;
    }

    public int getCurrentProgressSeconds() {
        return currentProgressSeconds;
    }

    public void setCurrentProgressSeconds(int currentProgressSeconds) {
        this.currentProgressSeconds = Math.max(0, currentProgressSeconds);
    }

    public KitchenStation getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(KitchenStation currentStation) {
        this.currentStation = currentStation;
    }

    public ItemDisplay getChefDisplay() {
        return chefDisplay;
    }

    public void setChefDisplay(ItemDisplay chefDisplay) {
        this.chefDisplay = chefDisplay;
    }

    public ArmorStand getBedrockStand() {
        return bedrockStand;
    }

    public void setBedrockStand(ArmorStand bedrockStand) {
        this.bedrockStand = bedrockStand;
    }

    public TextDisplay getHologramDisplay() {
        return hologramDisplay;
    }

    public void setHologramDisplay(TextDisplay hologramDisplay) {
        this.hologramDisplay = hologramDisplay;
    }

    public TextDisplay getCashierDisplay() {
        return cashierDisplay;
    }

    public void setCashierDisplay(TextDisplay cashierDisplay) {
        this.cashierDisplay = cashierDisplay;
    }

    public String getLastRenderedText() {
        return lastRenderedText;
    }

    public void setLastRenderedText(String lastRenderedText) {
        this.lastRenderedText = lastRenderedText;
    }

    public boolean isGliding() {
        return isGliding;
    }

    public void setGliding(boolean gliding) {
        isGliding = gliding;
    }

    public Interaction getInteractionEntity() {
        return interactionEntity;
    }

    public void setInteractionEntity(Interaction interactionEntity) {
        this.interactionEntity = interactionEntity;
    }

    public void removeEntities() {
        if (chefDisplay != null) {
            try { chefDisplay.remove(); } catch (Throwable ignored) {}
        }
        if (bedrockStand != null) {
            try { bedrockStand.remove(); } catch (Throwable ignored) {}
        }
        if (interactionEntity != null) {
            try { interactionEntity.remove(); } catch (Throwable ignored) {}
        }
        if (hologramDisplay != null) {
            try { hologramDisplay.remove(); } catch (Throwable ignored) {}
        }
        if (cashierDisplay != null) {
            try { cashierDisplay.remove(); } catch (Throwable ignored) {}
        }
        chefDisplay = null;
        bedrockStand = null;
        interactionEntity = null;
        hologramDisplay = null;
        cashierDisplay = null;
        lastRenderedText = null;
    }
}
