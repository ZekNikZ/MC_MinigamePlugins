package io.zkz.mc.minigameplugins.bingo.command;

public class BingoCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new RandomizeCardCommand());
    }
}
