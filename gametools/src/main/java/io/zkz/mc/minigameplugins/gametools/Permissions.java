package io.zkz.mc.minigameplugins.gametools;

public class Permissions {
    private static final String PREFIX = "gametools.";
    private static final String ADMIN_PREFIX = PREFIX + "admin.";

    public static class Ready {
        private static final String PREFIX = Permissions.PREFIX + "ready.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "ready.";

        public static final String READY_UP = PREFIX + "readyup";
        public static final String UNDO_READY_UP = ADMIN_PREFIX + "undoreadyup";
        public static final String STATUS = ADMIN_PREFIX + "status";
    }

    public static class Teams {
        private static final String PREFIX = Permissions.PREFIX + "teams.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "teams.";

        public static final String CREATE = ADMIN_PREFIX + "create";
        public static final String JOIN = ADMIN_PREFIX + "join";
        public static final String LEAVE = ADMIN_PREFIX + "leave";
        public static final String RELOAD = ADMIN_PREFIX + "reload";
    }
}
