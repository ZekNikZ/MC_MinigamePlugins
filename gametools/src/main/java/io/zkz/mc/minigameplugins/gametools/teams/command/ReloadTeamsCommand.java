package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ReloadTeamsCommand.COMMAND_NAME,
    desc = "Reload teams from the configuration",
    usage = "/" + ReloadTeamsCommand.COMMAND_NAME,
    permission = Permissions.Teams.RELOAD
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Teams.RELOAD,
    desc = "Reload teams"
))
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

        sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Successfully reloaded teams.");

        return true;
    }
}
