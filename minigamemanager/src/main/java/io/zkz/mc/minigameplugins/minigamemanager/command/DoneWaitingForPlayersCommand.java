package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.Permissions;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = DoneWaitingForPlayersCommand.COMMAND_NAME,
    desc = "Mark game as done waiting for players",
    usage = "/" + DoneWaitingForPlayersCommand.COMMAND_NAME,
    permission = Permissions.State.Exit.WAITING_FOR_PLAYERS
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.State.Exit.WAITING_FOR_PLAYERS,
    desc = "Manually decide that we are done waiting for players"
))
public class DoneWaitingForPlayersCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "donewaitingforplayers";

    protected DoneWaitingForPlayersCommand() {
        super(COMMAND_NAME, 0);
    }


    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        MinigameService.getInstance().markDoneWaitingForPlayers();

        return true;
    }
}
