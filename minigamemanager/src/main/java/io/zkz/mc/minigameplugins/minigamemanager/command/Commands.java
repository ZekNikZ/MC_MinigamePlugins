package io.zkz.mc.minigameplugins.minigamemanager.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class Commands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new AddPointsCommand());
    }
}
