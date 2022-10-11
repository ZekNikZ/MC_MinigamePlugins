package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;

import java.util.UUID;

@FunctionalInterface
public interface PlayerBasedMinigameScoreboard extends MinigameScoreboard {
    void apply(UUID playerId);

    @Override
    default void setup() {
        MinigameService.getInstance().getMinigame().getParticipants().forEach(this::apply);
    }
}
