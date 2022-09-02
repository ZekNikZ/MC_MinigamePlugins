package io.zkz.mc.minigameplugins.minigamemanager.round;

import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.jetbrains.annotations.Nullable;

public abstract class Round {
    private String mapName;
    private String mapBy;

    protected Round() {
        this.mapName = null;
        this.mapBy = null;
    }

    protected Round(String mapName) {
        this.mapName = mapName;
        this.mapBy = null;
    }

    protected Round(String mapName, String mapBy) {
        this.mapName = mapName;
        this.mapBy = mapBy;
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
    public void onRoundStart() {

    }

    public void onPhase1End() {

    }

    public void onPhase2Start() {

    }

    /**
     * Run when game enters pre-round phase.
     */
    public void onEnterPreRound() {

    }

    /**
     * Run when game enters post-round phase.
     */
    public void onEnterPostRound() {

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
     * Run when timer ticks
     *
     * @param currentTimeMillis value of the timer at this point in time
     */
    public void onPreRoundTimerTick(long currentTimeMillis) {

    }

    /**
     * Convenience method to end a round.
     */
    public void triggerRoundEnd() {
        MinigameService.getInstance().endRound();
    }

    /**
     * Convenience method to end the first phase.
     */
    public void triggerPhase1End() {
        MinigameService.getInstance().endPhase1();
    }

    /**
     * Convenience method to start the second phase.
     */
    public void triggerPhase2Start() {
        MinigameService.getInstance().startPhase2();
    }

    public @Nullable String getMapName() {
        return this.mapName;
    }

    public @Nullable String getMapBy() {
        return this.mapBy;
    }

    public void setMapName(@Nullable String mapName) {
        this.mapName = mapName;
    }

    protected void setMapBy(String author) {
        this.mapBy = author;
    }
}
