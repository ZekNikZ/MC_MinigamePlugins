package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

import java.util.Arrays;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = LeaveTeamCommand.COMMAND_NAME,
    desc = "Make player(s) leave a team",
    usage = "/" + LeaveTeamCommand.COMMAND_NAME + "[players...]",
    permission = TeamCommands.Permissions.TEAM_JOIN
))
@Permissions(
    @Permission(name = TeamCommands.Permissions.TEAM_LEAVE, desc = "Leave teams")
)
public class LeaveTeamCommand extends AbstractCommandExecutor {
    static final String COMMAND_NAME = "leaveteam";

    protected LeaveTeamCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Please specify at least one player.");
            return true;
        }

        Arrays.stream(args).forEach(playerName -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "The player '" + playerName + "' is not online.");
                return;
            }

            TeamService.getInstance().leaveTeam(player);
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Removed player '" + playerName + "' from their team.");
        });

        return true;
    }
}
