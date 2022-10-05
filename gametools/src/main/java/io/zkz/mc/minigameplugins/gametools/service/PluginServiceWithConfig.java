package io.zkz.mc.minigameplugins.gametools.service;

import io.zkz.mc.minigameplugins.gametools.data.ConfigHolder;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginServiceWithConfig<T extends JavaPlugin, C> extends PluginService<T> implements ConfigHolder<C> {
    private C config;

    @Override
    public void setConfig(C value) {
        this.config = value;
    }

    @Override
    public C getConfig() {
        return this.config;
    }
}
