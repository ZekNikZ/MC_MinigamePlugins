package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.Permissions;
import io.zkz.mc.minigameplugins.lobby.TournamentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ResetMinigamesCommand.COMMAND_NAME,
    desc = "Reset minigames",
    usage = "/" + ResetMinigamesCommand.COMMAND_NAME,
    permission = Permissions.Minigame.RESET_ALL
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Minigame.RESET_ALL,
    desc = "Reset minigames"
))
public class ResetMinigamesCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "resetallminigames";

    protected ResetMinigamesCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        TournamentManager.getInstance().resetAllMinigames();
        return true;
    }
}
