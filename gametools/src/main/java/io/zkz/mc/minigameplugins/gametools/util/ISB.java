package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Alias for {@link ItemStackBuilder}.
 */
public class ISB {
    public static ItemStackBuilder builder() {
        return ItemStackBuilder.builder();
    }

    public static ItemStackBuilder material(Material material) {
        return ItemStackBuilder.fromMaterial(material, (short) 0);
    }

    public static ItemStackBuilder material(Material material, short damage) {
        return ItemStackBuilder.fromMaterial(material, damage);
    }

    public static ItemStackBuilder fromItemStack(ItemStack stack) {
        return ItemStackBuilder.fromStack(stack);
    }

    public static ItemStack stack(Material material) {
        return new ItemStack(material);
    }

    public static ItemStack stack(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    public static ItemStack stack(Material material, short damage) {
        return new ItemStack(material, 1, damage);
    }

    public static ItemStack stack(Material material, int amount, short damage) {
        return new ItemStack(material, amount, damage);
    }
}
