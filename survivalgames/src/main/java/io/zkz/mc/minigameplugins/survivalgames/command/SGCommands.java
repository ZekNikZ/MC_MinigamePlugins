package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class SGCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SpecCommand());
        this.register(new StopSpecCommand());
        this.register(new SuddenDeathCommand());
        this.register(new RespawnCommand());
    }
}
