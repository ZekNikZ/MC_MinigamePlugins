package io.zkz.mc.uhc.commands.impl;

import dev.mattrm.mc.gametools.settings.impl.IntRangeSetting;
import dev.mattrm.mc.uhcplugin.game.GameManager;
import dev.mattrm.mc.uhcplugin.settings.SettingsManager;
import dev.mattrm.mc.uhcplugin.settings.enums.TeamStatus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;

import java.util.List;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = "uhcstartgame",
    aliases = {"startgame", "startuhc", "uhcstart"},
    desc = "Start UHC game",
    usage = "/uhcstartgame"
))
public class StartGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<IntRangeSetting> distances = SettingsManager.getInstance().worldborderDistances();
        if (distances.get(0).get() <= distances.get(1).get() || distances.get(1).get() <= distances.get(2).get()) {
            sender.sendMessage(ChatColor.RED + "Invalid world border setup. Use /settings to modify.");
        } else if (SettingsManager.getInstance().teamGame().get() == TeamStatus.TEAM_GAME && GameManager.getInstance().getInitialTeams().size() < 2) {
            sender.sendMessage(ChatColor.RED + "Not enough teams.");
        } else if (GameManager.getInstance().getInitialCompetitors().size() < 2) {
            sender.sendMessage(ChatColor.RED + "Not enough players.");
        } else {
            GameManager.getInstance().enterPregamePhase();
        }
        return true;
    }
}
