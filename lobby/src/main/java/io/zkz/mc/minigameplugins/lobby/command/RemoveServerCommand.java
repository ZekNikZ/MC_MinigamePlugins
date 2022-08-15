package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.TournamentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RemoveServerCommand.COMMAND_NAME,
    desc = "Remove the specified server",
    usage = "/" + RemoveServerCommand.COMMAND_NAME,
    permission = LobbyCommands.Permissions.SERVER_REMOVE
))
@Permissions(
    @Permission(name = LobbyCommands.Permissions.SERVER_REMOVE, desc = "Remove Server")
)
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