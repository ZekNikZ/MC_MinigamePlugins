package io.zkz.mc.minigameplugins.bingo;

public class Permissions {
    private static final String PREFIX = "bingo.";
    private static final String ADMIN_PREFIX = PREFIX + "admin.";

    public static class Card {
        private static final String PREFIX = Permissions.PREFIX + "card.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "card.";

        public static final String RANDOMIZE = ADMIN_PREFIX + "randomize";
    }
}
