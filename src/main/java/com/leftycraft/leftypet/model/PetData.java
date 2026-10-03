package com.leftycraft.leftypet.model;

import java.util.UUID;

public class PetData {

    private final UUID ownerUuid;
    private String name;
    private int level;
    private double energy;
    private PetClass petClass;
    private String skinKey;
    private String trailKey;
    private boolean isSummoned;
    private boolean isTraining;
    private UUID currentAltarId;
    private String lastAltarClaimDate;
    private int dailyAltarClaims;
    private boolean autoAttack;
    private long upgradeCooldownUntil;

    public PetData(UUID ownerUuid) {
        this(ownerUuid, null);
    }

    public PetData(UUID ownerUuid, String ownerName) {
        this.ownerUuid = ownerUuid;
        this.name = (ownerName != null && !ownerName.isBlank())
                ? ownerName + "'s Companion"
                : "Spirit Companion";
        this.level = 1;
        this.energy = 100.0;
        this.petClass = PetClass.FIGHTER;
        this.skinKey = "spirit";
        this.trailKey = "FLAME";
        this.isSummoned = false;
        this.isTraining = false;
        this.currentAltarId = null;
        this.lastAltarClaimDate = "";
        this.dailyAltarClaims = 0;
        this.autoAttack = true;
        this.upgradeCooldownUntil = 0L;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = Math.max(0.0, Math.min(100.0, energy));
    }

    public boolean isFainted() {
        return energy <= 0.001;
    }

    public void addEnergy(double amount) {
        setEnergy(this.energy + amount);
    }

    public void drainEnergy(double amount) {
        setEnergy(this.energy - amount);
    }

    public PetClass getPetClass() {
        return petClass;
    }

    public void setPetClass(PetClass petClass) {
        this.petClass = petClass;
    }

    public String getSkinKey() {
        return skinKey;
    }

    public void setSkinKey(String skinKey) {
        this.skinKey = skinKey;
    }

    public String getTrailKey() {
        return trailKey;
    }

    public void setTrailKey(String trailKey) {
        this.trailKey = trailKey;
    }

    public boolean isSummoned() {
        return isSummoned;
    }

    public void setSummoned(boolean summoned) {
        isSummoned = summoned;
    }

    public boolean isTraining() {
        return isTraining;
    }

    public void setTraining(boolean training) {
        isTraining = training;
    }

    public UUID getCurrentAltarId() {
        return currentAltarId;
    }

    public void setCurrentAltarId(UUID currentAltarId) {
        this.currentAltarId = currentAltarId;
    }

    public String getLastAltarClaimDate() {
        return lastAltarClaimDate;
    }

    public void setLastAltarClaimDate(String lastAltarClaimDate) {
        this.lastAltarClaimDate = lastAltarClaimDate;
    }

    public int getDailyAltarClaims() {
        return dailyAltarClaims;
    }

    public void setDailyAltarClaims(int dailyAltarClaims) {
        this.dailyAltarClaims = dailyAltarClaims;
    }

    public boolean isAutoAttack() {
        return autoAttack;
    }

    public void setAutoAttack(boolean autoAttack) {
        this.autoAttack = autoAttack;
    }

    public long getUpgradeCooldownUntil() {
        return upgradeCooldownUntil;
    }

    public void setUpgradeCooldownUntil(long upgradeCooldownUntil) {
        this.upgradeCooldownUntil = upgradeCooldownUntil;
    }

    public boolean isUpgradeOnCooldown() {
        return System.currentTimeMillis() < upgradeCooldownUntil;
    }

    public int getUpgradeCooldownRemainingSeconds() {
        return Math.max(0, (int) ((upgradeCooldownUntil - System.currentTimeMillis()) / 1000L));
    }

    /**
     * Calculates base attack damage based on level and class.
     */
    public double getAttackDamage(double classMultiplier) {
        double base = 2.0 + (level * 0.6);
        return base * classMultiplier;
    }

    /**
     * Formats the energy bar into an ASCII progress bar (e.g. [||||||||||]).
     */
    public String getEnergyProgressBar() {
        int totalBars = 10;
        int activeBars = (int) Math.round((energy / 100.0) * totalBars);
        StringBuilder sb = new StringBuilder();
        if (energy > 50) {
            sb.append("<green>");
        } else if (energy > 20) {
            sb.append("<yellow>");
        } else {
            sb.append("<red>");
        }
        for (int i = 0; i < totalBars; i++) {
            if (i < activeBars) {
                sb.append("|");
            } else {
                if (i == activeBars) sb.append("<dark_gray>");
                sb.append("|");
            }
        }
        return sb.toString();
    }
}
