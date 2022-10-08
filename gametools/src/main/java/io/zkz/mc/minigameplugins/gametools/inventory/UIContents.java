package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A separate instance of this is created each time this inventory is opened.
 */
public abstract class UIContents {
    protected final CustomUI inv;
    protected final Player player;
    private final List<InventoryItem> items;
    private final List<Pagination> paginations = new ArrayList<>();

    public UIContents(CustomUI inv, Player player) {
        this.inv = inv;
        this.player = player;
        this.items = new ArrayList<>(Collections.nCopies(this.slots(), null));
    }

    protected abstract void init();

    protected final void initialize() {
        this.init();
        this.paginations.forEach(Pagination::init);
    }

    protected void update() {
    }

    protected final int rows() {
        return this.inv.rows();
    }

    protected final int cols() {
        return this.inv.cols();
    }

    protected final int slots() {
        return this.rows() * this.cols();
    }

    protected final Optional<InventoryItem> get(int row, int col) {
        return Optional.ofNullable(this.items.get(row * this.cols() + col));
    }

    protected final Optional<InventoryItem> get(SlotPos pos) {
        return this.get(pos.row(), pos.col());
    }

    protected final UIContents set(int row, int col, InventoryItem item) {
        if (row < 0 || row >= this.rows()) {
            return this;
        }

        if (col < 0 || col >= this.cols()) {
            return this;
        }

        this.items.set(row * this.cols() + col, item);

        if (!InventoryService.getInstance().getOpenedPlayers(inv).contains(player)) {
            return this;
        }

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        topInventory.setItem(row * this.cols() + col, item.getItemStack());

        return this;
    }

    protected final UIContents set(int row, int col, ItemStack stack) {
        return this.set(row, col, ClickableItem.of(stack));
    }

    protected final UIContents set(SlotPos pos, InventoryItem item) {
        return this.set(pos.row(), pos.col(), item);
    }

    protected final UIContents set(SlotPos pos, ItemStack stack) {
        return this.set(pos.row(), pos.col(), stack);
    }

    protected final UIContents fill(InventoryItem item) {
        for (int row = 0; row < this.rows(); row++) {
            for (int col = 0; col < this.cols(); col++) {
                this.set(row, col, item.clone());
            }
        }

        return this;
    }

    protected final UIContents fillRow(int row, InventoryItem item) {
        for (int col = 0; col < this.cols(); col++) {
            this.set(row, col, item.clone());
        }

        return this;
    }

    protected final UIContents fillCol(int col, InventoryItem item) {
        for (int row = 0; row < this.rows(); row++) {
            this.set(row, col, item.clone());
        }

        return this;
    }

    protected final UIContents fillBorders(InventoryItem item) {
        this.fillRow(0, item);
        this.fillRow(this.rows() - 1, item);
        this.fillCol(0, item);
        this.fillCol(this.cols() - 1, item);

        return this;
    }

    protected final UIContents fillRect(int fromRow, int fromCol, int toRow, int toCol, InventoryItem item) {
        for (int row = fromRow; row <= toRow; row++) {
            for (int col = fromCol; col <= toCol; col++) {
                this.set(row, col, item.clone());
            }
        }

        return this;
    }

    protected final UIContents fillRect(SlotPos fromPos, SlotPos toPos, InventoryItem item) {
        return this.fillRect(fromPos.row(), fromPos.col(), toPos.row(), toPos.col(), item);
    }

    public List<InventoryItem> items() {
        return this.items;
    }

    public SlotIterator createIterator(SlotIterator.Type type, int startingRow, int startingCol) {
        return this.createIterator(type, startingRow, startingCol, false);
    }

    public SlotIterator createIterator(SlotIterator.Type type, int startingRow, int startingCol, boolean reversed) {
        return new SlotIterator(type, this, reversed, startingRow, startingCol);
    }

    public SlotIterator createIterator(SlotIterator.Type type, SlotPos startingPos) {
        return this.createIterator(type, startingPos, false);
    }

    public SlotIterator createIterator(SlotIterator.Type type, SlotPos startingPos, boolean reversed) {
        return this.createIterator(type, startingPos.row(), startingPos.col(), reversed);
    }

    public List<Pagination> paginations() {
        return this.paginations;
    }

    protected Pagination createPagination(int numPages) {
        return this.createPagination(null, numPages);
    }

    protected Pagination createPagination(@Nullable BiConsumer<Integer, Integer> onPageChange, int numPages) {
        Pagination pagination = new Pagination(onPageChange, numPages);
        this.paginations.add(pagination);
        return pagination;
    }
}
