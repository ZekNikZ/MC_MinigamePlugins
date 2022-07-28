package io.zkz.mc.minigameplugins.minigamemanager.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractCancellableEvent;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;

public class StateChangeEvent extends AbstractCancellableEvent {
    private final MinigameState oldState;
    private final MinigameState newState;

    public StateChangeEvent(MinigameState oldState, MinigameState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public MinigameState getOldState() {
        return this.oldState;
    }

    public MinigameState getNewState() {
        return this.newState;
    }
}
