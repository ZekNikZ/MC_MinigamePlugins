package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = DefaultTeamsCommand.COMMAND_NAME,
    desc = "Setup default teams",
    usage = "/" + DefaultTeamsCommand.COMMAND_NAME,
    permission = Permissions.Teams.CREATE
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Teams.CREATE,
    desc = "Create teams"
))
public class DefaultTeamsCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setupdefaultteams";

    protected DefaultTeamsCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        // Setup default teams
        try {
            TeamService.getInstance().setupDefaultTeams();
        } catch (TeamService.TeamCreationException exception) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Error: could not set up default teams.");
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + exception.getMessage());
            return true;
        }

        sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.GRAY + "Successfully set up default teams.");

        return true;
    }
}
