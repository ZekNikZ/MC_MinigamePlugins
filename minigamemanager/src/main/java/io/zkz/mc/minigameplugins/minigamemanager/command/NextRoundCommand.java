package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.minigamemanager.Permissions;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = NextRoundCommand.COMMAND_NAME,
    desc = "Go to next round",
    usage = "/" + NextRoundCommand.COMMAND_NAME,
    permission = Permissions.Round.NEXT
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Round.NEXT,
    desc = "Go to next minigame round"
))
public class NextRoundCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "nextround";
    static final String PERMISSION =  "minigamemanager.round.next";

    protected NextRoundCommand() {
        super(COMMAND_NAME, 0);
    }


    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        MinigameService.getInstance().goToNextRound();

        return true;
    }
}
