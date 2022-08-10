package io.zkz.mc.minigameplugins.bingo.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class BingoCommands extends CommandGroup {
    static class Permissions {
        public static final String RANDOMIZE_CARD = "bingo.card.randomize";
    }

    @Override
    public void registerCommands() {
        this.register(new RandomizeCardCommand());
    }
}
