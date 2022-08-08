package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.Color;

public class ColorUtils {
    public static Color toBukkitColor(java.awt.Color color) {
        return Color.fromRGB(color.getRGB() & 0x00FFFFFF);
    }
}
