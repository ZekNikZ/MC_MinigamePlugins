package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = JoinTeamCommand.COMMAND_NAME,
    desc = "Make player(s) join a team",
    usage = "/" + JoinTeamCommand.COMMAND_NAME + "<team> [players...]",
    permission = Permissions.Teams.JOIN
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Teams.JOIN,
    desc = "Make players join teams"
))
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
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>Please specify at least one player."));
            return true;
        }

        // Get the team
        GameTeam team = TeamService.getInstance().getTeam(args[0]);
        if (team == null) {
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>The team with id '<0>' does not exist.", mm(args[0])));
            return true;
        }

        Arrays.stream(args).skip(1).forEach(playerName -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>The player '<0>' is not online.", mm(playerName)));
                return;
            }

            TeamService.getInstance().joinTeam(player, team);
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Added player '<0>' to team '<1>'.", mm(playerName), team.getDisplayName()));
        });

        return true;
    }
}
