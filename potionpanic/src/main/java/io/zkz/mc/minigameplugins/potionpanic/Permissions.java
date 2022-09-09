package io.zkz.mc.minigameplugins.potionpanic;

public class Permissions {
    private static final String PREFIX = "potionpanic.";
    private static final String ADMIN_PREFIX = PREFIX + "admin.";

    public static class Competitors {
        private static final String PREFIX = Permissions.PREFIX + "competitors.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "competitors.";

        public static final String SELECT = ADMIN_PREFIX + "select";
    }
}