package io.zkz.mc.minigameplugins.gametools.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandGroup {
    private final Map<String, CommandExecutor> commands = new HashMap<>();

    protected final void register(String commandName, CommandExecutor executor) {
        if (this.commands.containsKey(commandName)) {
            throw new IllegalArgumentException("Cannot have duplicate command name");
        }

        this.commands.put(commandName, executor);
    }

    protected final void register(AbstractCommandExecutor executor) {
        this.register(executor.getCommandName(), executor);
    }

    public abstract void registerCommands();

    public final void registerCommands(JavaPlugin plugin) {
        this.registerCommands();

        this.commands.forEach((commandName, executor) -> plugin.getCommand(commandName).setExecutor(executor));

        plugin.getLogger().info("Initialized command group " + this.getClass().getSimpleName());
    }
}
