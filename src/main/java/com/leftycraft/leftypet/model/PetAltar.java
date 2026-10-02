package com.leftycraft.leftypet.model;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class PetAltar {

    private final UUID altarId;
    private final UUID ownerUuid;
    private final Location location;
    private long finishTimestamp;
    private int targetLevel;

    // Transient entities in world
    private transient ItemDisplay floatingDisplay;
    private transient TextDisplay hologramDisplay;

    public PetAltar(UUID altarId, UUID ownerUuid, Location location, long finishTimestamp, int targetLevel) {
        this.altarId = altarId;
        this.ownerUuid = ownerUuid;
        this.location = location;
        this.finishTimestamp = finishTimestamp;
        this.targetLevel = targetLevel;
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

    public long getFinishTimestamp() {
        return finishTimestamp;
    }

    public void setFinishTimestamp(long finishTimestamp) {
        this.finishTimestamp = finishTimestamp;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public boolean isFinished() {
        return System.currentTimeMillis() >= finishTimestamp;
    }

    public long getRemainingSeconds() {
        return Math.max(0, (finishTimestamp - System.currentTimeMillis()) / 1000);
    }

    public String getFormattedRemainingTime() {
        long seconds = getRemainingSeconds();
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
