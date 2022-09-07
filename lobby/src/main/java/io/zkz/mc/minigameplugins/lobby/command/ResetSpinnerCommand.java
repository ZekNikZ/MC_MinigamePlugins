package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.Permissions;
import io.zkz.mc.minigameplugins.lobby.SpinnerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ResetSpinnerCommand.COMMAND_NAME,
    desc = "Reset the spinner",
    usage = "/" + ResetSpinnerCommand.COMMAND_NAME,
    permission = Permissions.Spinner.RESET
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Spinner.RESET,
    desc = "Reset the spinner"
))
public class ResetSpinnerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "resetspinner";

    protected ResetSpinnerCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SpinnerService.getInstance().resetSpinner();
        return true;
    }
}
