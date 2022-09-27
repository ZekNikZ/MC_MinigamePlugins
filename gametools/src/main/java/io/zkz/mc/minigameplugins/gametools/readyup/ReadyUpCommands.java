package io.zkz.mc.minigameplugins.gametools.readyup;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.kyori.adventure.audience.Audience;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import static io.zkz.mc.minigameplugins.gametools.command.CommandHelpers.hasPermissionOrOp;
import static io.zkz.mc.minigameplugins.gametools.command.CommandHelpers.suggestions;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@RegisterPermissions
public class ReadyUpCommands {
    private static final Permission PERM_READY_BASE = new Permission("gametools.ready", "Ready up", PermissionDefault.TRUE);
    private static final Permission PERM_READY_STATUS = new Permission("gametools.ready.status", "See the ready status of the game");
    private static final Permission PERM_READY_UNDO = new Permission("gametools.ready.undo", "Undo the ready up of a player");

//    @RegisterCommands
//    private static void registerCommands(CommandRegistry registry) {
//        registry.register(Commands.literal("ready")
//            .requires(hasPermissionOrOp(PERM_READY_BASE))
//            .executes(cmd -> {
//                CommandSender sender = cmd.getSource().getBukkitSender();
//                if (!(sender instanceof Player player)) {
//                    sender.sendMessage(mm("<error>You cannot use this command from the console!"));
//                    return 1;
//                }
//
//                if (!ReadyUpService.getInstance().recordReady(player)) {
//                    Chat.sendMessage(player, ChatType.WARNING, mm("Nothing is waiting for you to be ready."));
//                } else {
//                    Chat.sendMessage(player, ChatType.ACTIVE_INFO, mm("You are now ready!"));
//                }
//
//                return 1;
//            })
//            .then(Commands.literal("status")
//                .requires(hasPermissionOrOp(PERM_READY_STATUS))
//                .executes(cmd -> {
//                    ReadyUpService.getInstance().sendStatus(cmd.getSource().getBukkitSender());
//                    return 1;
//                })
//            )
//            .then(Commands.literal("undo")
//                .requires(hasPermissionOrOp(PERM_READY_UNDO))
//                .then(Commands.argument("playerId", StringArgumentType.word())
//                    .suggests(suggestions(() -> ReadyUpService.getInstance().getAllReadyPlayerNames()))
//                    .executes(cmd -> {
//                        CommandSender sender = cmd.getSource().getBukkitSender();
//
//                        OfflinePlayer player = Bukkit.getOfflinePlayer(cmd.getArgument("playerId", String.class));
//
//                        if (!ReadyUpService.getInstance().undoReady(player.getUniqueId())) {
//                            Chat.sendMessage(sender, ChatType.WARNING, mm("That player was not marked as ready."));
//                        } else {
//                            Chat.sendMessage(sender, ChatType.ACTIVE_INFO, mm("Marked that player as not ready"));
//                        }
//
//                        return 1;
//                    })
//                )
//            )
//            .then(Commands.literal("test")
//                .executes(cmd -> {
//                    ReadyUpService.getInstance().waitForReady(Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).toList(), () -> {
//                        Audience.audience(Bukkit.getOnlinePlayers()).sendMessage(mm("<aqua>Done waiting for ready!"));
//                    });
//
//                    return 1;
//                })
//            )
//        );
//    }
}
