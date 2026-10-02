package com.leftycraft.leftypet.model;

public enum PetClass {
    FIGHTER("&c&lFIGHTER", "DIAMOND_SWORD"),
    SUPPORT("&a&lSUPPORT", "GOLDEN_APPLE"),
    LOOTER("&6&lLOOTER", "HOPPER"),
    TRAVELER("&b&lTRAVELER", "SADDLE");

    private final String displayName;
    private final String iconMaterial;

    PetClass(String displayName, String iconMaterial) {
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }
}
