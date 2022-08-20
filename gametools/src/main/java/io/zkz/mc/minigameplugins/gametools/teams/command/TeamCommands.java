package io.zkz.mc.minigameplugins.gametools.teams.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class TeamCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new DefaultTeamsCommand());
        this.register(new JoinTeamCommand());
        this.register(new LeaveTeamCommand());
        this.register(new ReloadTeamsCommand());
    }
}
