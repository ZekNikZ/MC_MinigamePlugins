package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class GuessTheBuildCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SetFloorCommand());
    }
}
