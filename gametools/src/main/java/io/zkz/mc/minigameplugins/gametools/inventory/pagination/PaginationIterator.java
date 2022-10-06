package io.zkz.mc.minigameplugins.gametools.inventory.pagination;

import io.zkz.mc.minigameplugins.gametools.inventory.SlotIterator;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;

public class PaginationIterator {
    private final SlotIterator iterator;
    private final InventoryItem[] items;
    private final int itemsPerPage;

    PaginationIterator(SlotIterator iterator, InventoryItem[] items, int itemsPerPage) {
        this.iterator = iterator;
        this.items = items;
        this.itemsPerPage = itemsPerPage;
    }

    public void apply(int page) {
        SlotIterator iter = this.iterator.clone();

        int index = page * this.itemsPerPage;
        int i = 0;

        while (iter.hasNext() && i < this.itemsPerPage) {
            if (index > items.length) {
                iter.set(ISB.stack(Material.AIR));
            } else {
                iter.set(this.items[index]);
            }

            iter.next();
            ++index;
            ++i;
        }
    }
}
