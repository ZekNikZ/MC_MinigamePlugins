package io.zkz.mc.minigameplugins.guessthebuild;

public class GuessTheBuildCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SetFloorCommand());
    }
}
