package io.zkz.mc.minigameplugins.minigamemanager.round;

import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.jetbrains.annotations.Nullable;

public abstract class Round {
    private String mapName;

    protected Round() {
        this.mapName = null;
    }

    protected Round(String mapName) {
        this.mapName = mapName;
    }

    /**
     * Run when round is selected. Designed for setting up spawnpoints, etc.
     */
    public void onSetup() {

    }

    /**
     * Run when round is deselected. Designed for saving scores, cleaning up the arena, etc.
     */
    public void onCleanup() {

    }

    /**
     * Run when round has begun (i.e., when players can start moving).
     */
    public void onStart() {

    }

    /**
     * Run when game enters pre-round phase.
     */
    public void onPreRound() {

    }

    /**
     * Run when game enters post-round phase.
     */
    public void onPostRound() {

    }

    /**
     * Run when round has ended (i.e., when timer expires or last man standing).
     */
    public void onEnd() {

    }

    /**
     * Run when game is paused.
     */
    public void onPause() {

    }

    /**
     * Run when game is unpaused.
     */
    public void onUnpause() {

    }

    /**
     * Convenience method to end a round.
     */
    public void triggerRoundEnd() {
        MinigameService.getInstance().endRound();
    }

    public @Nullable String getMapName() {
        return this.mapName;
    }

    public void setMapName(@Nullable String mapName) {
        this.mapName = mapName;
    }
}
