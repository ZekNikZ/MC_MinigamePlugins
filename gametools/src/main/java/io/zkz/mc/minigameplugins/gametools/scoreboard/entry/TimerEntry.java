package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;

// TODO: make another version that takes a timer proxy that can be replaced by the minigame service dynamically
public class TimerEntry extends ValueEntry<AbstractTimer> {
    private final int hookId;

    public TimerEntry(GameScoreboard scoreboard, AbstractTimer value) {
        super(scoreboard, value);

        this.hookId = value.addHook(this::markDirty);
    }

    public TimerEntry(GameScoreboard scoreboard, String mainText, AbstractTimer value) {
        super(scoreboard, mainText, value);

        this.hookId = value.addHook(this::markDirty);
    }

    public TimerEntry(GameScoreboard scoreboard, String prefix, String mainText, String suffix, AbstractTimer value) {
        super(scoreboard, prefix, mainText, suffix, value);

        this.hookId = value.addHook(this::markDirty);
    }

    @Override
    public void cleanup() {
        this.getValue().removeHook(this.hookId);
    }
}
