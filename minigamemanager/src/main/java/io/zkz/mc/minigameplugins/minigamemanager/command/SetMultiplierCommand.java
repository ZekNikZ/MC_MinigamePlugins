package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SetMultiplierCommand.COMMAND_NAME,
    desc = "Set the multiplier of the minigame",
    usage = "/" + SetMultiplierCommand.COMMAND_NAME + " <num>",
    permission = SetMultiplierCommand.PERMISSION
))
@Permissions(
    @Permission(name = SetMultiplierCommand.PERMISSION, desc = "Set point multiplier")
)
public class SetMultiplierCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setmultiplier";
    static final String PERMISSION = "minigamemanager.multiplier.set";

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
