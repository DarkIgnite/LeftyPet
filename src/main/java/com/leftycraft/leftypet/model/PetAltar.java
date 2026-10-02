package com.leftycraft.leftypet.model;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class PetAltar {

    private final UUID altarId;
    private final UUID ownerUuid;
    private final Location location;
    private int altarLevel;
    private int remainingSeconds;
    private int targetLevel;
    private boolean isTraining;

    // Transient entities in world
    private transient ItemDisplay floatingDisplay;
    private transient TextDisplay hologramDisplay;

    public PetAltar(UUID altarId, UUID ownerUuid, Location location, int altarLevel, int remainingSeconds, int targetLevel, boolean isTraining) {
        this.altarId = altarId;
        this.ownerUuid = ownerUuid;
        this.location = location;
        this.altarLevel = Math.max(1, Math.min(3, altarLevel));
        this.remainingSeconds = Math.max(0, remainingSeconds);
        this.targetLevel = targetLevel;
        this.isTraining = isTraining;
    }

    public PetAltar(UUID altarId, UUID ownerUuid, Location location, int altarLevel, long finishTimestamp, int targetLevel, boolean isTraining) {
        this(altarId, ownerUuid, location, altarLevel, (int) Math.max(0, (finishTimestamp - System.currentTimeMillis()) / 1000L), targetLevel, isTraining);
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
        this.altarLevel = Math.max(1, Math.min(3, altarLevel));
    }

    public double getTimeReductionPercent() {
        return switch (altarLevel) {
            case 2 -> 20.0;
            case 3 -> 30.0;
            default -> 10.0;
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
        long minutes = seconds / 60;
        long sec = seconds % 60;
        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02dh %02dm %02ds", hours, minutes, sec);
        }
        return String.format("%02dm %02ds", minutes, sec);
    }

    public ItemDisplay getFloatingDisplay() {
        return floatingDisplay;
    }

    public void setFloatingDisplay(ItemDisplay floatingDisplay) {
        this.floatingDisplay = floatingDisplay;
    }

    public TextDisplay getHologramDisplay() {
        return hologramDisplay;
    }

    public void setHologramDisplay(TextDisplay hologramDisplay) {
        this.hologramDisplay = hologramDisplay;
    }

    public void removeEntities() {
        if (floatingDisplay != null && floatingDisplay.isValid()) {
            floatingDisplay.remove();
        }
        if (hologramDisplay != null && hologramDisplay.isValid()) {
            hologramDisplay.remove();
        }
        floatingDisplay = null;
        hologramDisplay = null;
    }
}
