package io.zkz.mc.minigameplugins.minigamemanager.round;

import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;

public abstract class Round {
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
}
