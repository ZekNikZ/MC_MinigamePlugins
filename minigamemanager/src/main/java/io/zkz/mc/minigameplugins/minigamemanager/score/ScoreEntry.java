package io.zkz.mc.minigameplugins.minigamemanager.score;

import java.util.UUID;

public record ScoreEntry(UUID playerId, String minigame, int round, String reason, float points) {
}
