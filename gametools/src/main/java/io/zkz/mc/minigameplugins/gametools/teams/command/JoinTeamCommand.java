package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

import java.util.Arrays;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = JoinTeamCommand.COMMAND_NAME,
    desc = "Make player(s) join a team",
    usage = "/" + JoinTeamCommand.COMMAND_NAME + "<team> [players...]",
    permission = TeamCommands.Permissions.TEAM_JOIN
))
@Permissions(
    @Permission(name = TeamCommands.Permissions.TEAM_JOIN, desc = "Join teams")
)
public class JoinTeamCommand extends AbstractCommandExecutor {
    static final String COMMAND_NAME = "jointeam";

    protected JoinTeamCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Please specify at least one player.");
            return true;
        }

        // Get the team
        GameTeam team = TeamService.getInstance().getTeam(args[0]);
        if (team == null) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "The team with ID '" + args[0] + "' does not exist.");
            return true;
        }

        Arrays.stream(args).skip(1).forEach(playerName -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "The player '" + playerName + "' is not online.");
                return;
            }

            TeamService.getInstance().joinTeam(player, team);
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Added player '" + playerName + "' to team '" + team.getDisplayName() + "'.");
        });

        return true;
    }
}
