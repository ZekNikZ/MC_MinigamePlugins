package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

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
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>Please specify at least one player."));
            return true;
        }

        Arrays.stream(args).forEach(playerName -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (!offlinePlayer.hasPlayedBefore()) {
                    Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>Could not find player '<0>'.", mm(playerName)));
                    return;
                }

                TeamService.getInstance().leaveTeam(offlinePlayer.getUniqueId());
                Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Removed offline player '<0>' from their team.", mm(playerName)));
                return;
            }

            TeamService.getInstance().leaveTeam(player);
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Removed player '<0>' from their team.", mm(playerName)));
        });

        return true;
    }
}
