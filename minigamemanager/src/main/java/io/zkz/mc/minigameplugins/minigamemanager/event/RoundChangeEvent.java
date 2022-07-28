package io.zkz.mc.minigameplugins.minigamemanager.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractCancellableEvent;

public class RoundChangeEvent extends AbstractCancellableEvent {
    private final int oldRound;
    private final int newRound;

    public RoundChangeEvent(int oldRound, int newRound) {
        this.oldRound = oldRound;
        this.newRound = newRound;
    }

    public int getOldRound() {
        return this.oldRound;
    }

    public int getNewRound() {
        return this.newRound;
    }
}
