package io.zkz.mc.minigameplugins.gametools.inventory.pagination;

import io.zkz.mc.minigameplugins.gametools.inventory.CustomUI;
import io.zkz.mc.minigameplugins.gametools.inventory.SlotIterator;
import io.zkz.mc.minigameplugins.gametools.inventory.UIContents;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class PageableUIContents extends UIContents implements Pageable {
    private int numPages;
    private int page = 0;
    private final List<PaginationIterator> paginationIterators = new ArrayList<>();

    public PageableUIContents(CustomUI inv, Player player, int initialPageCount) {
        super(inv, player);
        this.numPages = initialPageCount;
    }

    private void handlePageChange(int oldPage) {
        this.paginationIterators.forEach(iter -> iter.apply(this.getPage()));
        this.onPageChange(oldPage);
    }

    @Override
    public void createPaginationIterator(SlotIterator iterator, InventoryItem[] items, int itemsPerPage) {
        this.paginationIterators.add(new PaginationIterator(iterator, items, itemsPerPage));
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public int numPages() {
        return this.numPages;
    }

    @Override
    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    @Override
    public void setPage(int page) {
        if (page < 0 || page >= this.numPages) {
            return;
        }

        int oldPage = this.page;
        this.page = page;
        this.handlePageChange(oldPage);
    }
}
