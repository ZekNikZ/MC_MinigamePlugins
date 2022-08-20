package io.zkz.mc.minigameplugins.bingo.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class BingoCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new RandomizeCardCommand());
    }
}
