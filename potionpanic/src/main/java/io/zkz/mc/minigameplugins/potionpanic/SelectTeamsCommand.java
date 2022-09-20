package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SelectTeamsCommand.COMMAND_NAME,
    desc = "Choose the competitors",
    usage = "/" + SelectTeamsCommand.COMMAND_NAME + " <team 1 id> <team 2 id>",
    permission = Permissions.Competitors.SELECT
))
@org.bukkit.plugin.java.annotation.permission.Permissions(
    @Permission(name = Permissions.Competitors.SELECT, desc = "Choose the competitors")
)
public class SelectTeamsCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "selectteams";

    protected SelectTeamsCommand() {
        super(COMMAND_NAME, 2);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        GameTeam team1 = TeamService.getInstance().getTeam(args[0]);
        GameTeam team2 = TeamService.getInstance().getTeam(args[1]);

        if (team1 == null || team2 == null) {
            sender.sendMessage(ChatColor.RED + "Error: make sure both of those teams exist");
            return true;
        }

        PotionPanicService.getInstance().setTeam1(team1);
        PotionPanicService.getInstance().setTeam2(team2);
        sender.sendMessage(ChatColor.GRAY + "Selected teams " + team1.id() + " and " + team2.id());

        return true;
    }
}
