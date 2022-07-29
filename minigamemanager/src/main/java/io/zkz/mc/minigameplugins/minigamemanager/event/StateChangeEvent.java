package io.zkz.mc.minigameplugins.minigamemanager.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractEvent;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.event.Cancellable;

public class StateChangeEvent extends AbstractEvent {
    private final MinigameState oldState;
    private final MinigameState newState;

    private StateChangeEvent(MinigameState oldState, MinigameState newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    public MinigameState getOldState() {
        return this.oldState;
    }

    public MinigameState getNewState() {
        return this.newState;
    }

    public static class Pre extends StateChangeEvent implements Cancellable {
        private boolean cancelled = false;

        public Pre(MinigameState oldState, MinigameState newState) {
            super(oldState, newState);
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }

    public static class Post extends StateChangeEvent {
        public Post(MinigameState oldState, MinigameState newState) {
            super(oldState, newState);
        }
    }
}
