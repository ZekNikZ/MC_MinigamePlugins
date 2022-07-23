package io.zkz.mc.minigameplugins.gametools.util;

import java.util.Collection;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface IObservable {
    void addListener(IObserver observer);

    void removeListener(IObserver observer);

    Collection<IObserver> getListeners();

    default void notifyObservers() {
        getListeners().forEach(observer -> observer.handleChanged(this));
    }
}
