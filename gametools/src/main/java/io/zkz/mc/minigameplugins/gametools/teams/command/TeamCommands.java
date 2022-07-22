package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class TeamCommands extends CommandGroup {
    public static class Permissions {
        public static final String TEAM_CREATE = "gametools.team.create";
        public static final String TEAM_JOIN = "gametools.team.join";
        public static final String TEAM_LEAVE = "gametools.team.leave";
        public static final String TEAM_RELOAD = "gametools.team.reload";
    }

    @Override
    public void registerCommands() {
        this.register(new DefaultTeamsCommand());
        this.register(new JoinTeamCommand());
        this.register(new LeaveTeamCommand());
        this.register(new ReloadTeamsCommand());
    }
}
