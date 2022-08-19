package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RespawnCommand.COMMAND_NAME,
    desc = "Respawn a player",
    usage = "/" + RespawnCommand.COMMAND_NAME + "<player>",
    permission = RespawnCommand.PERMISSION
))
@Permissions(@Permission(
    name = RespawnCommand.PERMISSION,
    desc = "Respawn a player",
    defaultValue = PermissionDefault.TRUE
))
public class RespawnCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "respawn";
    static final String PERMISSION = "survivalgames.admin.respawn";

    protected RespawnCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "Could not find player '" + args[0] + "'.");
            return true;
        }

        SGService.getInstance().getCurrentRound().respawnPlayer(player);

        return true;
    }
}
