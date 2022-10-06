package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.inventory.ItemStack;

public interface SlotIterator {
    boolean hasNext();

    void next();

    void set(InventoryItem item);

    default void set(ItemStack stack) {
        this.set(ClickableItem.of(stack));
    }

    int row();

    int setRow(int row);

    int col();

    int setCol(int col);

    int blacklist(int row, int col);

    int blacklistRow(int row);

    int blacklistCol(int col);

    Type getType();

    enum Type {
        HORIZONTAL,
        HORIZONTAL_WRAP,
        VERTICAL,
        VERTICAL_WRAP
    }
}
