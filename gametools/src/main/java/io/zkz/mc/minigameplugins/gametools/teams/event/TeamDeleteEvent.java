package io.zkz.mc.minigameplugins.gametools.teams.event;

import io.zkz.mc.minigameplugins.gametools.event.AbstractEvent;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TeamDeleteEvent extends AbstractEvent {
    private final List<GameTeam> teams;

    public TeamDeleteEvent(Collection<GameTeam> teams) {
        this.teams = new ArrayList<>(teams);
    }

    public TeamDeleteEvent(GameTeam... teams) {
        this.teams = Arrays.asList(teams);
    }

    public List<GameTeam> getTeams() {
        return this.teams;
    }
}
