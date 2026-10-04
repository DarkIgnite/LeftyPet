package com.leftycraft.leftypet.model;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class PetAltar {

    private final UUID altarId;
    private final UUID ownerUuid;
    private String cachedOwnerName;
    private final Location location;
    private int altarLevel;
    private int remainingSeconds;
    private int targetLevel;
    private boolean isTraining;

    // Transient entities in world
    private transient ItemDisplay floatingDisplay;
    private transient ArmorStand bedrockStand;
    private transient TextDisplay hologramDisplay;
    private transient String lastRenderedText;

    public PetAltar(UUID altarId, UUID ownerUuid, String cachedOwnerName, Location location, int altarLevel, int remainingSeconds, int targetLevel, boolean isTraining) {
        this.altarId = altarId;
        this.ownerUuid = ownerUuid;
        this.cachedOwnerName = cachedOwnerName;
        this.location = location;
        this.altarLevel = Math.max(1, Math.min(4, altarLevel));
        this.remainingSeconds = Math.max(0, remainingSeconds);
        this.targetLevel = targetLevel;
        this.isTraining = isTraining;
    }

    public PetAltar(UUID altarId, UUID ownerUuid, Location location, int altarLevel, int remainingSeconds, int targetLevel, boolean isTraining) {
        this(altarId, ownerUuid, null, location, altarLevel, remainingSeconds, targetLevel, isTraining);
    }

    public PetAltar(UUID altarId, UUID ownerUuid, Location location, int altarLevel, long finishTimestamp, int targetLevel, boolean isTraining) {
        this(altarId, ownerUuid, null, location, altarLevel, (int) Math.max(0, (finishTimestamp - System.currentTimeMillis()) / 1000L), targetLevel, isTraining);
    }

    public UUID getAltarId() {
        return altarId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public Location getLocation() {
        return location;
    }

    public int getAltarLevel() {
        return altarLevel;
    }

    public void setAltarLevel(int altarLevel) {
        this.altarLevel = Math.max(1, Math.min(4, altarLevel));
    }

    public double getTimeReductionPercent() {
        return switch (altarLevel) {
            case 2 -> 10.0;
            case 3 -> 20.0;
            case 4 -> 50.0;
            default -> 0.0;
        };
    }

    public double getTimeMultiplier() {
        return 1.0 - (getTimeReductionPercent() / 100.0);
    }

    public long getFinishTimestamp() {
        return System.currentTimeMillis() + (remainingSeconds * 1000L);
    }

    public void setFinishTimestamp(long finishTimestamp) {
        this.remainingSeconds = (int) Math.max(0, (finishTimestamp - System.currentTimeMillis()) / 1000L);
    }

    public int getRemainingSeconds() {
        if (!isTraining) return 0;
        return Math.max(0, remainingSeconds);
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = Math.max(0, remainingSeconds);
    }

    public void decrementRemainingSeconds() {
        if (this.remainingSeconds > 0) {
            this.remainingSeconds--;
        }
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public boolean isTraining() {
        return isTraining;
    }

    public void setTraining(boolean training) {
        isTraining = training;
    }

    public boolean isFinished() {
        return isTraining && remainingSeconds <= 0;
    }

    public String getFormattedRemainingTime() {
        if (!isTraining) return "&7Tidak ada pet";
        int seconds = getRemainingSeconds();
        if (seconds <= 0) {
            return "&a&lSELESAI!";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int sec = seconds % 60;
        if (hours >= 24) {
            int days = hours / 24;
            hours = hours % 24;
            return String.format("%dh %02dj %02dm %02dd", days, hours, minutes, sec);
        }
        if (hours > 0) {
            return String.format("%02dj %02dm %02dd", hours, minutes, sec);
        }
        return String.format("%02dm %02dd", minutes, sec);
    }

    public ItemDisplay getFloatingDisplay() {
        return floatingDisplay;
    }

    public void setFloatingDisplay(ItemDisplay floatingDisplay) {
        this.floatingDisplay = floatingDisplay;
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

    public String getCachedOwnerName() {
        return cachedOwnerName;
    }

    public void setCachedOwnerName(String cachedOwnerName) {
        this.cachedOwnerName = cachedOwnerName;
    }

    public String getLastRenderedText() {
        return lastRenderedText;
    }

    public void setLastRenderedText(String lastRenderedText) {
        this.lastRenderedText = lastRenderedText;
    }

    public void removeEntities() {
        if (floatingDisplay != null && floatingDisplay.isValid()) {
            floatingDisplay.remove();
        }
        if (bedrockStand != null && bedrockStand.isValid()) {
            bedrockStand.remove();
        }
        if (hologramDisplay != null && hologramDisplay.isValid()) {
            hologramDisplay.remove();
        }
        floatingDisplay = null;
        bedrockStand = null;
        hologramDisplay = null;
        lastRenderedText = null;
    }
}
