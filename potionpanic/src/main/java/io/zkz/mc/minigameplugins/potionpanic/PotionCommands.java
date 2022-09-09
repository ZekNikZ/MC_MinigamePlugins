package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class PotionCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SelectTeamsCommand());
    }
}
