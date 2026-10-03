package com.leftycraft.leftypet.manager;

import com.leftycraft.leftypet.LeftyPetPlugin;
import com.leftycraft.leftypet.entity.ActivePet;
import com.leftycraft.leftypet.model.PetClass;
import com.leftycraft.leftypet.model.PetData;
import com.leftycraft.leftypet.util.ColorUtil;
import com.leftycraft.leftypet.util.PetSoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PetDuelManager {

    private final LeftyPetPlugin plugin;
    private final Map<UUID, DuelInvite> pendingInvites = new ConcurrentHashMap<>();
    private final Map<UUID, ActiveDuel> activeDuels = new ConcurrentHashMap<>();

    public record DuelInvite(UUID challenger, UUID target, double bet, long timestamp) {
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 60000L; // 60s
        }
    }

    public static class ActiveDuel {
        final UUID id = UUID.randomUUID();
        final Player playerA;
        final Player playerB;
        final ActivePet petA;
        final ActivePet petB;
        final double bet;
        final Location centerLoc;
        int hpA = 100;
        int hpB = 100;
        int round = 0;
        BukkitTask task;

        public ActiveDuel(Player playerA, Player playerB, ActivePet petA, ActivePet petB, double bet, Location centerLoc) {
            this.playerA = playerA;
            this.playerB = playerB;
            this.petA = petA;
            this.petB = petB;
            this.bet = bet;
            this.centerLoc = centerLoc;
        }
    }

    public PetDuelManager(LeftyPetPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInDuel(UUID playerUuid) {
        return activeDuels.containsKey(playerUuid);
    }

    public void sendChallenge(Player challenger, Player target, double bet) {
        if (challenger.equals(target)) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Kamu tidak bisa menantang dirimu sendiri!</red>"));
            return;
        }

        ActivePet petA = plugin.getPetManager().getActivePet(challenger.getUniqueId());
        ActivePet petB = plugin.getPetManager().getActivePet(target.getUniqueId());

        if (petA == null || !petA.isValid() || petA.getData().isFainted() || petA.getData().isTraining()) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Pet kamu harus aktif dan tidak sedang pingsan/di altar untuk berduel!</red>"));
            return;
        }

        if (petB == null || !petB.isValid() || petB.getData().isFainted() || petB.getData().isTraining()) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Pet milik " + target.getName() + " sedang tidak aktif atau sedang pingsan/di altar!</red>"));
            return;
        }

        if (!challenger.getWorld().equals(target.getWorld()) || challenger.getLocation().distanceSquared(target.getLocation()) > 400.0) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Target terlalu jauh! Dekati target untuk menantang duel (maks 20 blok).</red>"));
            return;
        }

        if (isInDuel(challenger.getUniqueId()) || isInDuel(target.getUniqueId())) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Salah satu pemain sedang berada di dalam duel!</red>"));
            return;
        }

        if (bet > 0.0) {
            if (!plugin.getEconomyManager().hasEconomy()) {
                challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Sistem ekonomi server tidak aktif. Duel hanya bisa tanpa taruhan.</red>"));
                return;
            }
            if (!plugin.getEconomyManager().hasEnough(challenger, bet)) {
                challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Uangmu tidak cukup untuk memasang taruhan ini!</red>"));
                return;
            }
            if (!plugin.getEconomyManager().hasEnough(target, bet)) {
                challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Target tidak memiliki uang yang cukup untuk taruhan ini!</red>"));
                return;
            }
        }

        DuelInvite invite = new DuelInvite(challenger.getUniqueId(), target.getUniqueId(), bet, System.currentTimeMillis());
        pendingInvites.put(target.getUniqueId(), invite);

        String betStr = (bet > 0.0) ? " <gold>($" + plugin.getEconomyManager().format(bet) + ")</gold>" : "";
        challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") +
                "<yellow>Tantangan duel pet berhasil dikirim ke <white>" + target.getName() + "</white>" + betStr + "! Menunggu respon...</yellow>"));
        challenger.playSound(challenger.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);

        target.sendMessage(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
        target.sendMessage(ColorUtil.component("<gradient:#ff5f6d:#ffc371><b>⚔ TANTANGAN DUEL PET! ⚔</b></gradient>"));
        target.sendMessage(ColorUtil.component("<white>" + challenger.getName() + "</white> <yellow>menantang pet milikmu untuk bertarung!" + betStr + "</yellow>"));
        target.sendMessage(ColorUtil.component("<green>Ketik <click:run_command:'/pet duel accept'><yellow><b>/pet duel accept</b></yellow></click> untuk menerima!</green>"));
        target.sendMessage(ColorUtil.component("<red>Ketik <click:run_command:'/pet duel decline'><yellow><b>/pet duel decline</b></yellow></click> untuk menolak.</red>"));
        target.sendMessage(ColorUtil.component("<dark_gray>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯</dark_gray>"));
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
    }

    public void acceptChallenge(Player target) {
        DuelInvite invite = pendingInvites.remove(target.getUniqueId());
        if (invite == null || invite.isExpired()) {
            target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Tidak ada tantangan duel yang aktif atau sudah kedaluwarsa.</red>"));
            return;
        }

        Player challenger = Bukkit.getPlayer(invite.challenger());
        if (challenger == null || !challenger.isOnline()) {
            target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Pemain yang menantang sudah offline.</red>"));
            return;
        }

        ActivePet petA = plugin.getPetManager().getActivePet(challenger.getUniqueId());
        ActivePet petB = plugin.getPetManager().getActivePet(target.getUniqueId());

        if (petA == null || !petA.isValid() || petB == null || !petB.isValid()) {
            target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Kedua pet harus aktif untuk memulai duel!</red>"));
            return;
        }

        if (challenger.getLocation().distanceSquared(target.getLocation()) > 400.0) {
            target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Kalian terlalu jauh untuk memulai duel! Dekati lawan.</red>"));
            return;
        }

        double bet = invite.bet();
        if (bet > 0.0) {
            if (!plugin.getEconomyManager().hasEnough(challenger, bet) || !plugin.getEconomyManager().hasEnough(target, bet)) {
                target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Salah satu pemain tidak memiliki cukup uang taruhan saat ini!</red>"));
                challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Duel dibatalkan karena saldo taruhan tidak mencukupi.</red>"));
                return;
            }
            plugin.getEconomyManager().withdraw(challenger, bet);
            plugin.getEconomyManager().withdraw(target, bet);
        }

        startDuel(challenger, target, petA, petB, bet);
    }

    public void declineChallenge(Player target) {
        DuelInvite invite = pendingInvites.remove(target.getUniqueId());
        if (invite == null) {
            target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>Tidak ada tantangan duel untuk ditolak.</red>"));
            return;
        }

        target.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<yellow>Tantangan duel ditolak.</yellow>"));
        Player challenger = Bukkit.getPlayer(invite.challenger());
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + "<red>" + target.getName() + " menolak tantangan duel pet kamu.</red>"));
            challenger.playSound(challenger.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
        }
    }

    private void startDuel(Player playerA, Player playerB, ActivePet petA, ActivePet petB, double bet) {
        // Calculate center between players
        Location center = playerA.getLocation().clone().add(playerB.getLocation()).multiply(0.5);
        ActiveDuel duel = new ActiveDuel(playerA, playerB, petA, petB, bet, center);
        activeDuels.put(playerA.getUniqueId(), duel);
        activeDuels.put(playerB.getUniqueId(), duel);

        // Countdown 3.. 2.. 1.. FIGHT!
        broadcastDuel(duel, "<gradient:#ff5f6d:#ffc371><b>⚔ PERSIAPAN DUEL PET! ⚔</b></gradient>",
                "<yellow><b>" + petA.getData().getName() + "</b></yellow> <gray>VS</gray> <yellow><b>" + petB.getData().getName() + "</b></yellow>");

        final int[] countdown = {3};
        BukkitTask timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!playerA.isOnline() || !playerB.isOnline() || !petA.isValid() || !petB.isValid()) {
                cancelDuel(duel, "Pemain terputus atau pet tidak valid.");
                return;
            }

            if (countdown[0] > 0) {
                String sub = "<gold>Duel dimulai dalam <b>" + countdown[0] + "...</b></gold>";
                playerA.sendTitle(ColorUtil.colorize("&e&l" + countdown[0]), ColorUtil.colorize(sub), 0, 20, 5);
                playerB.sendTitle(ColorUtil.colorize("&e&l" + countdown[0]), ColorUtil.colorize(sub), 0, 20, 5);
                playerA.playSound(playerA.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (3 - countdown[0]) * 0.2f);
                playerB.playSound(playerB.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (3 - countdown[0]) * 0.2f);
                countdown[0]--;
            } else {
                playerA.sendTitle(ColorUtil.colorize("&c&l⚔ FIGHT! ⚔"), ColorUtil.colorize("&eSemoga pet terbaik yang menang!"), 0, 25, 10);
                playerB.sendTitle(ColorUtil.colorize("&c&l⚔ FIGHT! ⚔"), ColorUtil.colorize("&eSemoga pet terbaik yang menang!"), 0, 25, 10);
                playerA.playSound(playerA.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);
                playerB.playSound(playerB.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);
                duel.task.cancel();
                startCombatRounds(duel);
            }
        }, 0L, 20L);

        duel.task = timerTask;
    }

    private void startCombatRounds(ActiveDuel duel) {
        duel.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!duel.playerA.isOnline() || !duel.playerB.isOnline() || !duel.petA.isValid() || !duel.petB.isValid()) {
                cancelDuel(duel, "Pemain logout atau pet despawn selama duel.");
                return;
            }

            duel.round++;

            // Alternate attack or simultaneous strike
            boolean aHitsB = (duel.round % 2 != 0);

            if (aHitsB) {
                executeDuelAttack(duel, duel.playerA, duel.petA, duel.playerB, duel.petB, true);
            } else {
                executeDuelAttack(duel, duel.playerB, duel.petB, duel.playerA, duel.petA, false);
            }

            // Update nametags with duel HP bar
            updateDuelNameTag(duel.petA, duel.hpA);
            updateDuelNameTag(duel.petB, duel.hpB);

            // Check victory condition
            if (duel.hpB <= 0 || duel.hpA <= 0) {
                finishDuel(duel);
            }
        }, 10L, 16L); // Attack every 16 ticks (~0.8s)
    }

    private void executeDuelAttack(ActiveDuel duel, Player attackerPlayer, ActivePet attacker, Player defenderPlayer, ActivePet defender, boolean attackerIsA) {
        Location aLoc = attacker.getDisplayEntity().getLocation();
        Location dLoc = defender.getDisplayEntity().getLocation();

        PetData aData = attacker.getData();
        PetClass pClass = aData.getPetClass();

        // Base damage calculation: 14 + random(8) + level bonus
        int baseDmg = 14 + ThreadLocalRandom.current().nextInt(9) + (int) (aData.getLevel() * 0.12);

        // Class perks in Duel:
        switch (pClass) {
            case FIGHTER -> {
                baseDmg += 5;
                dLoc.getWorld().spawnParticle(Particle.CRIT, dLoc.clone().add(0, 0.3, 0), 12, 0.3, 0.3, 0.3, 0.1);
            }
            case SUPPORT -> {
                // Heal self 4 HP
                if (attackerIsA) duel.hpA = Math.min(100, duel.hpA + 4);
                else duel.hpB = Math.min(100, duel.hpB + 4);
                aLoc.getWorld().spawnParticle(Particle.HEART, aLoc.clone().add(0, 0.4, 0), 3, 0.2, 0.2, 0.2, 0.02);
            }
            case LOOTER -> {
                baseDmg += 2;
                dLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, dLoc.clone().add(0, 0.3, 0), 10, 0.2, 0.2, 0.2, 0.08);
            }
            case TRAVELER -> {
                baseDmg += 3;
                dLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, dLoc.clone().add(0, 0.3, 0), 12, 0.3, 0.3, 0.3, 0.15);
            }
        }

        // Apply skin sound
        PetSoundUtil.playCombatSound(dLoc, aData.getSkinKey());

        // Laser beam / slash particle from attacker to defender
        spawnLaserBeam(aLoc.clone().add(0, 0.2, 0), dLoc.clone().add(0, 0.2, 0));

        // Apply damage to defender
        if (attackerIsA) {
            duel.hpB = Math.max(0, duel.hpB - baseDmg);
        } else {
            duel.hpA = Math.max(0, duel.hpA - baseDmg);
        }

        // Sound on hit
        defenderPlayer.playSound(dLoc, Sound.ENTITY_PLAYER_HURT, 0.6f, 1.2f);
        attackerPlayer.playSound(aLoc, Sound.ENTITY_ARROW_HIT_PLAYER, 0.6f, 1.2f);
    }

    private void updateDuelNameTag(ActivePet pet, int hp) {
        if (pet.getNameTagDisplay() == null || !pet.getNameTagDisplay().isValid()) return;
        String bar = getDuelProgressBar(hp, 100);
        String text = "<yellow><b>" + pet.getData().getName() + "</b></yellow>\n" +
                bar + " <white>" + hp + "/100 HP</white>";
        pet.getNameTagDisplay().text(ColorUtil.component(text));
    }

    private String getDuelProgressBar(int current, int max) {
        int totalBars = 10;
        float percent = (float) current / max;
        int filled = Math.max(0, Math.min(totalBars, Math.round(totalBars * percent)));
        String color = (percent > 0.5) ? "<green>" : (percent > 0.2) ? "<yellow>" : "<red>";
        return color + "█".repeat(filled) + "<gray>" + "█".repeat(totalBars - filled) + "</gray>" + color.replace("<", "</");
    }

    private void spawnLaserBeam(Location start, Location end) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        dir.normalize();

        for (double d = 0; d < length; d += 0.35) {
            Location pLoc = start.clone().add(dir.clone().multiply(d));
            pLoc.getWorld().spawnParticle(Particle.END_ROD, pLoc, 1, 0, 0, 0, 0);
        }
    }

    private void finishDuel(ActiveDuel duel) {
        if (duel.task != null) duel.task.cancel();

        boolean aWon = (duel.hpA > duel.hpB);
        Player winner = aWon ? duel.playerA : duel.playerB;
        Player loser = aWon ? duel.playerB : duel.playerA;
        ActivePet winPet = aWon ? duel.petA : duel.petB;
        ActivePet losePet = aWon ? duel.petB : duel.petA;

        // Reset nametags
        duel.petA.updateNameTag();
        duel.petB.updateNameTag();

        // Celebration on winner
        winPet.playCelebrationAnimation();
        winner.sendTitle(ColorUtil.colorize("&a&lVICTORY!"), ColorUtil.colorize("&ePet kamu memenangkan duel!"), 5, 45, 10);
        loser.sendTitle(ColorUtil.colorize("&c&lDEFEAT"), ColorUtil.colorize("&7Pet kamu kalah dalam duel."), 5, 45, 10);

        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.2f);
        loser.playSound(loser.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);

        String betMsg = "";
        if (duel.bet > 0.0) {
            double totalPrize = duel.bet * 2.0;
            plugin.getEconomyManager().deposit(winner, totalPrize);
            betMsg = " <gold>Hadiah taruhan: <b>$" + plugin.getEconomyManager().format(totalPrize) + "</b>!</gold>";
        }

        String broadcast = "<gradient:#ff9a00:#ff5500><b>⚔ [PET DUEL] ⚔</b></gradient> " +
                "<white>" + winner.getName() + "</white> <yellow>dengan pet</yellow> <white>" + winPet.getData().getName() + "</white> " +
                "<green><b>MENANG</b></green> <yellow>melawan pet milik</yellow> <white>" + loser.getName() + "</white>!" + betMsg;

        winner.sendMessage(ColorUtil.component(broadcast));
        loser.sendMessage(ColorUtil.component(broadcast));

        activeDuels.remove(duel.playerA.getUniqueId());
        activeDuels.remove(duel.playerB.getUniqueId());
    }

    public void cancelDuel(ActiveDuel duel, String reason) {
        if (duel.task != null) duel.task.cancel();

        // Refund bets if active
        if (duel.bet > 0.0) {
            if (duel.playerA != null && duel.playerA.isOnline()) {
                plugin.getEconomyManager().deposit(duel.playerA, duel.bet);
            }
            if (duel.playerB != null && duel.playerB.isOnline()) {
                plugin.getEconomyManager().deposit(duel.playerB, duel.bet);
            }
        }

        if (duel.petA != null && duel.petA.isValid()) duel.petA.updateNameTag();
        if (duel.petB != null && duel.petB.isValid()) duel.petB.updateNameTag();

        broadcastDuel(duel, "<red>Duel pet dibatalkan: " + reason + "</red>", "");

        activeDuels.remove(duel.playerA.getUniqueId());
        activeDuels.remove(duel.playerB.getUniqueId());
    }

    private void broadcastDuel(ActiveDuel duel, String title, String subtitle) {
        if (duel.playerA.isOnline()) {
            duel.playerA.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + title));
            if (!subtitle.isEmpty()) duel.playerA.sendMessage(ColorUtil.component(subtitle));
        }
        if (duel.playerB.isOnline()) {
            duel.playerB.sendMessage(ColorUtil.component(plugin.getConfigManager().getMessage("prefix") + title));
            if (!subtitle.isEmpty()) duel.playerB.sendMessage(ColorUtil.component(subtitle));
        }
    }

    public void cleanupAll() {
        for (ActiveDuel duel : new HashSet<>(activeDuels.values())) {
            cancelDuel(duel, "Server reload/shutdown.");
        }
        pendingInvites.clear();
        activeDuels.clear();
    }
}
