package io.zkz.mc.minigameplugins.gametools.readyup.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class ReadyUpCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new ReadyUpCommand());
    }
}
