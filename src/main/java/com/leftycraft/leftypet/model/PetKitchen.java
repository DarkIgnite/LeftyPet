package com.leftycraft.leftypet.model;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class PetKitchen {

    public enum KitchenStation {
        PREPARING("🥩 Menyiapkan Bahan"),
        COOKING("🍳 Memasak di Kompor"),
        PACKING("🍱 Mengemas Box MBG");

        private final String displayName;

        KitchenStation(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final UUID kitchenId;
    private final UUID ownerUuid;
    private String cachedOwnerName;
    private final Location location;
    private int kitchenLevel;
    private int cookedPortions;
    private int currentProgressSeconds;
    private KitchenStation currentStation;
    private boolean isCooking;

    // Transient Display Entities
    private transient ItemDisplay chefDisplay;
    private transient ArmorStand bedrockStand;
    private transient TextDisplay hologramDisplay;
    private transient String lastRenderedText;

    public PetKitchen(UUID kitchenId, UUID ownerUuid, String cachedOwnerName, Location location, int kitchenLevel, int cookedPortions, int currentProgressSeconds, KitchenStation currentStation, boolean isCooking) {
        this.kitchenId = kitchenId;
        this.ownerUuid = ownerUuid;
        this.cachedOwnerName = cachedOwnerName;
        this.location = location;
        this.kitchenLevel = Math.max(1, Math.min(3, kitchenLevel));
        this.cookedPortions = Math.max(0, cookedPortions);
        this.currentProgressSeconds = Math.max(0, currentProgressSeconds);
        this.currentStation = (currentStation != null) ? currentStation : KitchenStation.PREPARING;
        this.isCooking = isCooking;
    }

    public PetKitchen(UUID kitchenId, UUID ownerUuid, String cachedOwnerName, Location location, int kitchenLevel) {
        this(kitchenId, ownerUuid, cachedOwnerName, location, kitchenLevel, 0, 0, KitchenStation.PREPARING, true);
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

    public int getKitchenLevel() {
        return kitchenLevel;
    }

    public void setKitchenLevel(int kitchenLevel) {
        this.kitchenLevel = Math.max(1, Math.min(3, kitchenLevel));
    }

    public int getCookedPortions() {
        return cookedPortions;
    }

    public void setCookedPortions(int cookedPortions) {
        this.cookedPortions = Math.max(0, cookedPortions);
    }

    public void addCookedPortion() {
        this.cookedPortions = Math.min(getMaxCapacity(), this.cookedPortions + 1);
    }

    public int getMaxCapacity() {
        return switch (kitchenLevel) {
            case 3 -> 30;
            case 2 -> 20;
            default -> 10;
        };
    }

    public int getRewardPerPortion() {
        return switch (kitchenLevel) {
            case 3 -> 1000;
            case 2 -> 500;
            default -> 250;
        };
    }

    public int getCurrentProgressSeconds() {
        return currentProgressSeconds;
    }

    public void setCurrentProgressSeconds(int currentProgressSeconds) {
        this.currentProgressSeconds = currentProgressSeconds;
    }

    public KitchenStation getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(KitchenStation currentStation) {
        this.currentStation = currentStation;
    }

    public boolean isCooking() {
        return isCooking;
    }

    public void setCooking(boolean cooking) {
        isCooking = cooking;
    }

    public boolean isStorageFull() {
        return cookedPortions >= getMaxCapacity();
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

    public String getLastRenderedText() {
        return lastRenderedText;
    }

    public void setLastRenderedText(String lastRenderedText) {
        this.lastRenderedText = lastRenderedText;
    }

    public void removeEntities() {
        if (chefDisplay != null && chefDisplay.isValid()) {
            chefDisplay.remove();
        }
        if (bedrockStand != null && bedrockStand.isValid()) {
            bedrockStand.remove();
        }
        if (hologramDisplay != null && hologramDisplay.isValid()) {
            hologramDisplay.remove();
        }
        chefDisplay = null;
        bedrockStand = null;
        hologramDisplay = null;
        lastRenderedText = null;
    }
}
