package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class BlockUtils {
    public static final boolean isWool(Material material) {
        return switch (material) {
            case WHITE_WOOL, BLACK_WOOL, BLUE_WOOL, BROWN_WOOL, CYAN_WOOL, GRAY_WOOL, GREEN_WOOL, LIGHT_BLUE_WOOL, LIGHT_GRAY_WOOL, LIME_WOOL, MAGENTA_WOOL, ORANGE_WOOL, PINK_WOOL, PURPLE_WOOL, RED_WOOL, YELLOW_WOOL ->
                true;
            default -> false;
        };
    }

    public static Material getWoolColor(ChatColor color) {
        return switch (color) {
            case BLACK -> Material.BLACK_WOOL;
            case DARK_BLUE -> Material.BLUE_WOOL;
            case DARK_GREEN -> Material.GREEN_WOOL;
            case DARK_AQUA -> Material.CYAN_WOOL;
            case DARK_RED -> Material.RED_WOOL;
            case DARK_PURPLE -> Material.PURPLE_WOOL;
            case GOLD -> Material.ORANGE_WOOL;
            case GRAY -> Material.LIGHT_GRAY_WOOL;
            case DARK_GRAY -> Material.GRAY_WOOL;
            case BLUE -> Material.BROWN_WOOL;
            case GREEN -> Material.LIME_WOOL;
            case AQUA -> Material.LIGHT_BLUE_WOOL;
            case RED -> Material.PINK_WOOL;
            case LIGHT_PURPLE -> Material.MAGENTA_WOOL;
            case YELLOW -> Material.YELLOW_WOOL;
            case WHITE -> Material.WHITE_WOOL;
            default -> null;
        };
    }
}
