package io.zkz.mc.minigameplugins.gametools.data;

import io.zkz.mc.minigameplugins.gametools.service.PluginService;

import java.io.IOException;

public abstract class AbstractDataManager<T extends PluginService<?>> {
    protected final T service;

    public AbstractDataManager(T service) {
        this.service = service;
    }

    public abstract void loadData() throws IOException;

    public abstract void saveData() throws IOException;

    public void cleanup() {}
}
