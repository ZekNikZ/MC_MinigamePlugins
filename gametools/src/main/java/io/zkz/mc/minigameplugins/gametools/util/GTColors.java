package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GTColors implements TagResolver {
    private static final Map<String, GTColor> COLORS = new HashMap<>();

    public static final GTColor RED = color("red", 0xFB5455);
    public static final GTColor ORANGE = color("orange", 0xFCA800);
    public static final GTColor YELLOW = color("yellow", 0xFBFB00);
    public static final GTColor GREEN = color("green", 0x00A800);
    public static final GTColor LIME = color("lime", 0x54FB55);
    public static final GTColor BLUE = color("blue", 0x3B68F7);
    public static final GTColor AQUA = color("aqua", 0x42D7FC);
    public static final GTColor CYAN = color("cyan", 0x02A183);
    public static final GTColor MAGENTA = color("magenta", 0xFB54FB);
    public static final GTColor PURPLE = color("purple", 0x8632FC);
    public static final GTColor WHITE = color("white", 0xFFFFFF);
    public static final GTColor LIGHT_GRAY = color("light_gray", 0xA5ADAD);
    public static final GTColor DARK_GRAY = color("dark_gray", 0x545454);
    public static final GTColor BLACK = color("black", 0x2B2B2B);
    public static final GTColor ALERT_INFO = color("alert_info", 0x0AFFFF);
    public static final GTColor ALERT_SUCCESS = color("alert_success", 0x17FF32);
    public static final GTColor ALERT_ACCENT = color("alert_accent", 0xFFBB00);
    public static final GTColor ALERT_WARNING = color("alert_warning", 0xFC1B0F);

    private static GTColor color(String name, int rgb) {
        COLORS.put(name, new GTColor(rgb));
        return COLORS.get(name);
    }

    protected static final GTColors INSTANCE = new GTColors();

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        if (COLORS.containsKey(name)) {
            return Tag.styling(COLORS.get(name).textColor());
        }

        return StandardTags.color().resolve(name, arguments, ctx);
    }

    @Override
    public boolean has(@NotNull String name) {
        return COLORS.containsKey(name) || StandardTags.color().has(name);
    }
}
