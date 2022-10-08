package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PaginationIterator {
    private static int nextId = 0;
    private final int id = nextId++;

    private final SlotIterator iterator;
    private final List<InventoryItemSupplier> items;
    private final int itemsPerPage;

    PaginationIterator(SlotIterator iterator, List<InventoryItemSupplier> items, int itemsPerPage) {
        this.iterator = iterator;
        this.items = items;
        this.itemsPerPage = itemsPerPage;
    }

    public void apply(int page) {
        SlotIterator iter = this.iterator.clone();

        int index = page * this.itemsPerPage;
        int i = 0;

        while (iter.hasNext() && i < this.itemsPerPage) {
            if (index >= items.size()) {
                iter.set(ISB.stack(Material.AIR));
            } else {
                iter.set(this.items.get(index).get());
            }

            iter.next();
            ++index;
            ++i;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationIterator that = (PaginationIterator) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
