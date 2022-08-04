package io.zkz.mc.minigameplugins.gametools.readyup.command;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ReadyUpCommand.COMMAND_NAME,
    desc = "Mark yourself as ready",
    usage = "/" + ReadyUpCommand.COMMAND_NAME,
    permission = ReadyUpCommands.Permissions.READY_UP
))
@Permissions(
    @Permission(name = ReadyUpCommands.Permissions.READY_UP, desc = "Ready up", defaultValue = PermissionDefault.TRUE)
)
public class ReadyUpCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "ready";

    protected ReadyUpCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure sender is a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatConstantsService.getInstance().getChatPrefix() + ChatColor.RED + "You cannot execute this command from the console.");
            return true;
        }

        if (!ReadyUpService.getInstance().recordReady(player)) {
            Chat.sendAlert(player, ChatType.WARNING, "Nothing is waiting for you to be ready.");
        } else {
            Chat.sendAlert(player, ChatType.ACTIVE_INFO, "You are now ready!");
        }

        return true;
    }
}
