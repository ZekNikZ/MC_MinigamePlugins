package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RespawnCommand.COMMAND_NAME,
    desc = "Respawn a player",
    usage = "/" + RespawnCommand.COMMAND_NAME + "<player>",
    permission = Permissions.Event.RESPAWN
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Event.RESPAWN,
    desc = "Sudden death"
))
public class RespawnCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "respawn";

    protected RespawnCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(MinigameConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Could not find player '" + args[0] + "'.");
            return true;
        }

        SGService.getInstance().getCurrentRound().respawnPlayer(player);

        return true;
    }
}
