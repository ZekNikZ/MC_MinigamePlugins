package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SetRoundCommand.COMMAND_NAME,
    desc = "Set the current round of the minigame",
    usage = "/" + SetRoundCommand.COMMAND_NAME + " <round>",
    permission = MinigameCommands.Permissions.SET_ROUND
))
@Permissions(
    @Permission(name = MinigameCommands.Permissions.SET_ROUND, desc = "Set minigame round")
)
public class SetRoundCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setround";

    protected SetRoundCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        int round;
        try {
            round = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid number");
            return true;
        }

        if (round < 0 || round >= MinigameService.getInstance().getRoundCount()) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a registered round");
            return true;
        }

        MinigameService.getInstance().getCurrentRound().onPostRound();
        MinigameService.getInstance().setCurrentRound(round);
        MinigameService.getInstance().setState(MinigameState.PRE_ROUND);

        return true;
    }
}
