package io.zkz.mc.minigameplugins.dev.survivalgames;

import io.zkz.mc.minigameplugins.dev.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = PopulateChestsCommand.COMMAND_NAME,
    desc = "Populate chests",
    usage = "/" + PopulateChestsCommand.COMMAND_NAME,
    permission = Permissions.SurvivalGames.ADMIN
))
public class PopulateChestsCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "populatechests";

    protected PopulateChestsCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Gotta be a player.");
            return true;
        }

        SGService.getInstance().populateChests(player);

        return true;
    }
}
