package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
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
    name = NextRoundCommand.COMMAND_NAME,
    desc = "Go to next round",
    usage = "/" + NextRoundCommand.COMMAND_NAME,
    permission = Permissions.Round.NEXT
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Round.NEXT,
    desc = "Go to next minigame round"
))
public class NextRoundCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "nextround";
    static final String PERMISSION =  "minigamemanager.round.next";

    protected NextRoundCommand() {
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
        } else if (MinigameService.getInstance().getCurrentState() != MinigameState.POST_ROUND) {
            sender.sendMessage(ChatColor.RED + "Cannot transition states: you can only use this command in the POST_ROUND state, did you mean /donewaitingforplayers?");
            return true;
        }

        MinigameService.getInstance().goToNextRound();

        return true;
    }
}
