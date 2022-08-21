package io.zkz.mc.minigameplugins.gametools.readyup.command;

import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ReadyStatusCommand.COMMAND_NAME,
    desc = "Get the status of ready up sessions",
    usage = "/" + ReadyStatusCommand.COMMAND_NAME,
    permission = Permissions.Ready.STATUS
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Ready.STATUS,
    desc = "Get the status of ready up sessions"
))
public class ReadyStatusCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "readystatus";

    protected ReadyStatusCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        ReadyUpService.getInstance().sendStatus(sender);

        return true;
    }
}
