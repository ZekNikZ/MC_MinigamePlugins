package io.zkz.mc.minigameplugins.bingo.card;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum BingoItem {
    ACTIVATOR_RAIL(Material.ACTIVATOR_RAIL, ItemGroup.RAIL),
    ANDESITE(Material.ANDESITE, ItemGroup.STONE),
    ANVIL(Material.ANVIL, ItemGroup.IRON_HIGH),
    APPLE(Material.APPLE, ItemGroup.FOOD),
    ARMOR_STAND(Material.ARMOR_STAND, ItemGroup.CRAFTABLE_EASY),
    ARROW(Material.ARROW, ItemGroup.CRAFTABLE_MEDIUM),
    BLAST_FURNACE(Material.BLAST_FURNACE, ItemGroup.CRAFTABLE_HARD),
    BOOKSHELF(Material.BOOKSHELF, ItemGroup.CRAFTABLE_HARD),
    BOOK_AND_QUILL(Material.WRITABLE_BOOK, ItemGroup.CRAFTABLE_HARD),
    BRICKS(Material.BRICKS, ItemGroup.EXPLORATION),
    CAMPFIRE(Material.CAMPFIRE, ItemGroup.WOOD),
    CARTOGRAPHY_TABLE(Material.CARTOGRAPHY_TABLE, ItemGroup.CRAFTABLE_MEDIUM),
    CAULDRON(Material.CAULDRON, ItemGroup.IRON_HIGH),
    CHEST_MINECART(Material.CHEST_MINECART, ItemGroup.MINECART),
    CLOCK(Material.CLOCK, ItemGroup.GOLD),
    COAL_BLOCK(Material.COAL_BLOCK, ItemGroup.MINING, true),
    COBWEB(Material.COBWEB, ItemGroup.EXPLORATION),
    COD_BUCKET(Material.COD_BUCKET, ItemGroup.EXPLORATION),
    COMPASS(Material.COMPASS, ItemGroup.IRON_MEDIUM),
    COOKED_BEEF(Material.COOKED_BEEF, ItemGroup.FOOD),
    COOKED_CHICKEN(Material.COOKED_CHICKEN, ItemGroup.FOOD),
    COOKED_COD(Material.COOKED_COD, ItemGroup.FOOD),
    COOKED_MUTTON(Material.COOKED_MUTTON, ItemGroup.FOOD),
    COOKED_PORKCHOP(Material.COOKED_PORKCHOP, ItemGroup.FOOD),
    COOKED_SALMON(Material.COOKED_SALMON, ItemGroup.FOOD),
    CROSSBOW(Material.CROSSBOW, ItemGroup.STRING),
    CYAN_CONCRETE(Material.CYAN_CONCRETE, ItemGroup.COLORS),
    CYAN_GLAZED_TERRACOTTA(Material.CYAN_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    CYAN_TERRACOTTA(Material.CYAN_TERRACOTTA, ItemGroup.COLORS),
    DETECTOR_RAIL(Material.DETECTOR_RAIL, ItemGroup.RAIL),
    DIAMOND(Material.DIAMOND, ItemGroup.DIAMOND),
    DIAMOND_BLOCK(Material.DIAMOND_BLOCK, ItemGroup.DIAMOND),
    DIAMOND_HOE(Material.DIAMOND_HOE, ItemGroup.DIAMOND),
    DIORITE(Material.DIORITE, ItemGroup.STONE),
    DISPENSER(Material.DISPENSER, ItemGroup.STRING),
    EMERALD(Material.EMERALD, ItemGroup.EXPLORATION),
    ENCHANTING_TABLE(Material.ENCHANTING_TABLE, ItemGroup.DIAMOND),
    FIREWORK(Material.FIREWORK_ROCKET, ItemGroup.CRAFTABLE_HARD),
    FLETCHING_TABLE(Material.FLETCHING_TABLE, ItemGroup.CRAFTABLE_MEDIUM),
    FLINT_AND_STEEL(Material.FLINT_AND_STEEL, ItemGroup.IRON_LOW),
    FLOWER_POT(Material.FLOWER_POT, ItemGroup.EXPLORATION),
    GLASS_BOTTLE(Material.GLASS_BOTTLE, ItemGroup.CRAFTABLE_MEDIUM),
    GLISTERING_MELON_SLICE(Material.GLISTERING_MELON_SLICE, ItemGroup.GOLD),
    GOLDEN_APPLE(Material.GOLDEN_APPLE, ItemGroup.GOLD),
    GOLDEN_CARROT(Material.GOLDEN_CARROT, ItemGroup.GOLD),
    GOLD_BLOCK(Material.GOLD_BLOCK, ItemGroup.GOLD),
    GRANITE(Material.GRANITE, ItemGroup.STONE),
    GRINDSTONE(Material.GRINDSTONE, ItemGroup.CRAFTABLE_MEDIUM),
    HAY_BLOCK(Material.HAY_BLOCK, ItemGroup.FOOD),
    HOPPER(Material.HOPPER, ItemGroup.IRON_MEDIUM),
    HOPPER_MINECART(Material.HOPPER_MINECART, ItemGroup.MINECART),
    IRON_BARS(Material.IRON_BARS, ItemGroup.IRON_HIGH),
    IRON_BLOCK(Material.IRON_BLOCK, ItemGroup.IRON_HIGH),
    ITEM_FRAME(Material.ITEM_FRAME, ItemGroup.CRAFTABLE_EASY),
    JACK_O_LANTERN(Material.JACK_O_LANTERN, ItemGroup.CRAFTABLE_MEDIUM),
    JUKEBOX(Material.JUKEBOX, ItemGroup.DIAMOND),
    KELP(Material.KELP, ItemGroup.EXPLORATION),
    LADDER(Material.LADDER, ItemGroup.WOOD),
    LANTERN(Material.LANTERN, ItemGroup.IRON_LOW),
    LAPIS_BLOCK(Material.LAPIS_BLOCK, ItemGroup.MINING),
    LECTERN(Material.LECTERN, ItemGroup.CRAFTABLE_HARD),
    LIGHT_BLUE_CONCRETE(Material.LIGHT_BLUE_CONCRETE, ItemGroup.COLORS),
    LIGHT_BLUE_GLAZED_TERRACOTTA(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    LIGHT_BLUE_TERRACOTTA(Material.LIGHT_BLUE_TERRACOTTA, ItemGroup.COLORS),
    LIGHT_GRAY_CONCRETE(Material.LIGHT_GRAY_CONCRETE, ItemGroup.COLORS),
    LIGHT_GRAY_GLAZED_TERRACOTTA(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    LIGHT_GRAY_TERRACOTTA(Material.LIGHT_GRAY_TERRACOTTA, ItemGroup.COLORS),
    LIME_CONCRETE(Material.LIME_CONCRETE, ItemGroup.COLORS),
    LIME_GLAZED_TERRACOTTA(Material.LIME_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    LIME_TERRACOTTA(Material.LIME_TERRACOTTA, ItemGroup.COLORS),
    LOOM(Material.LOOM, ItemGroup.STRING),
    MAGENTA_CONCRETE(Material.MAGENTA_CONCRETE, ItemGroup.COLORS),
    MAGENTA_GLAZED_TERRACOTTA(Material.MAGENTA_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    MAGENTA_TERRACOTTA(Material.MAGENTA_TERRACOTTA, ItemGroup.COLORS),
    MAGMA_BLOCK(Material.MAGMA_BLOCK, ItemGroup.EXPLORATION),
    MAP(Material.MAP, ItemGroup.IRON_LOW),
    MILK_BUCKET(Material.MILK_BUCKET, ItemGroup.IRON_LOW),
    MUSHROOM_STEW(Material.MUSHROOM_STEW, ItemGroup.EXPLORATION),
    NOTE_BLOCK(Material.NOTE_BLOCK, ItemGroup.WOOD),
    OBSIDIAN(Material.OBSIDIAN, ItemGroup.DIAMOND, true),
    ORANGE_CONCRETE(Material.ORANGE_CONCRETE, ItemGroup.COLORS),
    ORANGE_GLAZED_TERRACOTTA(Material.ORANGE_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    ORANGE_TERRACOTTA(Material.ORANGE_TERRACOTTA, ItemGroup.COLORS),
    PAINTING(Material.PAINTING, ItemGroup.CRAFTABLE_EASY),
    PAPER(Material.PAPER, ItemGroup.CRAFTABLE_EASY),
    PINK_CONCRETE(Material.PINK_CONCRETE, ItemGroup.COLORS),
    PINK_GLAZED_TERRACOTTA(Material.PINK_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    PINK_TERRACOTTA(Material.PINK_TERRACOTTA, ItemGroup.COLORS),
    PISTON(Material.PISTON, ItemGroup.IRON_LOW),
    POWERED_RAIL(Material.POWERED_RAIL, ItemGroup.RAIL),
    PURPLE_CONCRETE(Material.PURPLE_CONCRETE, ItemGroup.COLORS),
    PURPLE_GLAZED_TERRACOTTA(Material.PURPLE_GLAZED_TERRACOTTA, ItemGroup.COLORS),
    PURPLE_TERRACOTTA(Material.PURPLE_TERRACOTTA, ItemGroup.COLORS),
    REDSTONE_BLOCK(Material.REDSTONE_BLOCK, ItemGroup.MINING),
    REPEATER(Material.REPEATER, ItemGroup.MINING),
    SALMON_BUCKET(Material.SALMON_BUCKET, ItemGroup.EXPLORATION),
    SCAFFOLDING(Material.SCAFFOLDING, ItemGroup.CRAFTABLE_MEDIUM),
    SHEARS(Material.SHEARS, ItemGroup.IRON_LOW),
    SMITHING_TABLE(Material.SMITHING_TABLE, ItemGroup.CRAFTABLE_MEDIUM),
    SPIDER_EYE(Material.SPIDER_EYE, ItemGroup.EXPLORATION),
    STONE(Material.STONE, ItemGroup.STONE),
    STONE_BRICKS(Material.STONE_BRICKS, ItemGroup.STONE),
    TARGET(Material.TARGET, ItemGroup.CRAFTABLE_HARD),
    TNT(Material.TNT, ItemGroup.CRAFTABLE_HARD),
    TNT_MINECART(Material.TNT_MINECART, ItemGroup.MINECART),
    TROPICAL_FISH_BUCKET(Material.TROPICAL_FISH_BUCKET, ItemGroup.EXPLORATION),
    ;

    private final Material material;
    private final ItemGroup mainGroup;
    private final boolean alwaysPresent;

    BingoItem(Material material, ItemGroup mainGroup) {
        this(material, mainGroup, false);
    }

    BingoItem(Material material, ItemGroup mainGroup, boolean alwaysPresent) {
        this.material = material;
        this.mainGroup = mainGroup;
        this.alwaysPresent = alwaysPresent;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemGroup getMainGroup() {
        return mainGroup;
    }

    public boolean isAlwaysPresent() {
        return alwaysPresent;
    }

    public static Map<ItemGroup, List<BingoItem>> itemGroups() {
        return Arrays.stream(values()).collect(Collectors.groupingBy(BingoItem::getMainGroup));
    }
}
