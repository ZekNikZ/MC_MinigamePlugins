package io.zkz.mc.minigameplugins.gametools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommandExecutor implements CommandExecutor {
    private final String commandName;

    protected AbstractCommandExecutor(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return this.handleCommand(sender, command, label, args);
    }

    public abstract boolean handleCommand(CommandSender sender, Command command, String label, String[] args);

    public final String getCommandName() {
        return this.commandName;
    }
}
