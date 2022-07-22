package io.zkz.mc.minigameplugins.gametools.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AbstractEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
