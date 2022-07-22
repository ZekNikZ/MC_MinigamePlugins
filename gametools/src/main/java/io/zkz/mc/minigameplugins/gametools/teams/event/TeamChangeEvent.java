package io.zkz.mc.minigameplugins.gametools.teams.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractEvent;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;

import java.util.*;

public class TeamChangeEvent extends AbstractEvent {
    private final List<UUID> players;
    private final GameTeam oldTeam;
    private final GameTeam newTeam;

    public TeamChangeEvent(GameTeam oldTeam, GameTeam newTeam, UUID... players) {
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
        this.players = Arrays.asList(players);
    }

    public TeamChangeEvent(GameTeam oldTeam, GameTeam newTeam, Collection<UUID> players) {
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
        this.players = new ArrayList<>(players);
    }

    public List<UUID> getPlayers() {
        return this.players;
    }

    public GameTeam getOldTeam() {
        return this.oldTeam;
    }

    public GameTeam getNewTeam() {
        return this.newTeam;
    }
}
