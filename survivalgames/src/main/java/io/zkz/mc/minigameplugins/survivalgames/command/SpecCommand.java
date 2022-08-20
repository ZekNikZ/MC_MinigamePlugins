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
    name = SpecCommand.COMMAND_NAME,
    desc = "Start spectating",
    usage = "/" + SpecCommand.COMMAND_NAME,
    permission = Permissions.Spectate.START
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Spectate.START,
    desc = "Spectate the match",
    defaultValue = PermissionDefault.TRUE
))
public class SpecCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "spec";

    protected SpecCommand() {
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
