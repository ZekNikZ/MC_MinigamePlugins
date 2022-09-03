package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SelectKitCommand.COMMAND_NAME,
    desc = "Choose a kit",
    usage = "/" + SelectKitCommand.COMMAND_NAME + " <kit>",
    permission = Permissions.Kit.SELECT
))
@org.bukkit.plugin.java.annotation.permission.Permissions(
    @Permission(name = Permissions.Kit.SELECT, desc = "Select kit", defaultValue = PermissionDefault.TRUE)
)
public class SelectKitCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "selectkit";

    protected SelectKitCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            BattleBoxService.getInstance().getCurrentRound().assignKit(player, args[0]);
        }

        return true;
    }
}
