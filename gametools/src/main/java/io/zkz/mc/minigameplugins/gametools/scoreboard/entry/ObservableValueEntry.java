package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.ObservableValue;

public class ObservableValueEntry<T, O extends ObservableValue<T>> extends ValueEntry<O> implements IObserver<O> {
    public ObservableValueEntry(GameScoreboard scoreboard, O value) {
        super(scoreboard, value);

        value.addListener(this);
    }

    public ObservableValueEntry(GameScoreboard scoreboard, String mainText, O value) {
        super(scoreboard, mainText, value);

        value.addListener(this);
    }

    public ObservableValueEntry(GameScoreboard scoreboard, String prefix, String mainText, String suffix, O value) {
        super(scoreboard, prefix, mainText, suffix, value);

        value.addListener(this);
    }

    @Override
    protected String getValueString() {
        return this.getValue().get().toString();
    }

    @Override
    public void cleanup() {
        this.getValue().removeListener(this);
    }

    @Override
    public void handleChanged(O observable) {
        this.markDirty();
    }
}
