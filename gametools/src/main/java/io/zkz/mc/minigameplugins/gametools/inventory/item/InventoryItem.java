package io.zkz.mc.minigameplugins.gametools.inventory.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryItem {
    public abstract ItemStack getItemStack();

    public abstract void handleClick(InventoryClickEvent event);

    @Override
    public abstract InventoryItem clone();
}
