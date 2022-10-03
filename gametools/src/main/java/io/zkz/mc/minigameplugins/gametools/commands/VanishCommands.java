package io.zkz.mc.minigameplugins.gametools.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.arguments.selector.SinglePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.SinglePlayerSelectorArgument;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.List;
import java.util.Set;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@RegisterPermissions
public class VanishCommands {
    private static final Permission PERM_VANISH_SELF = new Permission("gametools.vanish.self", "Vanish self");
    private static final Permission PERM_VANISH_OTHERS = new Permission("gametools.vanish.others", "Vanish other players");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        Command.Builder<CommandSender> builder = registry.newBaseCommand("vanish", "v");

        // Base
        registry.registerCommand(
            builder
                .permission(PERM_VANISH_SELF.getName())
                .handler(cmd -> {
                    if (!(cmd.getSender() instanceof Player player)) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("you cannot use this command from the console."));
                        return;
                    }

                    Set<String> reasons = VanishingService.getInstance().getPlayerHiddenReasons(player).orElseGet(Set::of);
                    if (reasons.contains("manual")) {
                        // Already vanished
                        ActionBarService.getInstance().removeMessage(player.getUniqueId(), "vanish");
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().showPlayer(player, "manual"));
                    } else {
                        // Not already vanished
                        ActionBarService.getInstance().addMessage(player.getUniqueId(), "vanish", mm("<alert_warning>You are currently vanished."));
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().hidePlayer(player, "manual"));
                    }
                })
        );

        // Select reason
        registry.registerCommand(
            builder
                .permission(PERM_VANISH_SELF.getName())
                .literal("self")
                .argument(StringArgument.<CommandSender>newBuilder("key")
                    .withSuggestionsProvider((cmd, str) -> {
                        if (!(cmd.getSender() instanceof Player player)) {
                            return List.of();
                        }
                        return VanishingService.getInstance().getPlayerHiddenReasons(player.getUniqueId()).orElseGet(Set::of).stream().toList();
                    })
                    .single()
                    .asRequired()
                    .build()
                )
                .handler(cmd -> {
                    if (!(cmd.getSender() instanceof Player player)) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("you cannot use this command from the console."));
                        return;
                    }

                    String key = cmd.get("key");

                    Set<String> reasons = VanishingService.getInstance().getPlayerHiddenReasons(player).orElseGet(Set::of);
                    if (reasons.contains(key)) {
                        // Already vanished
                        if (key.equals("manual")) {
                            ActionBarService.getInstance().removeMessage(player.getUniqueId(), "vanish");
                        }
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().showPlayer(player, key));
                    } else {
                        // Not already vanished
                        if (key.equals("manual")) {
                            ActionBarService.getInstance().addMessage(player.getUniqueId(), "vanish", mm("<alert_warning>You are currently vanished."));
                        }
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().hidePlayer(player, key));
                    }
                })
        );


        // Select reason and player
        registry.registerCommand(
            builder
                .permission(PERM_VANISH_OTHERS.getName())
                .argument(SinglePlayerSelectorArgument.of("player"))
                .argument(StringArgument.<CommandSender>newBuilder("key")
                    .withSuggestionsProvider((cmd, str) -> {
                        SinglePlayerSelector p = cmd.get("player");
                        if (p.getPlayer() == null) {
                            return List.of();
                        }
                        return VanishingService.getInstance().getPlayerHiddenReasons(p.getPlayer().getUniqueId()).orElseGet(Set::of).stream().toList();
                    })
                    .single()
                    .asRequired()
                    .build()
                )
                .handler(cmd -> {
                    SinglePlayerSelector p = cmd.get("player");
                    Player player = p.getPlayer();
                    String key = cmd.get("key");

                    if (player == null) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("you cannot use this command from the console."));
                        return;
                    }

                    Set<String> reasons = VanishingService.getInstance().getPlayerHiddenReasons(player).orElseGet(Set::of);
                    if (reasons.contains(key)) {
                        // Already vanished
                        if (key.equals("manual")) {
                            ActionBarService.getInstance().removeMessage(player.getUniqueId(), "vanish");
                        }
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().showPlayer(player, key));
                    } else {
                        // Not already vanished
                        if (key.equals("manual")) {
                            ActionBarService.getInstance().addMessage(player.getUniqueId(), "vanish", mm("<alert_warning>You are currently vanished."));
                        }
                        BukkitUtils.runNextTick(() -> VanishingService.getInstance().hidePlayer(player, key));
                    }
                })
        );
    }
}
