package io.zkz.mc.minigameplugins.gametools.util;

public class ObservableValue<T> extends AbstractObservable {
    private T value;

    public ObservableValue(T defaultValue) {
        this.value = defaultValue;
    }

    private void set(T newValue) {
        this.value = newValue;
        this.notifyObservers();
    }

    public T get() {
        return this.value;
    }
}
