package io.zkz.mc.minigameplugins.uhc.game;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ComputableValueEntry;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.Minigame;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.DefaultScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.TeamBasedMinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.uhc.scoreboard.ScoreboardUpdateTask;
import io.zkz.mc.minigameplugins.uhc.scoreboard.TeamMembersEntry;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TeamStatus;
import io.zkz.mc.minigameplugins.uhc.task.*;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class UHCMinigame extends Minigame<UHCRound> {
    @Override
    public String getMinigameName() {
        return "UHC";
    }

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
    public boolean getAutomaticNextRound() {
        return false;
    }

    @Override
    public @Nullable MinigameScoreboard buildScoreboard(MinigameState state) {
        return (TeamBasedMinigameScoreboard) team -> {
            GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(mm("<legacy_gold><bold>UHC"));

            // Timer
            DefaultScoreboard.addMinigameTimer(scoreboard);
            scoreboard.addEntry(new ComputableValueEntry<>("<legacy_red><bold>In-game time:</bold></legacy_red> <value>", () -> {
                long time = WorldSyncUtils.getTime();
                long adjustedTime = (time + 6000) % 24000;
                long hours = adjustedTime / 1000;
                long minutes = (adjustedTime % 1000) * 60 / 1000;
                return String.format("%02d:%02d", hours, minutes);
            }));

            // World border
            if (state.isInGame() || state == MinigameState.PRE_ROUND || state == MinigameState.POST_ROUND || state == MinigameState.POST_GAME) {
                scoreboard.addEntry("worldborder", new ComputableValueEntry<>("<legacy_aqua><bold>Worldborder:</bold></legacy_aqua> \u00B1<value>", () -> this.getCurrentRound().getCurrentWorldborderSize() / 2));
            }

            // Alive people
            scoreboard.addSpace();
            if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
                scoreboard.addEntry(new ComputableValueEntry<>("<legacy_green><bold>Alive teams:</bold></legacy_green> <value>", () -> this.getCurrentRound().getAliveTeams().size()));
            }
            scoreboard.addEntry(new ComputableValueEntry<>("<legacy_green><bold>Alive players:</bold></legacy_green> <value>", () -> this.getCurrentRound().getAlivePlayers().size()));

            // Team members
            if (team != null && SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_aqua><bold>Team members:"));
                scoreboard.addEntry(new TeamMembersEntry(team));
            }

            // Tab list
            scoreboard.setTabListObjective("hp", Criteria.HEALTH, mm("HP"), RenderType.HEARTS);

            if (team != null) {
                ScoreboardService.getInstance().setTeamScoreboard(team.id(), scoreboard);
            } else {
                ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
            }
        };
    }

    @Override
    public @NotNull List<Supplier<? extends MinigameTask>> buildTasks(MinigameState state) {
        if (state.isInGame()) {
            return List.of(
                ScoreboardUpdateTask::new,
                UpdateSpectatorInventoriesTask::new,
                WorldBorderWarningTask::new,
                WorldBorderStateCheckerTask::new,
                CoordinatesHudTask::new
            );
        }
        if (state == MinigameState.POST_ROUND) {
            return List.of(
                GameOverEffects::new,
                CoordinatesHudTask::new
            );
        }
        return List.of(
            CoordinatesHudTask::new
        );
    }
}
