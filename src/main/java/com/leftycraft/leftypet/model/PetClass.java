package com.leftycraft.leftypet.model;

public enum PetClass {
    FIGHTER("<gradient:#ff416c:#ff4b2b><b>ғɪɢʜᴛᴇʀ</b></gradient>", "DIAMOND_SWORD"),
    SUPPORT("<gradient:#11998e:#38ef7d><b>sᴜᴘᴘᴏʀᴛ</b></gradient>", "GOLDEN_APPLE"),
    LOOTER("<gradient:#f7971e:#ffd200><b>ʟᴏᴏᴛᴇʀ</b></gradient>", "HOPPER"),
    TRAVELER("<gradient:#00c6ff:#0072ff><b>ᴛʀᴀᴠᴇʟᴇʀ</b></gradient>", "SADDLE");

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
