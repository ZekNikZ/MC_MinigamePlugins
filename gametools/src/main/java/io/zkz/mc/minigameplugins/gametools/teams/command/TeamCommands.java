package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class TeamCommands extends CommandGroup {
    public static class Permissions {
        public static final String TEAM_CREATE = "gametools.team.create";
    }

    @Override
    public void registerCommands() {
        this.register(new DefaultTeamsCommand());
    }
}
