package io.zkz.mc.minigameplugins.dev.survivalgames;

public class SGCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SGDevCommand());
        this.register(new PopulateChestsCommand());
    }
}
