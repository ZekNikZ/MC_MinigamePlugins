package io.zkz.mc.minigameplugins.gametools.data;

public interface ConfigHolder<C> {
    void setConfig(C value);
    C getConfig();
}
