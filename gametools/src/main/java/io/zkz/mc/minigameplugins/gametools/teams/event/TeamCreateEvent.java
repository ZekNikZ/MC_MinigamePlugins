package io.zkz.mc.minigameplugins.gametools.teams.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractEvent;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TeamCreateEvent extends AbstractEvent {
    private final List<GameTeam> teams;

    public TeamCreateEvent(Collection<GameTeam> teams) {
        this.teams = new ArrayList<>(teams);
    }

    public TeamCreateEvent(GameTeam... teams) {
        this.teams = Arrays.asList(teams);
    }

    public List<GameTeam> getTeams() {
        return this.teams;
    }
}
