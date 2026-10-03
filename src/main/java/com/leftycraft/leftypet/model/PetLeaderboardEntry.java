package com.leftycraft.leftypet.model;

import java.util.UUID;

public record PetLeaderboardEntry(
        UUID playerUuid,
        String playerName,
        int level,
        String petName,
        PetClass petClass,
        String skinKey
) implements Comparable<PetLeaderboardEntry> {

    @Override
    public int compareTo(PetLeaderboardEntry o) {
        return Integer.compare(o.level, this.level); // Descending
    }
}
