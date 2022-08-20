package io.zkz.mc.minigameplugins.survivalgames;

public class Permissions {
    private static final String PREFIX = "survivalgames.";
    private static final String ADMIN_PREFIX = PREFIX + "survivalgames.";

    public static class Spectate {
        private static final String PREFIX = Permissions.PREFIX + "spectate.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "spectate.";

        public static final String START = PREFIX + "start";
        public static final String STOP = PREFIX + "stop";
    }

    public static class Event {
        private static final String PREFIX = Permissions.PREFIX + "event.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "event.";

        public static final String SUDDEN_DEATH = ADMIN_PREFIX + "suddendeath";
        public static final String RESPAWN = ADMIN_PREFIX + "respawn";
    }
}