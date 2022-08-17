package io.zkz.mc.minigameplugins.dev.survivalgames;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class SGCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SGDevCommand());
    }
}
