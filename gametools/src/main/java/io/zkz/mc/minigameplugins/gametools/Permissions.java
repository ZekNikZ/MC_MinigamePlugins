package io.zkz.mc.minigameplugins.gametools;

import org.bukkit.command.CommandExecutor;

public abstract class Permissions implements CommandExecutor {
    private static final String PREFIX = "gametools.";

    public static class Ready {
        private static final String PREFIX = Permissions.PREFIX + "ready.";

        public static final String READY_UP = Permissions.PREFIX + "ready";
        public static final String UNDO_READY_UP = PREFIX + "undoreadyup";
        public static final String STATUS = PREFIX + "status";
    }

    public static class Vanish {
        private static final String PREFIX = Permissions.PREFIX + "vanish.";

        public static final String COMMAND = PREFIX + "command";
        public static final String SELF = PREFIX + "self";
        public static final String OTHERS = PREFIX + "others";
    }

    public static class Teams {
        private static final String PREFIX = Permissions.PREFIX + "teams.";

        public static final String CREATE = PREFIX + "create";
        public static final String JOIN = PREFIX + "join";
        public static final String LEAVE = PREFIX + "leave";
        public static final String RELOAD = PREFIX + "reload";
    }
}
