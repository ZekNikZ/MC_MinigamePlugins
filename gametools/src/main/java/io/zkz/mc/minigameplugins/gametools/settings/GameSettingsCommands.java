package io.zkz.mc.minigameplugins.gametools.settings;

import cloud.commandframework.Command;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@RegisterPermissions
public class GameSettingsCommands {
    private GameSettingsCommands() {
    }

    private static final Permission PERM_CHANGE_SETTINGS = new Permission("gametools.settings.change", "Change game settings");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        Command.Builder<CommandSender> builder = registry.newBaseCommand("settings");

        registry.registerCommand(
            builder
                .permission(PERM_CHANGE_SETTINGS.getName())
                .handler(cmd -> {
                    CommandSender sender = cmd.getSender();
                    if (!(sender instanceof Player player)) {
                        Chat.sendMessage(sender, ChatType.COMMAND_ERROR, mm("you cannot use this command from the console."));
                        return;
                    }

                    GameSettingsService.getInstance().openMenu(player);
                })
        );
    }
}
