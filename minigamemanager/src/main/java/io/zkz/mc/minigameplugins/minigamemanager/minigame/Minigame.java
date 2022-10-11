package io.zkz.mc.minigameplugins.minigamemanager.minigame;

import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.state.PlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class Minigame<R extends Round> {
    public abstract List<List<Component>> buildRulesSlides();

    public abstract List<? extends R> buildRounds();

    public abstract Map<MinigameState, PlayerState> buildPlayerStates();

    public abstract int getPreRoundDelay();

    public abstract int getPostRoundDelay();

    public abstract int getPostGameDelay();

    /**
     * Call addSetupHandler, addCleanupHandler, and addTask here
     */
    public abstract void init();

    /**
     * Override this if not all non-spectators are participating
     *
     * @return the list of player UUIDs that are participating in the minigame
     */
    public Collection<UUID> getParticipants() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                return team != null && !team.spectator();
            })
            .toList();
    }

    public Collection<UUID> getPlayersAndGameMasters() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                return team != null && (team.equals(DefaultTeams.GAME_MASTER) || !team.spectator());
            })
            .toList();
    }

    @SuppressWarnings("unchecked")
    public final R getCurrentRound() {
        return (R) MinigameService.getInstance().getCurrentRound();
    }

    protected final void addSetupHandler(MinigameState state, Runnable handler) {
        MinigameService.getInstance().addSetupHandler(state, handler);
    }

    protected final void addCleanupHandler(MinigameState state, Runnable handler) {
        MinigameService.getInstance().addCleanupHandler(state, handler);
    }

    protected final void addTask(MinigameState state, Supplier<MinigameTask> taskSupplier) {
        MinigameService.getInstance().addTask(state, taskSupplier);
    }
}
