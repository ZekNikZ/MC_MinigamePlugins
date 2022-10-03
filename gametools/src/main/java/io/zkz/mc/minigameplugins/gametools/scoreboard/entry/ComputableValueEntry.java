package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import net.kyori.adventure.text.Component;

import java.util.function.Supplier;

public class ComputableValueEntry<T> extends ValueEntry<Supplier<T>> {
    public ComputableValueEntry(String format, Supplier<T> value) {
        super(format, value);
    }

    @Override
    public void setValue(Supplier<T> value) {
        throw new UnsupportedOperationException("Cannot set the value of a computable value entry");
    }

    @Override
    protected Component getValueComponent() {
        return Component.text(this.getValue().get().toString());
    }
}
