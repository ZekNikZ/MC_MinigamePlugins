package io.zkz.mc.minigameplugins.gametools.util;

public interface IObserver<T extends IObservable> {
    void handleChanged(T observable);
}
