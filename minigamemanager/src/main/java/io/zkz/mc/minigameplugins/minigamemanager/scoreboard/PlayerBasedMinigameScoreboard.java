package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;

import java.util.UUID;

@FunctionalInterface
public interface PlayerBasedMinigameScoreboard extends MinigameScoreboard {
    void apply(UUID playerId);

    @Override
    default void setup() {
        MinigameService.getInstance().getPlayers().forEach(this::apply);
    }
}