package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.*;

public class BlockUtils {
    private static final Set<Material> WOOLS = Set.of(Material.WHITE_WOOL, Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIGHT_GRAY_WOOL, Material.LIME_WOOL, Material.MAGENTA_WOOL, Material.ORANGE_WOOL, Material.PINK_WOOL, Material.PURPLE_WOOL, Material.RED_WOOL, Material.YELLOW_WOOL);
    private static final Set<Material> LOGS = Set.of(
        Material.OAK_LOG,
        Material.DARK_OAK_LOG,
        Material.BIRCH_LOG,
        Material.ACACIA_LOG,
        Material.SPRUCE_LOG,
        Material.JUNGLE_LOG,
        Material.MANGROVE_LOG
    );
    private static final Set<Material> LEAVES = Set.of(
        Material.OAK_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.BIRCH_LEAVES,
        Material.ACACIA_LEAVES,
        Material.SPRUCE_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.MANGROVE_LEAVES
    );

    public static Set<Material> allWools() {
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

    public static boolean isLog(Material type) {
        return LOGS.contains(type);
    }

    public static boolean isLeaves(Material type) {
        return LEAVES.contains(type);
    }
}
