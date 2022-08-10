package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class MinigameCommands extends CommandGroup {
    public static class Permissions {
        public static final String ADD_POINTS = "minigamemanager.score.add";
        public static final String SET_ROUND = "minigamemanager.round.set";
    }

    @Override
    public void registerCommands() {
        this.register(new AddPointsCommand());
        this.register(new SetRoundCommand());
    }
}
