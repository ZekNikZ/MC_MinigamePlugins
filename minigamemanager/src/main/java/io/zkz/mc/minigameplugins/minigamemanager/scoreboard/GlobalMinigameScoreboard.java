package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

@FunctionalInterface
public interface GlobalMinigameScoreboard extends MinigameScoreboard {
    void apply();

    @Override
    default void setup() {
        this.apply();
    }
}
