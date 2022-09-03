package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockUtils {
    private static final List<Material> WOOLS;

    static {
        ArrayList<Material> wools = new ArrayList<>();
        wools.add(Material.WHITE_WOOL);
        wools.add(Material.BLACK_WOOL);
        wools.add(Material.BLUE_WOOL);
        wools.add(Material.BROWN_WOOL);
        wools.add(Material.CYAN_WOOL);
        wools.add(Material.GRAY_WOOL);
        wools.add(Material.GREEN_WOOL);
        wools.add(Material.LIGHT_BLUE_WOOL);
        wools.add(Material.LIGHT_GRAY_WOOL);
        wools.add(Material.LIME_WOOL);
        wools.add(Material.MAGENTA_WOOL);
        wools.add(Material.ORANGE_WOOL);
        wools.add(Material.PINK_WOOL);
        wools.add(Material.PURPLE_WOOL);
        wools.add(Material.RED_WOOL);
        wools.add(Material.YELLOW_WOOL);
        WOOLS = Collections.unmodifiableList(wools);
    }

    public static List<Material> allWools() {
        return WOOLS;
    }

    public static boolean isWool(Material material) {
        return WOOLS.contains(material);
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
            case BLUE -> Material.BLUE_WOOL;
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
