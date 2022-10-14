package io.zkz.mc.minigameplugins.gametools.teams;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import cloud.commandframework.minecraft.extras.TextColorArgument;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.command.arguments.GTColorArgument;
import io.zkz.mc.minigameplugins.gametools.command.arguments.TeamArgument;
import io.zkz.mc.minigameplugins.gametools.command.arguments.TextComponentArgument;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmArgs;

@RegisterPermissions
public class TeamCommands {
    private TeamCommands() {
    }

    private static final Permission PERM_CREATE_DEFAULTS = new Permission("gametools.team.create.default", "Set up default teams");
    private static final Permission PERM_CREATE = new Permission("gametools.team.create", "Create teams");
    private static final Permission PERM_REMOVE = new Permission("gametools.team.remove", "Remove teams");
    private static final Permission PERM_JOIN = new Permission("gametools.team.join", "Add players to teams");
    private static final Permission PERM_LEAVE = new Permission("gametools.team.leave", "Remove players from teams");
    private static final Permission PERM_RELOAD = new Permission("gametools.team.reload", "Reload team data");
    private static final Permission PERM_LIST = new Permission("gametools.team.list", "List registered teams");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        Command.Builder<CommandSender> builder = registry.newBaseCommand("gteam");

        // Setup default teams
        registry.registerCommand(
            builder.literal("defaults")
                .permission(PERM_CREATE_DEFAULTS.getName())
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    CommandSender sender = cmd.getSender();

                    try {
                        TeamService.getInstance().setupDefaultTeams();
                    } catch (TeamService.TeamCreationException exception) {
                        Chat.sendMessage(sender, ChatType.COMMAND_ERROR, exception, mm("could not set up default teams."));
                    }

                    Chat.sendMessage(sender, ChatType.COMMAND_SUCCESS, mm("Successfully set up default teams."));
                }))
        );

        // Join team
        registry.registerCommand(
            builder.literal("join")
                .permission(PERM_JOIN.getName())
                .argument(TeamArgument.of("team"))
                .argument(MultiplePlayerSelectorArgument.of("players"))
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    CommandSender sender = cmd.getSender();
                    GameTeam team = cmd.get("team");
                    MultiplePlayerSelector players = cmd.get("players");
                    players.getPlayers().forEach(p -> {
                        team.addMember(p.getUniqueId());
                        Chat.sendMessage(sender, ChatType.COMMAND_SUCCESS, mm("Added player '<0>' to team '<1>'.", mm(p.getName()), team.getDisplayName()));
                    });
                }))
        );

        // Leave team
        registry.registerCommand(
            builder.literal("leave")
                .permission(PERM_LEAVE.getName())
                .argument(MultiplePlayerSelectorArgument.of("players"))
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    CommandSender sender = cmd.getSender();
                    MultiplePlayerSelector players = cmd.get("players");
                    players.getPlayers().forEach(p -> {
                        TeamService.getInstance().leaveTeam(p.getUniqueId());
                        Chat.sendMessage(sender, ChatType.COMMAND_SUCCESS, mm("Removed player '<0>' from their team.", mm(p.getName())));
                    });
                }))
        );

        // Reload teams
        registry.registerCommand(
            builder.literal("reload")
                .permission(PERM_RELOAD.getName())
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    TeamService.getInstance().loadAllData();
                    Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_SUCCESS, mm("Successfully reloaded teams."));
                }))
        );

        // List teams
        registry.registerCommand(
            builder.literal("list")
                .permission(PERM_LIST.getName())
                .handler(cmd -> BukkitUtils.runNow(() -> Chat.sendMessage(
                    cmd.getSender(),
                    TeamService.getInstance().getAllTeams().stream()
                        .map(team -> mm("<0> (<1>)", team.getDisplayName(), mm(team.id())))
                        .collect(ComponentUtils.joining(mm(", ")))
                )))
        );

        // Create team
        final CommandFlag<Component> prefixFlag = registry.newFlag("prefix", TextComponentArgument.of("prefix")).build();
        final CommandFlag<GTColor> colorFlag = registry.newFlag("color", GTColorArgument.of("color")).build();
        final CommandFlag<String> formatTagFlag = registry.newFlag("formatTag", StringArgument.of("formatTag")).build();
        final CommandFlag<TextColor> scoreboardColorFlag = registry.newFlag("scoreboardColor", TextColorArgument.of("scoreboardColor")).build();
        registry.registerCommand(
            builder.literal("create", "new")
                .permission(PERM_CREATE.getName())
                .argument(StringArgument.of("id"))
                .argument(TextComponentArgument.of("name"))
                .flag(prefixFlag)
                .flag(colorFlag)
                .flag(formatTagFlag)
                .flag(scoreboardColorFlag)
                .flag(registry.newFlag("spectator"))
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    GameTeam.Builder teamBuilder = GameTeam.builder(cmd.get("id"), cmd.get("name"));
                    cmd.flags().getValue(prefixFlag).ifPresent(teamBuilder::prefix);
                    cmd.flags().getValue(colorFlag).ifPresent(teamBuilder::color);
                    cmd.flags().getValue(formatTagFlag).ifPresent(teamBuilder::formatTag);
                    cmd.flags().getValue(scoreboardColorFlag).ifPresent(scoreboardColor -> teamBuilder.scoreboardColor(NamedTextColor.nearestTo(scoreboardColor)));
                    if (cmd.flags().isPresent("spectator")) {
                        teamBuilder.spectator(true);
                    }

                    GameTeam team = teamBuilder.build();

                    try {
                        TeamService.getInstance().createTeam(team);
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_SUCCESS, mmArgs("Successfully created team with id '<0>'.", team.id()));
                    } catch (TeamService.TeamCreationException e) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, e, mmArgs("could not create team with id '<0>'.", team.id()));
                    }
                }))
        );

        registry.registerCommand(
            builder.literal("remove", "delete")
                .permission(PERM_REMOVE.getName())
                .argument(TeamArgument.of("team"))
                .handler(cmd -> BukkitUtils.runNow(() -> {
                    GameTeam team = cmd.get("team");
                    TeamService.getInstance().removeTeam(team.id());
                    Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_SUCCESS, mmArgs("Successfully removed team with id '<0>'.", team.id()));
                }))
        );
    }
}
