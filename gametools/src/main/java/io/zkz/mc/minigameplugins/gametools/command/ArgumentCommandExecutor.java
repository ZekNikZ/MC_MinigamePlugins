package io.zkz.mc.minigameplugins.gametools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@Deprecated
public abstract class ArgumentCommandExecutor extends AbstractCommandExecutor {
    private final int expectedArgumentCount;

    protected ArgumentCommandExecutor(String commandName, int expectedArgumentCount) {
        super(commandName);
        this.expectedArgumentCount = expectedArgumentCount;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != expectedArgumentCount) {
            return false;
        }

        return this.handleCommand(sender, command, label, args);
    }
}
