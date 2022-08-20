package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = StopSpecCommand.COMMAND_NAME,
    desc = "Stop spectating the match",
    usage = "/" + SpecCommand.COMMAND_NAME,
    permission = Permissions.Spectate.STOP
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Spectate.STOP,
    desc = "Spectate the match",
    defaultValue = PermissionDefault.TRUE
))
public class StopSpecCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "lobby";

    protected StopSpecCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            SGService.getInstance().activateSpectatorMode(player);
        }

        return true;
    }
}
