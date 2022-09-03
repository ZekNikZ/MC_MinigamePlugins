package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class BattleBoxCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SelectKitCommand());
    }
}
