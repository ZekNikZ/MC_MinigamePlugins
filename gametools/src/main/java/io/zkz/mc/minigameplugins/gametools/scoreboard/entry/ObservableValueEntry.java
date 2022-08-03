package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.ObservableValue;

public class ObservableValueEntry<T, O extends ObservableValue<T>> extends ValueEntry<O> implements IObserver<O> {
    public ObservableValueEntry(String format, O value) {
        super(format, value);

        this.getValue().addListener(this);
    }

    @Override
    public void setValue(O value) {
        throw new UnsupportedOperationException("Cannot set the value of a observable value entry");
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

