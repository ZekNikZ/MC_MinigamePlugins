package io.zkz.mc.minigameplugins.minigamemanager;

public class Permissions {
    private static final String PREFIX = "minigame.";
    private static final String ADMIN_PREFIX = PREFIX + "minigame.";

    public static class Round {
        private static final String PREFIX = Permissions.PREFIX + "round.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "round.";

        public static final String CHANGE = ADMIN_PREFIX + "change";
        public static final String NEXT = ADMIN_PREFIX + "next";
    }

    public static class Score {
        private static final String PREFIX = Permissions.PREFIX + "score.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "score.";

        public static final String ADD = ADMIN_PREFIX + "add";
        public static final String MULTIPLIER = ADMIN_PREFIX + "multiplier";
    }

    public static class State {
        private static final String PREFIX = Permissions.PREFIX + "state.";
        private static final String ADMIN_PREFIX = Permissions.ADMIN_PREFIX + "state.";

        public static class Enter {
            private static final String PREFIX = Permissions.State.PREFIX + "enter.";
            private static final String ADMIN_PREFIX = Permissions.State.ADMIN_PREFIX + "enter.";

            public static final String SERVER_STARTING = ADMIN_PREFIX + "server_starting";
            public static final String LOADING = ADMIN_PREFIX + "loading";
            public static final String SETUP = ADMIN_PREFIX + "setup";
            public static final String WAITING_FOR_PLAYERS = ADMIN_PREFIX + "waiting_for_players";
            public static final String RULES = ADMIN_PREFIX + "rules";
            public static final String WAITING_TO_BEGIN = ADMIN_PREFIX + "waiting_to_begin";
            public static final String PRE_ROUND = ADMIN_PREFIX + "pre_round";
            public static final String IN_GAME = ADMIN_PREFIX + "in_game";
            public static final String PAUSED = ADMIN_PREFIX + "paused";
            public static final String POST_ROUND = ADMIN_PREFIX + "post_round";
            public static final String POST_GAME = ADMIN_PREFIX + "post_game";
        }

        public static class Exit {
            private static final String PREFIX = Permissions.State.PREFIX + "exit.";
            private static final String ADMIN_PREFIX = Permissions.State.ADMIN_PREFIX + "exit.";

            public static final String SERVER_STARTING = ADMIN_PREFIX + "server_starting";
            public static final String LOADING = ADMIN_PREFIX + "loading";
            public static final String SETUP = ADMIN_PREFIX + "setup";
            public static final String WAITING_FOR_PLAYERS = ADMIN_PREFIX + "waiting_for_players";
            public static final String RULES = ADMIN_PREFIX + "rules";
            public static final String WAITING_TO_BEGIN = ADMIN_PREFIX + "waiting_to_begin";
            public static final String PRE_ROUND = ADMIN_PREFIX + "pre_round";
            public static final String IN_GAME = ADMIN_PREFIX + "in_game";
            public static final String PAUSED = ADMIN_PREFIX + "paused";
            public static final String POST_ROUND = ADMIN_PREFIX + "post_round";
            public static final String POST_GAME = ADMIN_PREFIX + "post_game";
        }
    }
}