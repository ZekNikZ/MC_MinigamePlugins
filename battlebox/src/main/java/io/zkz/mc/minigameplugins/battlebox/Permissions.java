package io.zkz.mc.minigameplugins.battlebox;

public class Permissions {
    private static final String PREFIX = "battlebox.";
    private static final String ADMIN_PREFIX = PREFIX + "admin.";

    public static class Kit {
        private static final String PREFIX = Permissions.PREFIX + "kit.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "kit.";

        public static final String SELECT = ADMIN_PREFIX + "select";
    }
}
