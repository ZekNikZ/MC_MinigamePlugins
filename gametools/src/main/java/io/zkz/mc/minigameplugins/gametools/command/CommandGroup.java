package io.zkz.mc.minigameplugins.gametools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
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

        this.commands.forEach((commandName, executor) -> {
            PluginCommand command = plugin.getCommand(commandName);
            if (command == null) {
                plugin.getLogger().severe("Could not register command /" + commandName);
                return;
            }

            command.setExecutor(executor);
            if (executor instanceof TabCompleter completer) {
               command.setTabCompleter(completer);
            }
        });

        plugin.getLogger().info("Initialized command group " + this.getClass().getSimpleName());
    }
}
