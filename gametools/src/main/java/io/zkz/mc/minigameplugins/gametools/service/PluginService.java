package io.zkz.mc.minigameplugins.gametools.service;

import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class PluginService<T extends JavaPlugin> implements Listener {
    private T plugin;
    private final List<AbstractDataManager<?>> dataManagers = new ArrayList<>();

    public final void setup(T plugin) {
        this.plugin = plugin;
        this.setup();
    }

    public T getPlugin() {
        return this.plugin;
    }

    public Logger getLogger() {
        return this.getPlugin().getLogger();
    }

    /**
     * Setup code to initialize the service as soon as the plugin is ready.
     */
    protected void setup() {
    }

    /**
     * Setup code to run when the plugin is enabled. Ran AFTER {@link #loadAllData()} is called.
     */
    protected void onEnable() {
    }

    /**
     * Setup code to run when the plugin is disabled. Ran BEFORE {@link #saveAllData()} is called.
     */
    protected void onDisable() {
    }

    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of();
    }

    public final void loadAllData() {
        this.dataManagers.forEach(dataManager -> {
            try {
                dataManager.loadData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public final void saveAllData() {
        this.dataManagers.forEach(dataManager -> {
            try {
                dataManager.saveData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public final void init(T plugin, PluginManager pluginManager) {
        // Initialization
        this.setup(plugin);

        // Link data managers and load initial data
        this.dataManagers.addAll(this.getDataManagers());
        this.loadAllData();

        // On enable callback
        this.onEnable();

        // Link event handlers
        pluginManager.registerEvents(this, plugin);

        plugin.getLogger().info("Initialized service " + this.getClass().getSimpleName());
    }

    public final void cleanup() {
        // On disable callback
        this.onDisable();

        // Save final data and cleanup managers
        this.saveAllData();
        this.dataManagers.forEach(AbstractDataManager::cleanup);
    }
}
