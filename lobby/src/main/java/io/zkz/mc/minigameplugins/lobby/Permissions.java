package io.zkz.mc.minigameplugins.lobby;

public class Permissions {
    private static final String PREFIX = "lobby.";
    private static final String ADMIN_PREFIX = PREFIX + "admin.";

    public static class Minigame {
        private static final String PREFIX = Permissions.PREFIX + "minigame.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "minigame.";

        public static final String SETUP = ADMIN_PREFIX + "setup";
        public static final String RESET = ADMIN_PREFIX + "reset";
    }
}
