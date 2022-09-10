package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SetFloorCommand.COMMAND_NAME,
    desc = "Change the floor",
    usage = "/" + SetFloorCommand.COMMAND_NAME
))
public class SetFloorCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "setfloor";

    protected SetFloorCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        }

        if (!GuessTheBuildService.getInstance().getCurrentRound().getBuilder().equals(player)) {
            sender.sendMessage(ChatColor.RED + "You must be the builder to use this command.");
            return true;
        }

        GuessTheBuildService.getInstance().getCurrentRound().setFloor(player);

        return true;
    }
}
