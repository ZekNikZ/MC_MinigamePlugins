package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class MinigameCommands extends CommandGroup {
    public static class Permissions {
        public static final String ADD_POINTS = "minigamemanager.score.add";
    }

    @Override
    public void registerCommands() {
        this.register(new AddPointsCommand());
    }
}
