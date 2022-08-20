package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.Permissions;
import io.zkz.mc.minigameplugins.lobby.TournamentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RemoveServerCommand.COMMAND_NAME,
    desc = "Remove the specified server",
    usage = "/" + RemoveServerCommand.COMMAND_NAME,
    permission = Permissions.Minigame.RESET
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Minigame.RESET,
    desc = "Reset a minigame"
))
public class RemoveServerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "resetminigame";

    protected RemoveServerCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        TournamentManager.getInstance().resetMinigame();
        return true;
    }
}