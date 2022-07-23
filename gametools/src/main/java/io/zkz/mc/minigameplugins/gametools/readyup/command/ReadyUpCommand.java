package io.zkz.mc.minigameplugins.gametools.readyup.command;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
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
            sender.sendMessage(MinigameConstantsService.getInstance().getPrefix() + ChatColor.RED + "You cannot execute this command from the console.");
            return true;
        }

        if (!ReadyUpService.getInstance().recordReady(player)) {
            sender.sendMessage(MinigameConstantsService.getInstance().getPrefix() + ChatColor.RED + "Nothing is waiting for you to be ready.");
        } else {
            sender.sendMessage(MinigameConstantsService.getInstance().getPrefix() + ChatColor.GRAY + "You are now ready!");
        }

        return true;
    }
}
