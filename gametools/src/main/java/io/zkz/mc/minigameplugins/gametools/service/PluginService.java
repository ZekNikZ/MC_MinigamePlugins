package io.zkz.mc.minigameplugins.gametools.service;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginService<T extends JavaPlugin> implements Listener {
    private T plugin;

    public final void setup(T plugin) {
        this.plugin = plugin;
        this.setup();
    }

    public T getPlugin() {
        return this.plugin;
    }

    /**
     * Setup code to initialize the service as soon as the plugin is ready.
     */
    protected abstract void setup();

    /**
     * Setup code to run when the plugin is enabled.
     */
    public abstract void onEnable();

    /**
     * Setup code to run when the plugin is disabled.
     */
    public abstract void onDisable();
}
