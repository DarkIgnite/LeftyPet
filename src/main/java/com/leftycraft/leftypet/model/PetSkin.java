package com.leftycraft.leftypet.model;

public class PetSkin {
    private final String id;
    private final String displayName;
    private final String texture;

    public PetSkin(String id, String displayName, String texture) {
        this.id = id;
        this.displayName = displayName;
        this.texture = texture;
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
}
