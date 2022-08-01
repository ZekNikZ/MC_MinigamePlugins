package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;

import static io.zkz.mc.minigameplugins.gametools.util.Constants.*;
import static org.bukkit.ChatColor.*;

class Constants {
    private static final String INFO_CHAR = YELLOW + "⯈" + RESET;
    private static final String POINT_CHAR = YELLOW + "★" + RESET;

    public static final String INFO_PREFIX = "[" + INFO_CHAR + "] ";
    public static final String POINT_PREFIX = "[+%points%" + POINT_CHAR + "] ";
    public static final String GAME_PREFIX = "[" + GOLD + BOLD + "%name%" + RESET + "] ";
}

public enum ChatType {
    ALERT(
        INFO_PREFIX + AQUA + BOLD + "%message%",
        POINT_PREFIX + INFO_PREFIX + AQUA + BOLD + "%message%"
    ),
    WARNING(
        INFO_PREFIX + DARK_RED + BOLD + "%message%",
        POINT_PREFIX + INFO_PREFIX + DARK_RED + BOLD + "%message%"
    ),
    PASSIVE_INFO(
        INFO_PREFIX + GRAY + "%message%",
        POINT_PREFIX + GRAY + "%message%"
    ),
    ACTIVE_INFO(
        INFO_PREFIX + "%message%",
        POINT_PREFIX + "%message%"
    ),
    SUCCESS(
        INFO_PREFIX + GREEN + BOLD + "%message%",
        POINT_PREFIX + GREEN + BOLD + "%message%"
    ),
    ELIMINATION(
        "[" + RED + "\uD83D\uDC80" + RESET + "] " + GRAY + "%message%",
        POINT_PREFIX + "[" + RED + "\uD83D\uDC80" + RESET + "] " + "%message%"
    ),
    TEAM_ELIMINATION(
        "[" + RED + "\uD83D\uDC80\uD83D\uDC80\uD83D\uDC80" + RESET + "] " + "%message%",
        POINT_PREFIX + "[" + RED + "\uD83D\uDC80\uD83D\uDC80\uD83D\uDC80" + RESET + "] " + "%message%"
    ),
    GAME_INFO(
        GAME_PREFIX + "%message%",
        POINT_PREFIX + GAME_PREFIX + "%message%"
    ),
    GAME_SUCCESS(
        GAME_PREFIX + GREEN  + "%message%",
        POINT_PREFIX + GAME_PREFIX + GREEN  + "%message%"
    );

    private final String withoutPointsFormat;
    private final String withPointsFormat;

    ChatType(String withoutPointsFormat, String withPointsFormat) {
        this.withoutPointsFormat = withoutPointsFormat;
        this.withPointsFormat = withPointsFormat;
    }

    public String format(String message) {
        return this.format(this.withoutPointsFormat, message);
    }

    public String format(String message, float points) {
        return this.format(this.withPointsFormat, message, points);
    }

    private String format(String format, String message) {
        return format
            .replace("%message%", message)
            .replace("%name%", ChatConstantsService.getInstance().getMinigameName());
    }

    private String format(String format, String message, float points) {
        return this.format(format, message)
            .replace("%points%", String.valueOf(points));
    }
}
