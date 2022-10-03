package io.zkz.mc.minigameplugins.minigamemanager.command;

public class MinigameCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new AddPointsCommand());
        this.register(new SetRoundCommand());
        this.register(new DoneWaitingForPlayersCommand());
        this.register(new NextRoundCommand());
        this.register(new SetMultiplierCommand());
    }
}
