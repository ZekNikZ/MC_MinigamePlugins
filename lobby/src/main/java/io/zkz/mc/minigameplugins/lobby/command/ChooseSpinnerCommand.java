package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.Permissions;
import io.zkz.mc.minigameplugins.lobby.SpinnerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ChooseSpinnerCommand.COMMAND_NAME,
    desc = "Choose the result of the spinner",
    usage = "/" + ChooseSpinnerCommand.COMMAND_NAME + " <result>",
    permission = Permissions.Spinner.CHOOSE
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Spinner.CHOOSE,
    desc = "Choose the result of the spinner"
))
public class ChooseSpinnerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "pickspinnerresult";

    protected ChooseSpinnerCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SpinnerService.getInstance().pickSpinnerResult(Integer.parseInt(args[0]));
        return true;
    }
}
