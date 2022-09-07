package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.Permissions;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;
import java.util.stream.Collectors;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = AddPointsCommand.COMMAND_NAME,
    desc = "Add points to a given team",
    usage = "/" + AddPointsCommand.COMMAND_NAME + " <teamID> <points> [reason]",
    permission = Permissions.Score.ADD
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Score.ADD,
    desc = "Add points"
))
public class AddPointsCommand extends AbstractCommandExecutor {
    static final String COMMAND_NAME = "addpoints";

    protected AddPointsCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        // Team ID
        String teamId = args[0];
        if (TeamService.getInstance().getTeam(teamId) == null) {
            sender.sendMessage(ChatColor.RED + "Could not add points: team with given id '" + teamId + "' does not exist.");
            return true;
        }

        // Points
        double points;
        try {
            points = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Could not add points: specified points is not a value number.");
            return true;
        }

        // Reason
        String reason;
        if (args.length == 2) {
            reason = null;
        } else {
            reason = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
        }

        // Assign points
        ScoreService.getInstance().earnPointsUUID(TeamService.getInstance().getTeamMembers(teamId), reason, points / TeamService.getInstance().getTeamMembers(teamId).size(), MinigameService.getInstance().getCurrentRoundIndex());
        sender.sendMessage(ChatColor.GRAY + "Successfully assigned points.");

        return true;
    }
}
