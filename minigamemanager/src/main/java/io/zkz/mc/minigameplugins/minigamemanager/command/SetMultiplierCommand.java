package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.Permissions;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SetMultiplierCommand.COMMAND_NAME,
    desc = "Set the multiplier of the minigame",
    usage = "/" + SetMultiplierCommand.COMMAND_NAME + " <num>",
    permission = Permissions.Score.MULTIPLIER
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Score.MULTIPLIER,
    desc = "Set minigame score multiplier"
))
public class SetMultiplierCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setmultiplier";

    protected SetMultiplierCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid number");
            return true;
        }

        MinigameService.getInstance().setPointMultiplier(multiplier);
        MinigameService.getInstance().refreshScoreboard();

        return true;
    }
}
