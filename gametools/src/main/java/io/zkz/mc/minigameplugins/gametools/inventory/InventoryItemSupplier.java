package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;

import java.util.function.Supplier;

@FunctionalInterface
public interface InventoryItemSupplier extends Supplier<InventoryItem> {
}
