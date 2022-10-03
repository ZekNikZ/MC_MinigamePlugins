package io.zkz.mc.minigameplugins.potionpanic;

public class PotionCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SelectTeamsCommand());
    }
}
