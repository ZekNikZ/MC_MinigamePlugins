package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmResolve;

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
            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>Error: could not set up default teams."));
            Chat.sendMessage(sender, ChatType.GAME_INFO, mmResolve("<red><exmsg>", Placeholder.unparsed("exmsg", exception.getMessage())));
            return true;
        }

        Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Successfully set up default teams."));

        return true;
    }
}
