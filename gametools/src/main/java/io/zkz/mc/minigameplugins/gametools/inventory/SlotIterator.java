package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;

public class SlotIterator {
    public enum Type {
        HORIZONTAL(
            iter -> new SlotPos(iter.row(), iter.col() + (iter.reversed() ? -1 : 1))
        ),
        HORIZONTAL_WRAP(
            iter -> {
                int newCol = iter.col() + (iter.reversed() ? -1 : 1);
                if (newCol == -1) {
                    return new SlotPos(iter.row() - 1, iter.contents.cols() - 1);
                } else if (newCol == iter.contents.cols()) {
                    return new SlotPos(iter.row() + 1, 0);
                }
                return new SlotPos(iter.row(), newCol);
            }
        ),
        VERTICAL(
            iter -> new SlotPos(iter.row() + (iter.reversed() ? -1 : 1), iter.col())
        ),
        VERTICAL_WRAP(
            iter -> {
                int newRow = iter.row() + (iter.reversed() ? -1 : 1);
                if (newRow == -1) {
                    return new SlotPos(iter.contents.rows() - 1, iter.col() - 1);
                } else if (newRow == iter.contents.rows()) {
                    return new SlotPos(0, iter.col() + 1);
                }
                return new SlotPos(iter.row(), newRow);
            }
        );

        private final Function<SlotIterator, SlotPos> next;

        Type(Function<SlotIterator, SlotPos> advance) {
            this.next = advance;
        }

        public SlotPos next(SlotIterator iterator) {
            return this.next.apply(iterator);
        }
    }

    private final Type type;
    private final UIContents contents;
    private final boolean reversed;
    private final Set<Integer> blacklistedRows = new HashSet<>();
    private final Set<Integer> blacklistedCols = new HashSet<>();
    private final Set<SlotPos> blacklistedPositions = new HashSet<>();
    private int row, col;
    private SlotPos nextPos;

    protected SlotIterator(Type type, UIContents contents, boolean reversed, int row, int col) {
        this.type = type;
        this.contents = contents;
        this.reversed = reversed;
        this.row = row;
        this.col = col;
        this.computeNext();
    }

    public boolean hasNext() {
        return this.isValid(this.nextPos);
    }

    public void next() {
        this.row = this.nextPos.row();
        this.col = this.nextPos.col();
        this.computeNext();
    }

    public Optional<InventoryItem> get() {
        return this.contents.get(this.row, this.col);
    }

    public void set(InventoryItem item) {
        this.contents.set(this.row, this.col, item);
    }

    public void set(ItemStack stack) {
        this.set(ClickableItem.of(stack));
    }

    public boolean reversed() {
        return this.reversed;
    }

    public SlotPos pos() {
        return new SlotPos(this.row, this.col);
    }

    public void pos(SlotPos pos) {
        this.row = pos.row();
        this.col = pos.col();
        this.computeNext();
    }

    public int row() {
        return this.row;
    }

    public void row(int row) {
        this.row = row;
        this.computeNext();
    }

    public int col() {
        return this.col;
    }

    public void col(int col) {
        this.col = col;
        this.computeNext();
    }

    public void blacklist(int row, int col) {
        this.blacklist(new SlotPos(row, col));
    }

    public void blacklist(SlotPos pos) {
        this.blacklistedPositions.add(pos);
    }

    public final void blacklistRow(int... rows) {
        for (int row : rows) {
            this.blacklistedRows.add(row);
        }
    }

    public final void blacklistCol(int... cols) {
        for (int col : cols) {
            this.blacklistedCols.add(col);
        }
    }

    public Type type() {
        return this.type;
    }

    private boolean isValid(SlotPos pos) {
        return pos.row() >= 0 && pos.row() < this.contents.rows() && pos.col() >= 0 && pos.col() < this.contents.cols();
    }

    private void computeNext() {
        SlotPos nextPos;
        do {
            nextPos = this.type.next(this);
            if (!this.isValid(nextPos)) {
                break;
            }
        }
        while (this.isBlacklisted(nextPos));

        this.nextPos = nextPos;
    }

    private boolean isBlacklisted(SlotPos pos) {
        return this.blacklistedPositions.contains(pos)
            || this.blacklistedRows.contains(pos.row())
            || this.blacklistedCols.contains(pos.col());
    }

    @Override
    public SlotIterator clone() {
        SlotIterator iter = new SlotIterator(
            this.type,
            this.contents,
            this.reversed,
            this.row,
            this.col
        );
        iter.blacklistedPositions.addAll(this.blacklistedPositions);
        iter.blacklistedRows.addAll(this.blacklistedRows);
        iter.blacklistedCols.addAll(this.blacklistedCols);
        return iter;
    }
}
