package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static io.zkz.mc.minigameplugins.gametools.util.ChatType.Constants.*;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.*;

public enum ChatType {
    NORMAL(
        "<message>",
        POINT_PREFIX + "<message>"
    ),
    ALERT(
        INFO_PREFIX + "<aqua><b><message>",
        POINT_PREFIX + INFO_PREFIX + "<aqua><b><message>"
    ),
    WARNING(
        INFO_PREFIX + "<dark_red><b><message>",
        POINT_PREFIX + INFO_PREFIX + "<dark_red><b><message>"
    ),
    PASSIVE_INFO(
        INFO_PREFIX + "<gray><message>",
        POINT_PREFIX + "<gray><message>"
    ),
    ACTIVE_INFO(
        INFO_PREFIX + "<message>",
        POINT_PREFIX + "<message>"
    ),
    SUCCESS(
        INFO_PREFIX + "<green><b><message>",
        POINT_PREFIX + "<green><b><message>"
    ),
    ELIMINATION(
        "<gray>[<red>\u2620<gray>] <message>",
        POINT_PREFIX + "<gray>[<red>\u2620<gray>]<reset> <message>"
    ),
    TEAM_ELIMINATION(
        "[<red>\u2620\u2620\u2620<reset>] <message>",
        POINT_PREFIX + "[<red>\u2620\u2620\u2620<reset>] <message>"
    ),
    GAME_INFO(
        GAME_PREFIX + "<message>",
        POINT_PREFIX + GAME_PREFIX + "<message>"
    ),
    GAME_SUCCESS(
        GAME_PREFIX + "<green><message>",
        POINT_PREFIX + GAME_PREFIX + "<green><message>"
    ),
    COMMAND_SUCCESS(
        "<light_gray><message>",
        null
    ),
    COMMAND_ERROR(
        "<alert_warning>Command error: <message>",
        null,
        "<alert_warning>Command error: <message>\n<dark_gray><cause>"
    );

    public static class Constants {
        public static final String INFO_CHAR = "<yellow>\u25B6</yellow>";
        public static final String POINT_CHAR = "<yellow>\u2605</yellow>";
        public static final String INFO_PREFIX = "[" + INFO_CHAR + "] ";
        public static final String POINT_PREFIX = "[+<points>" + POINT_CHAR + "] ";
        public static final String GAME_PREFIX = "[<gold><b><name><reset>] ";
    }

    private final String withoutPointsFormat;
    private final String withPointsFormat;
    private final String withErrorFormat;

    ChatType(@NotNull String withoutPointsFormat, @Nullable String withPointsFormat) {
        this(withoutPointsFormat, withPointsFormat, null);
    }

    ChatType(@NotNull String withoutPointsFormat, @Nullable String withPointsFormat, @Nullable String withErrorFormat) {
        this.withoutPointsFormat = withoutPointsFormat;
        this.withPointsFormat = withPointsFormat;
        this.withErrorFormat = withErrorFormat;
    }

    public Component format(Component message) {
        return this.format(this.withoutPointsFormat, message);
    }

    public Component format(Component message, double points) {
        if (this.withPointsFormat == null) {
            throw new UnsupportedOperationException("Formatting with points is not supported for message type " + this.name());
        }
        return this.format(this.withPointsFormat, message, points);
    }

    public Component format(Component message, Throwable cause) {
        if (this.withErrorFormat == null) {
            throw new UnsupportedOperationException("Formatting with cause is not supported for message type " + this.name());
        }
        return this.format(this.withErrorFormat, message, cause);
    }

    private Component format(String format, Component message) {
        return mmResolve(
            format,
            Placeholder.component("message", message),
            Placeholder.unparsed("name", MinigameConstantsService.getInstance().getMinigameName())
        );
    }

    private Component format(String format, Component message, double points) {
        return mmResolve(
            format,
            Placeholder.component("message", message),
            Placeholder.unparsed("name", MinigameConstantsService.getInstance().getMinigameName()),
            Placeholder.unparsed("points", String.valueOf(points))
        );
    }

    private Component format(String format, Component message, Throwable cause) {
        return mmResolve(
            format,
            Placeholder.component("message", message),
            Placeholder.unparsed("name", MinigameConstantsService.getInstance().getMinigameName()),
            Placeholder.unparsed("cause", cause.getMessage())
        );
    }
}
