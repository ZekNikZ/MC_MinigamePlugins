package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;

public class LobbyCommands extends CommandGroup {
    static class Permissions {
        public static final String SERVER_SETUP = "lobby.server.setup";
        public static final String SERVER_REMOVE = "lobby.server.remove";
    }

    @Override
    public void registerCommands() {
//        this.register(new TestCommand());
        this.register(new SetupServerCommand());
        this.register(new RemoveServerCommand());
    }
}
