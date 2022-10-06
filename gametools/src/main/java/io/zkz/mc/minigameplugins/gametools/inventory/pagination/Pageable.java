package io.zkz.mc.minigameplugins.gametools.inventory.pagination;

import io.zkz.mc.minigameplugins.gametools.inventory.SlotIterator;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;

import java.util.List;

public interface Pageable {
    int getPage();

    void setPage(int page);

    int numPages();

    void setNumPages(int numPages);

    void createPaginationIterator(SlotIterator iterator, InventoryItem[] items, int itemsPerPage);

    default void createPaginationIterator(SlotIterator iterator, List<InventoryItem> items, int itemsPerPage) {
        this.createPaginationIterator(iterator, items.toArray(InventoryItem[]::new), itemsPerPage);
    }

    default void prev() {
        this.setPage(this.getPage() - 1);
    }

    default void next() {
        this.setPage(this.getPage() - 1);
    }

    default void onPageChange(int oldPage) {
    }
}
