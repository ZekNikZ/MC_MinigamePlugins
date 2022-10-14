package io.zkz.mc.minigameplugins.uhc.game;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ComputableValueEntry;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.Minigame;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.DefaultScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.TeamBasedMinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.uhc.scoreboard.ScoreboardUpdateTask;
import io.zkz.mc.minigameplugins.uhc.scoreboard.TeamMembersEntry;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TeamStatus;
import io.zkz.mc.minigameplugins.uhc.task.GameOverEffects;
import io.zkz.mc.minigameplugins.uhc.task.UpdateSpectatorInventoriesTask;
import io.zkz.mc.minigameplugins.uhc.task.WorldBorderWarningTask;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class UHCMinigame extends Minigame<UHCRound> {
    @Override
    public @NotNull List<List<Component>> buildRulesSlides() {
        return List.of(
        );
    }

    @Override
    public @NotNull List<? extends UHCRound> buildRounds() {
        return List.of(new UHCRound());
    }

    @Override
    public boolean isTeamGame() {
        return SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME;
    }

    @Override
    public void handleMinigameOver() {
    }

    @Override
    public boolean getReadyUpEachRound() {
        return true;
    }

    @Override
    public boolean getShowScoreSummary() {
        return false;
    }

    @Override
    public boolean getAutomaticShowRules() {
        return false;
    }

    @Override
    public @Nullable MinigameScoreboard buildScoreboard(MinigameState state) {
        return (TeamBasedMinigameScoreboard) team -> {
            GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(mm("<legacy_gold><bold>UHC"));

            // Timer
            DefaultScoreboard.addMinigameTimer(scoreboard);

            // World border
            if (state.isInGame()) {
                scoreboard.addEntry("worldborder", new ComputableValueEntry<>("<legacy_green><bold>Worldborder: </bold></legacy_green> \u00B1<value>", () -> this.getCurrentRound().getCurrentWorldborderSize() / 2));
                scoreboard.addSpace();
            }

            // Alive people
            if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
                scoreboard.addEntry(new ComputableValueEntry<>("<legacy_green><bold>Alive teams: </bold></legacy_green> <value>", () -> this.getCurrentRound().getAliveTeams().size()));
            }
            scoreboard.addEntry(new ComputableValueEntry<>("<legacy_green><bold>Alive players: </bold></legacy_green> <value>", () -> this.getCurrentRound().getAlivePlayers().size()));

            // Team members
            if (team != null && SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_aqua><bold>Team members:"));
                scoreboard.addEntry(new TeamMembersEntry(team));
            }

            ScoreboardService.getInstance().setTeamScoreboard(team.id(), scoreboard);
            ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
        };
    }

    @Override
    public @NotNull List<Supplier<? extends MinigameTask>> buildTasks(MinigameState state) {
        if (state.isInGame()) {
            return List.of(
                ScoreboardUpdateTask::new,
                UpdateSpectatorInventoriesTask::new,
                WorldBorderWarningTask::new
                // TODO: worldborder state change
            );
        }
        if (state == MinigameState.POST_ROUND) {
            return List.of(
                GameOverEffects::new
            );
        }
        return List.of();
    }

    @Override
    public @Nullable IPlayerState buildPlayerState(MinigameState state) {
        return switch (state) {
            case SERVER_STARTING, LOADING, SETUP, WAITING_FOR_PLAYERS, WAITING_TO_BEGIN, RULES -> null;
            // TODO: gamemodes, location, etc.
            default -> null;
        };
    }


}
