package io.zkz.mc.minigameplugins.minigamemanager;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.round.PlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class Minigame<R extends Round> {
    public abstract List<List<Component>> rulesSlides();

    public abstract List<? extends R> rounds();

    public abstract int preRoundDelay();

    public abstract int postRoundDelay();

    public abstract int postGameDelay();

    public abstract Map<MinigameState, PlayerState> playerStates();

    public abstract void init();

    public Collection<UUID> participants() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                return team != null && !team.spectator();
            })
            .toList();
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

    @SuppressWarnings("unchecked")
    public final R getCurrentRound() {
        return (R) MinigameService.getInstance().getCurrentRound();
    }
}
