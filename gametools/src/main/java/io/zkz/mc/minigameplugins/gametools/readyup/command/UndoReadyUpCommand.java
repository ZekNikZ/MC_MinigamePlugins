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
    name = UndoReadyUpCommand.COMMAND_NAME,
    desc = "Mark a player as not ready",
    usage = "/" + UndoReadyUpCommand.COMMAND_NAME,
    permission = Permissions.Ready.UNDO_READY_UP
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Ready.UNDO_READY_UP,
    desc = "Undo ready up"
))
public class UndoReadyUpCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "undoready";

    protected UndoReadyUpCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!ReadyUpService.getInstance().undoReady(player.getUniqueId())) {
            Chat.sendAlert(sender, ChatType.WARNING, "That player was not marked as ready.");
        } else {
            Chat.sendAlert(sender, ChatType.ACTIVE_INFO, "Marked that player as not ready");
        }

        return true;
    }
}
