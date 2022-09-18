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
        return ItemStackBuilder.fromMaterial(material);
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
}
