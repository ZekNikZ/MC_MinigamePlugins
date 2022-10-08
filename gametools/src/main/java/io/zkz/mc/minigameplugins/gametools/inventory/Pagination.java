package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class Pagination {
    private int page = 0;
    private int numPages;
    private final Set<PaginationIterator> paginationIterators = new HashSet<>();

    private final @Nullable BiConsumer<Integer, Integer> onPageChange;

    Pagination(@Nullable BiConsumer<Integer, Integer> onPageChange, int numPages) {
        this.onPageChange = onPageChange;
        this.numPages = numPages;
    }

    public void init() {
        if (this.onPageChange != null) {
            this.onPageChange.accept(0, 0);
        }
    }

    public int page() {
        return this.page;
    }

    public void page(int page) {
        if (page < 0 || page >= this.numPages) {
            return;
        }

        int oldPage = this.page;
        this.page = page;

        this.paginationIterators.forEach(iter -> iter.apply(this.page));
        if (this.onPageChange != null) {
            this.onPageChange.accept(oldPage, page);
        }
    }

    public int numPages() {
        return this.numPages;
    }

    public void numPages(int numPages) {
        this.numPages = numPages;

        if (this.page >= this.numPages) {
            this.page = this.numPages - 1;
        }
        this.page(this.page);
    }

    public PaginationIterator createIterator(SlotIterator iterator, List<InventoryItemSupplier> items, int itemsPerPage) {
        var iter = new PaginationIterator(iterator, items, itemsPerPage);
        this.paginationIterators.add(iter);
        iter.apply(this.page);
        return iter;
    }

    public PaginationIterator createIterator(SlotIterator iterator, int itemsPerPage, List<? extends InventoryItem> items) {
        return this.createIterator(iterator, items.stream().map(item -> (InventoryItemSupplier) () -> item).toList(), itemsPerPage);
    }

    public void removeIterator(PaginationIterator iterator) {
        this.paginationIterators.remove(iterator);
    }

    public void prev() {
        this.page(this.page() - 1);
    }

    public void next() {
        this.page(this.page() + 1);
    }
}
