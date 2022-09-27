package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.command.arguments.PlayerArgument;
import io.zkz.mc.minigameplugins.gametools.command.arguments.TeamArgument;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.commands.Commands;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.util.Collection;

import static io.zkz.mc.minigameplugins.gametools.command.CommandHelpers.hasPermissionOrOp;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmResolve;

@RegisterPermissions
public class TeamCommands {
    private static final Permission PERM_CREATE_DEFAULTS = new Permission("gametools.team.create.default", "Set up default teams");
    private static final Permission PERM_CREATE = new Permission("gametools.team.create", "Create teams");
    private static final Permission PERM_REMOVE = new Permission("gametools.team.remove", "Remove teams");
    private static final Permission PERM_JOIN = new Permission("gametools.team.join", "Add players to teams");
    private static final Permission PERM_LEAVE = new Permission("gametools.team.leave", "Remove players from teams");
    private static final Permission PERM_RELOAD = new Permission("gametools.team.reload", "Reload team data");
    private static final Permission PERM_LIST = new Permission("gametools.team.list", "List registered teams");

//    @RegisterCommands
//    private static void registerCommands(CommandRegistry registry) {
//        registry.register(Commands.literal("gteam")
//            .then(Commands.literal("defaults")
//                .requires(hasPermissionOrOp(PERM_CREATE_DEFAULTS))
//                .executes(cmd -> {
//                    CommandSender sender = cmd.getSource().getBukkitSender();
//
//                    try {
//                        TeamService.getInstance().setupDefaultTeams();
//                    } catch (TeamService.TeamCreationException exception) {
//                        Chat.sendMessage(sender, ChatType.GAME_INFO, mm("<red>Error: could not set up default teams."));
//                        Chat.sendMessage(sender, ChatType.GAME_INFO, mmResolve("<red><exmsg>", Placeholder.unparsed("exmsg", exception.getMessage())));
//                        return 0;
//                    }
//
//                    Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Successfully set up default teams."));
//
//                    return 1;
//                })
//            )
//            .then(Commands.literal("join")
//                .requires(hasPermissionOrOp(PERM_JOIN))
//                .then(Commands.argument("team", TeamArgument.team())
//                    .then(Commands.argument("players", PlayerArgument.players())
//                        .executes(cmd -> {
//                            CommandSender sender = cmd.getSource().getBukkitSender();
//                            GameTeam team = TeamArgument.getTeam(cmd, "team");
//                            Collection<OfflinePlayer> players = PlayerArgument.getPlayers(cmd, "players");
//
//                            players.forEach(p -> {
//                                team.addMember(p.getUniqueId());
//                                Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Added player '<0>' to team '<1>'.", mm(p.getName()), team.getDisplayName()));
//                            });
//
//                            return 1;
//                        })
//                    )
//                )
//            )
//            .then(Commands.literal("leave")
//                .requires(hasPermissionOrOp(PERM_LEAVE))
//                .then(Commands.argument("players", PlayerArgument.players())
//                    .executes(cmd -> {
//                        CommandSender sender = cmd.getSource().getBukkitSender();
//                        Collection<OfflinePlayer> players = PlayerArgument.getPlayers(cmd, "players");
//
//                        players.forEach(p -> {
//                            TeamService.getInstance().leaveTeam(p.getUniqueId());
//                            Chat.sendMessage(sender, ChatType.GAME_INFO, mm("Removed player '<0>' from their team.", mm(p.getName())));
//                        });
//
//                        return 1;
//                    })
//                )
//            )
//            .then(Commands.literal("reload")
//                .requires(hasPermissionOrOp(PERM_RELOAD))
//                .executes(cmd -> {
//                    // TODO: reload teams command
//                    return 0;
//                })
//            )
//            .then(Commands.literal("list")
//                .requires(hasPermissionOrOp(PERM_LIST))
//                .executes(cmd -> {
//                    // TODO: list teams command
//                    return 0;
//                })
//            )
//            .then(Commands.literal("create")
//                .requires(hasPermissionOrOp(PERM_CREATE))
//                .executes(cmd -> {
//                    // TODO: create teams command
//                    return 0;
//                })
//            )
//            .then(Commands.literal("remove")
//                .requires(hasPermissionOrOp(PERM_REMOVE))
//                .executes(cmd -> {
//                    // TODO: delete teams command
//                    return 0;
//                })
//            )
//        );
//    }
}
