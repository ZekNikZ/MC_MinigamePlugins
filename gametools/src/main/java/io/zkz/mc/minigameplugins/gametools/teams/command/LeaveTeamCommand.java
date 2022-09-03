package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = LeaveTeamCommand.COMMAND_NAME,
    desc = "Make player(s) leave a team",
    usage = "/" + LeaveTeamCommand.COMMAND_NAME + "[players...]",
    permission = Permissions.Teams.LEAVE
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Teams.LEAVE,
    desc = "Make players leave teams"
))
public class LeaveTeamCommand extends AbstractCommandExecutor {
    static final String COMMAND_NAME = "leaveteam";

    protected LeaveTeamCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Please specify at least one player.");
            return true;
        }

        Arrays.stream(args).forEach(playerName -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (!offlinePlayer.hasPlayedBefore()) {
                    sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Could not find player '" + playerName + "'.");
                    return;
                }

                TeamService.getInstance().leaveTeam(offlinePlayer.getUniqueId());
                sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Removed offline player '" + playerName + "' from their team.");
                return;
            }

            TeamService.getInstance().leaveTeam(player);
            sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Removed player '" + playerName + "' from their team.");
        });

        return true;
    }
}
