package io.zkz.mc.minigameplugins.bingo.card;

import org.bukkit.Material;

public enum ItemGroup {
    AMETHYST(Material.AMETHYST_CLUSTER, 1),
    BUCKET(Material.WATER_BUCKET, 1),
    COLORS(Material.CYAN_DYE, 2),
    COPPER(Material.COPPER_INGOT, 1),
    CRAFTABLE_EASY(Material.CRAFTING_TABLE, 2),
    CRAFTABLE_FOOD(Material.COOKED_BEEF, 1),
    CRAFTABLE_HARD(Material.SMITHING_TABLE, 1),
    CRAFTABLE_MEDIUM(Material.CARTOGRAPHY_TABLE, 2),
    CRAFTABLE_STONE(Material.DEEPSLATE_TILES, 1),
    DIAMOND(Material.DIAMOND, 1),
    EXPLORATION(Material.CLAY_BALL, 2),
    FINDABLE_STONE(Material.GRANITE, 1),
    GOLD(Material.GOLD_INGOT, 1),
    IRON_HIGH(Material.IRON_BLOCK, 1),
    IRON_LOW(Material.IRON_NUGGET, 1),
    LUSH_CAVE(Material.FLOWERING_AZALEA, 1),
    MINECART(Material.MINECART, 1),
    MINING(Material.TORCH, 2),
    RAIL(Material.RAIL, 1),
    STRING(Material.STRING, 1),
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