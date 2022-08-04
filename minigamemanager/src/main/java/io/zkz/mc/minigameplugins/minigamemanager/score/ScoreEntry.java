package io.zkz.mc.minigameplugins.minigamemanager.score;

import java.util.UUID;

public record ScoreEntry(UUID playerId, String minigame, int round, String reason, double points, double multiplier) {
    public double getTotalPoints() {
        return this.points * this.multiplier;
    }
}
