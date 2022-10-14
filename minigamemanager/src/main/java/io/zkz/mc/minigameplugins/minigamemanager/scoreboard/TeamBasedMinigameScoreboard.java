package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;

@FunctionalInterface
public interface TeamBasedMinigameScoreboard extends MinigameScoreboard {
    void apply (GameTeam team) ;

    @Override
    default void setup() {
        TeamService.getInstance().getAllTeams().forEach(this::apply);
        this.apply(null);
    }
}
