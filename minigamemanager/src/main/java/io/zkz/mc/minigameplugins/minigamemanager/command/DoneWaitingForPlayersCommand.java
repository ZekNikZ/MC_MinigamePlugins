package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.Permissions;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
        if (!TeamService.getInstance().areAllNonSpectatorsOnline()) {
            sender.sendMessage(ChatColor.RED + "Cannot transition states: all participants are not online. Either remove offline players from teams or wait for all players to be present.");
            return true;
        } else if (Bukkit.getOnlinePlayers().stream().anyMatch(p -> TeamService.getInstance().getTeamOfPlayer(p) == null)) {
            sender.sendMessage(ChatColor.RED + "Cannot transition states: someone is not on a team.");
            return true;
        } else if (MinigameService.getInstance().getCurrentState() != MinigameState.WAITING_FOR_PLAYERS) {
            sender.sendMessage(ChatColor.RED + "Cannot transition states: you can only use this command in the WAITING_FOR_PLAYERS state, did you mean /nextround?");
            return true;
        }

        MinigameService.getInstance().markDoneWaitingForPlayers();

        return true;
    }
}
