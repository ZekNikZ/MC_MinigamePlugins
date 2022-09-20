package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;

public record GTColor(int rgb) {
    public static GTColor of(int rgb) {
        return new GTColor(rgb);
    }

    public TextColor textColor() {
        return TextColor.color(this.rgb);
    }

    public Color bukkitColor() {
        return Color.fromRGB(this.rgb);
    }

    public java.awt.Color awtColor() {
        return new java.awt.Color(this.rgb);
    }
}
