package io.zkz.mc.minigameplugins.bingo.card;

import org.bukkit.Material;

public enum ItemGroup {
    STONE(Material.STONE, 1),
    COLORS(Material.CYAN_DYE, 2),
    STRING(Material.STRING, 1),
    RAIL(Material.RAIL, 1),
    IRON_LOW(Material.IRON_NUGGET, 2),
    IRON_MEDIUM(Material.IRON_INGOT, 2),
    IRON_HIGH(Material.IRON_BLOCK, 1),
    CRAFTABLE_EASY(Material.CRAFTING_TABLE, 2),
    CRAFTABLE_MEDIUM(Material.CARTOGRAPHY_TABLE, 2),
    CRAFTABLE_HARD(Material.SMITHING_TABLE, 1),
    EXPLORATION(Material.CLAY_BALL, 2),
    MINECART(Material.MINECART, 1),
    FOOD(Material.APPLE, 2),
    GOLD(Material.GOLD_INGOT, 1),
    DIAMOND(Material.DIAMOND, 1),
    WOOD(Material.OAK_LOG, 1),
    MINING(Material.TORCH, 2)
    ;

    private final Material material;
    private final int defaultCount;

    ItemGroup(Material material, int defaultCount) {
        this.material = material;
        this.defaultCount = defaultCount;
    }

    public Material getMaterial() {
        return material;
    }

    public int getDefaultCount() {
        return defaultCount;
    }
}