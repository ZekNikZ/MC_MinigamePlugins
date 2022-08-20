package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SuddenDeathCommand.COMMAND_NAME,
    desc = "Sudden death",
    usage = "/" + SuddenDeathCommand.COMMAND_NAME,
    permission = Permissions.Event.SUDDEN_DEATH
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Event.SUDDEN_DEATH,
    desc = "Sudden death"
))
public class SuddenDeathCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "suddendeath";

    protected SuddenDeathCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SGService.getInstance().getCurrentRound().startSuddenDeath();

        return true;
    }
}
