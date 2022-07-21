package io.zkz.mc.minigameplugins.gametools.command;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandGroup<T extends JavaPlugin> {
    public abstract void registerCommands(T plugin);
}
