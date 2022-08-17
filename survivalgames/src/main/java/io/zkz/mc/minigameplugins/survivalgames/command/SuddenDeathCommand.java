package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SuddenDeathCommand.COMMAND_NAME,
    desc = "Sudden death",
    usage = "/" + SuddenDeathCommand.COMMAND_NAME,
    permission = SuddenDeathCommand.PERMISSION
))
@Permissions(@Permission(
    name = SuddenDeathCommand.PERMISSION,
    desc = "Sudden death"
))
public class SuddenDeathCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "suddendeath";
    static final String PERMISSION = "survivalgames.suddendeath.start";

    protected SuddenDeathCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SGService.getInstance().getCurrentRound().startSuddenDeath();

        return true;
    }
}
