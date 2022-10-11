package io.zkz.mc.minigameplugins.minigamemanager.task;

import io.zkz.mc.minigameplugins.gametools.util.GameTask;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;

public abstract class MinigameTask extends GameTask {
    public MinigameTask(int delay, int period) {
        super(delay, period);
    }

    public MinigameTask(int delay) {
        super(delay);
    }

    public synchronized void cancel(boolean removeReference) throws IllegalStateException {
        super.cancel(removeReference);
        if (removeReference) {
            MinigameService.getInstance().removeRunningTask(this);
        }
    }
}
