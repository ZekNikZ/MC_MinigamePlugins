package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class MinigameCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new AddPointsCommand());
        this.register(new SetRoundCommand());
        this.register(new DoneWaitingForPlayersCommand());
        this.register(new NextRoundCommand());
    }
}
