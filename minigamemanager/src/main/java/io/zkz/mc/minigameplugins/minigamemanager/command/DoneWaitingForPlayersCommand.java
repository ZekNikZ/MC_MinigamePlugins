package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = DoneWaitingForPlayersCommand.COMMAND_NAME,
    desc = "Add points to a given team",
    usage = "/" + DoneWaitingForPlayersCommand.COMMAND_NAME,
    permission = DoneWaitingForPlayersCommand.PERMISSION
))
@Permissions(
    @Permission(name = DoneWaitingForPlayersCommand.PERMISSION, desc = "Done waiting for players")
)
public class DoneWaitingForPlayersCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "donewaitingforplayers";
    static final String PERMISSION = "minigamemanager.state.waiting_for_players.exit";

    protected DoneWaitingForPlayersCommand() {
        super(COMMAND_NAME, 0);
    }


    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        MinigameService.getInstance().markDoneWaitingForPlayers();

        return true;
    }
}
