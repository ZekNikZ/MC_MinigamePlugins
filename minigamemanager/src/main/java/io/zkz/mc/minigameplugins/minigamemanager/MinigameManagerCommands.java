package io.zkz.mc.minigameplugins.minigamemanager;

import cloud.commandframework.arguments.standard.IntegerArgument;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@RegisterPermissions
public class MinigameManagerCommands {
    private static final Permission PERM_ROUND_CHANGE = new Permission("Change Round", "Change the current round.");

    private static final Permission PERM_STATE_CHANGE = new Permission("Change State", "Change current state.");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        var roundBuilder = registry.newBaseCommand("round");

        // Set round
        registry.registerCommand(
            roundBuilder.literal("set")
                .permission(PERM_ROUND_CHANGE.getName())
                .argument(IntegerArgument.of("round"))
                .handler(cmd -> {
                    int round = cmd.get("round");

                    if (round < 0 || round >= MinigameService.getInstance().getRoundCount()) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("<0> is not a registered round.", Component.text(round)));
                        return;
                    }

                    MinigameService.getInstance().getCurrentRound().onEnterPostRound();
                    MinigameService.getInstance().setCurrentRound(round);
                    MinigameService.getInstance().setState(MinigameState.PRE_ROUND);
                })
        );

        // Next round
        registry.registerCommand(
            roundBuilder.literal("next")
                .permission(PERM_ROUND_CHANGE.getName())
                .handler(cmd -> {
                    if (!TeamService.getInstance().areAllNonSpectatorsOnline()) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: all participants are not online. Either remove offline players from teams or wait for all players to be present."));
                        return;
                    } else if (Bukkit.getOnlinePlayers().stream().anyMatch(p -> TeamService.getInstance().getTeamOfPlayer(p) == null)) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: someone is not on a team."));
                        return;
                    } else if (MinigameService.getInstance().getCurrentState() != MinigameState.POST_ROUND) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: you can only use this command in the POST_ROUND state, did you mean /donewaitingforplayers?"));
                        return;
                    }

                    MinigameService.getInstance().goToNextRound();
                })
        );

        // Done waiting for players
        registry.registerCommand(
            registry.newBaseCommand("donewaitingforplayers")
                .permission(PERM_STATE_CHANGE.getName())
                .handler(cmd -> {
                    if (!TeamService.getInstance().areAllNonSpectatorsOnline()) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: all participants are not online. Either remove offline players from teams or wait for all players to be present."));
                        return;
                    } else if (Bukkit.getOnlinePlayers().stream().anyMatch(p -> TeamService.getInstance().getTeamOfPlayer(p) == null)) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: someone is not on a team."));
                        return;
                    } else if (MinigameService.getInstance().getCurrentState() != MinigameState.WAITING_FOR_PLAYERS) {
                        Chat.sendMessage(cmd.getSender(), ChatType.COMMAND_ERROR, mm("cannot transition states: you can only use this command in the WAITING_FOR_PLAYERS state, did you mean /nextround?"));
                        return;
                    }

                    MinigameService.getInstance().markDoneWaitingForPlayers();
                })
        );
    }
}
