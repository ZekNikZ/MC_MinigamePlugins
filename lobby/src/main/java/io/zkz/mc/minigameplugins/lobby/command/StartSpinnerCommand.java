package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.Permissions;
import io.zkz.mc.minigameplugins.lobby.SpinnerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = StartSpinnerCommand.COMMAND_NAME,
    desc = "Start the spinner",
    usage = "/" + StartSpinnerCommand.COMMAND_NAME,
    permission = Permissions.Spinner.START
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Spinner.START,
    desc = "Start the spinner"
))
public class StartSpinnerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "startspinner";

    protected StartSpinnerCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SpinnerService.getInstance().startSpinner();
        return true;
    }
}
