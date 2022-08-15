package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.lobby.TournamentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SetupServerCommand.COMMAND_NAME,
    desc = "Set up the specified server",
    usage = "/" + SetupServerCommand.COMMAND_NAME + " <minigame>",
    permission = LobbyCommands.Permissions.SERVER_SETUP
))
@Permissions(
    @Permission(name = LobbyCommands.Permissions.SERVER_SETUP, desc = "Setup Server")
)
public class SetupServerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setupminigame";

    protected SetupServerCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        TournamentManager.getInstance().startMinigame(args[0]);
        return true;
    }
}
