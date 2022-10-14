package io.zkz.mc.minigameplugins.gametools.readyup;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmArgs;

@RegisterPermissions
public class ReadyUpCommands {
    private ReadyUpCommands() {
    }

    private static final Permission PERM_READY_BASE = new Permission("gametools.ready", "Ready up", PermissionDefault.TRUE);
    private static final Permission PERM_READY_STATUS = new Permission("gametools.ready.status", "See the ready status of the game");
    private static final Permission PERM_READY_UNDO = new Permission("gametools.ready.undo", "Undo the ready up of a player");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        var builder = registry.newConfirmableCommand("ready");

        // Ready up
        registry.registerCommand(
            builder
                .permission(PERM_READY_BASE.getName())
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    CommandSender sender = cmd.getSender();
                    if (!(sender instanceof Player player)) {
                        Chat.sendMessage(sender, ChatType.COMMAND_ERROR, mm("you cannot use this command from the console."));
                        return;
                    }

                    if (!ReadyUpService.getInstance().recordReady(player)) {
                        Chat.sendMessage(player, ChatType.COMMAND_ERROR, mm("nothing is waiting for you to be ready."));
                    } else {
                        Chat.sendMessage(player, ChatType.COMMAND_SUCCESS, mm("You are now ready!"));
                    }
                }))
        );

        // Status
        registry.registerCommand(
            builder.literal("status")
                .permission(PERM_READY_STATUS.getName())
                .handler(cmd -> BukkitUtils.runNow(() -> ReadyUpService.getInstance().sendStatus(cmd.getSender())))
        );

        // Undo
        registry.registerCommand(
            builder.literal("undo")
                .permission(PERM_READY_UNDO.getName())
                .argument(StringArgument.<CommandSender>newBuilder("player")
                    .single()
                    .withSuggestionsProvider((cmd, str) -> ReadyUpService.getInstance().getAllReadyPlayerNames().stream().toList())
                    .asRequired()
                    .build()
                )
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    CommandSender sender = cmd.getSender();

                    String playerName = cmd.get("player");
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

                    if (!ReadyUpService.getInstance().undoReady(player.getUniqueId())) {
                        Chat.sendMessage(sender, ChatType.COMMAND_ERROR, mm("that player was not marked as ready."));
                    } else {
                        Chat.sendMessage(sender, ChatType.COMMAND_SUCCESS, mmArgs("Marked player <0> as not ready.", playerName));
                    }
                }))
        );

        // Bypass
        registry.registerCommand(
            builder.literal("bypass")
                .meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    Map<Integer, ReadyUpSession> sessions = ReadyUpService.getInstance().getSessions();
                    sessions.values().forEach(ReadyUpSession::complete);
                }))
        );

        // Test
        // TODO: remove
        registry.registerCommand(
            builder.literal("test")
                .handler(cmd -> BukkitUtils.runNow(() ->
                    ReadyUpService.getInstance().waitForReady(
                        Bukkit.getOnlinePlayers().stream()
                            .map(Entity::getUniqueId).toList(),
                        () -> Bukkit.getServer().sendMessage(mm("<aqua>Done waiting for ready!"))
                    )))
        );
    }
}
