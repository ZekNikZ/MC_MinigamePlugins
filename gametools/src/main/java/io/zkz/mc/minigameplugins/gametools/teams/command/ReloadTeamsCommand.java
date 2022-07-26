package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ReloadTeamsCommand.COMMAND_NAME,
    desc = "Reload teams from the configuration",
    usage = "/" + ReloadTeamsCommand.COMMAND_NAME,
    permission = TeamCommands.Permissions.TEAM_RELOAD
))
@Permissions(
    @Permission(name = TeamCommands.Permissions.TEAM_RELOAD, desc = "Reload teams")
)
public class ReloadTeamsCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "reloadteams";

    protected ReloadTeamsCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        // Setup default teams
        TeamService.getInstance().loadAllData();

        // TODO: error checking?

        sender.sendMessage(MinigameConstantsService.getInstance().getPrefix() + ChatColor.GRAY + "Successfully reloaded teams.");

        return true;
    }
}
