package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGFinalArena;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import io.zkz.mc.minigameplugins.survivalgames.SGState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.List;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = DeclareWinnerCommand.COMMAND_NAME,
    desc = "Declare the winner of the game",
    usage = "/" + DeclareWinnerCommand.COMMAND_NAME,
    permission = Permissions.FinalArena.DECLARE_WINNER
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.FinalArena.DECLARE_WINNER,
    desc = "Declare the winner of the game"
))
public class DeclareWinnerCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "declarewinner";

    protected DeclareWinnerCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (MinigameService.getInstance().getCurrentState() != MinigameState.IN_GAME || SGService.getInstance().getGameState() != SGState.FINAL_TWO) {
            Chat.sendMessage(sender, ChatColor.RED + "You can only run this command during the final two phase.");
            return true;
        }

        if (TeamService.getInstance().getTeam(args[0]) == null) {
            Chat.sendMessage(sender, ChatColor.RED + "That team does not exist.");
            return true;
        }

        if (!SGService.getInstance().getCurrentRound().isTeamAlive(TeamService.getInstance().getTeam(args[0]))) {
            Chat.sendMessage(sender, ChatColor.RED + "That team is not in the final two.");
            return true;
        }

        SGService.getInstance().declareWinner(args[0]);

        return true;
    }
}
