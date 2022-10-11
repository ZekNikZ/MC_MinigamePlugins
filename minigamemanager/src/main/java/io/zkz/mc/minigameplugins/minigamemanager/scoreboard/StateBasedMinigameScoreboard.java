package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;

@FunctionalInterface
public interface StateBasedMinigameScoreboard extends MinigameScoreboard {
    void apply(MinigameState state);

    @Override
    default void setup() {
        this.apply(MinigameService.getInstance().getCurrentState());
    }
}
