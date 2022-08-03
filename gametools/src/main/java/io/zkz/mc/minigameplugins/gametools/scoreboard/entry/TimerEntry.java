package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;

public class TimerEntry extends ValueEntry<AbstractTimer> {
    private final int hookId;

    public TimerEntry(String format, AbstractTimer value) {
        super(format, value);

        this.hookId = this.getValue().addHook(this::markDirty);
    }

    @Override
    public void setValue(AbstractTimer value) {
        throw new UnsupportedOperationException("Cannot set the value of a timer entry");
    }

    @Override
    public void cleanup() {
        this.getValue().removeHook(this.hookId);
    }
}
