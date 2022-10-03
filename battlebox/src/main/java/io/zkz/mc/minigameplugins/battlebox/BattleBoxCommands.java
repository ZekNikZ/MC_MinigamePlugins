package io.zkz.mc.minigameplugins.battlebox;

public class BattleBoxCommands extends CommandGroup {
    @Override
    public void registerCommands() {
        this.register(new SelectKitCommand());
    }
}
