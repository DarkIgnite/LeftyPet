package com.leftycraft.leftypet.model;

public class PetSkin {
    private final String id;
    private final String displayName;
    private final String texture;
    private final String requiredPermission; // null = no restriction

    public PetSkin(String id, String displayName, String texture) {
        this(id, displayName, texture, null);
    }

    public PetSkin(String id, String displayName, String texture, String requiredPermission) {
        this.id = id;
        this.displayName = displayName;
        this.texture = texture;
        this.requiredPermission = requiredPermission;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTexture() {
        return texture;
    }

    /** Returns null if anyone can use this skin, otherwise the permission node required. */
    public String getRequiredPermission() {
        return requiredPermission;
    }

    public boolean hasPermission() {
        return requiredPermission != null && !requiredPermission.isBlank();
    }
}
