package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Set;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class BlockUtils {
    private static final Set<Material> WOOLS = Set.of(
        Material.WHITE_WOOL,
        Material.BLACK_WOOL,
        Material.BLUE_WOOL,
        Material.BROWN_WOOL,
        Material.CYAN_WOOL,
        Material.GRAY_WOOL,
        Material.GREEN_WOOL,
        Material.LIGHT_BLUE_WOOL,
        Material.LIGHT_GRAY_WOOL,
        Material.LIME_WOOL,
        Material.MAGENTA_WOOL,
        Material.ORANGE_WOOL,
        Material.PINK_WOOL,
        Material.PURPLE_WOOL,
        Material.RED_WOOL,
        Material.YELLOW_WOOL
    );
    private static final Set<Material> CONCRETES = Set.of(
        Material.WHITE_CONCRETE,
        Material.BLACK_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.LIME_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.PINK_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.RED_CONCRETE,
        Material.YELLOW_CONCRETE
    );
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

    public static Material getWoolColor(NamedTextColor color) {
        if (color.value() == BLACK.value()) {
            return Material.BLACK_WOOL;
        } else if (color.value() == DARK_BLUE.value()) {
            return Material.BLUE_WOOL;
        } else if (color.value() == DARK_GREEN.value()) {
            return Material.GREEN_WOOL;
        } else if (color.value() == DARK_AQUA.value()) {
            return Material.CYAN_WOOL;
        } else if (color.value() == DARK_RED.value()) {
            return Material.RED_WOOL;
        } else if (color.value() == DARK_PURPLE.value()) {
            return Material.PURPLE_WOOL;
        } else if (color.value() == GOLD.value()) {
            return Material.ORANGE_WOOL;
        } else if (color.value() == GRAY.value()) {
            return Material.LIGHT_GRAY_WOOL;
        } else if (color.value() == DARK_GRAY.value()) {
            return Material.GRAY_WOOL;
        } else if (color.value() == BLUE.value()) {
            return Material.BLUE_WOOL;
        } else if (color.value() == GREEN.value()) {
            return Material.LIME_WOOL;
        } else if (color.value() == AQUA.value()) {
            return Material.LIGHT_BLUE_WOOL;
        } else if (color.value() == RED.value()) {
            return Material.PINK_WOOL;
        } else if (color.value() == LIGHT_PURPLE.value()) {
            return Material.MAGENTA_WOOL;
        } else if (color.value() == YELLOW.value()) {
            return Material.YELLOW_WOOL;
        } else if (color.value() == WHITE.value()) {
            return Material.WHITE_WOOL;
        }
        return null;
    }

    public static Set<Material> allConcretes() {
        return CONCRETES;
    }

    public static boolean isConcrete(Material material) {
        return CONCRETES.contains(material);
    }

    public static Material getConcreteColor(NamedTextColor color) {
        if (color.value() == BLACK.value()) {
            return Material.BLACK_CONCRETE;
        } else if (color.value() == DARK_BLUE.value()) {
            return Material.BLUE_CONCRETE;
        } else if (color.value() == DARK_GREEN.value()) {
            return Material.GREEN_CONCRETE;
        } else if (color.value() == DARK_AQUA.value()) {
            return Material.CYAN_CONCRETE;
        } else if (color.value() == DARK_RED.value()) {
            return Material.RED_CONCRETE;
        } else if (color.value() == DARK_PURPLE.value()) {
            return Material.PURPLE_CONCRETE;
        } else if (color.value() == GOLD.value()) {
            return Material.ORANGE_CONCRETE;
        } else if (color.value() == GRAY.value()) {
            return Material.LIGHT_GRAY_CONCRETE;
        } else if (color.value() == DARK_GRAY.value()) {
            return Material.GRAY_CONCRETE;
        } else if (color.value() == BLUE.value()) {
            return Material.BLUE_CONCRETE;
        } else if (color.value() == GREEN.value()) {
            return Material.LIME_CONCRETE;
        } else if (color.value() == AQUA.value()) {
            return Material.LIGHT_BLUE_CONCRETE;
        } else if (color.value() == RED.value()) {
            return Material.PINK_CONCRETE;
        } else if (color.value() == LIGHT_PURPLE.value()) {
            return Material.MAGENTA_CONCRETE;
        } else if (color.value() == YELLOW.value()) {
            return Material.YELLOW_CONCRETE;
        } else if (color.value() == WHITE.value()) {
            return Material.WHITE_CONCRETE;
        }
        return null;
    }

    public static boolean isLog(Material type) {
        return LOGS.contains(type);
    }

    public static boolean isLeaves(Material type) {
        return LEAVES.contains(type);
    }
}
