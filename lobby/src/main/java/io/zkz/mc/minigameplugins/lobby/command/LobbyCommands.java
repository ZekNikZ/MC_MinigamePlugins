package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class LobbyCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new TestCommand());
    }
}
