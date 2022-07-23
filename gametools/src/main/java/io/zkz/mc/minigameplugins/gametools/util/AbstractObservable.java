package io.zkz.mc.minigameplugins.gametools.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
public class AbstractObservable implements IObservable {
    private final List<IObserver> observers = new ArrayList<>();

    @Override
    public void addListener(IObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeListener(IObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public Collection<IObserver> getListeners() {
        return this.observers;
    }
}
