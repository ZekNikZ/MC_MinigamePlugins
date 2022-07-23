package io.zkz.mc.minigameplugins.gametools.readyup.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class ReadyUpCommands extends CommandGroup {
    public static class Permissions {
        public static final String READY_UP = "gametools.readyup";
    }

    @Override
    public void registerCommands() {
        this.register(new ReadyUpCommand());
    }
}
