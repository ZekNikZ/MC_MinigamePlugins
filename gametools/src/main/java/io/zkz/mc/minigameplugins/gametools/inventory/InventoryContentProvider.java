package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A separate instance of this is created each time this inventory is opened.
 */
public abstract class InventoryContentProvider {
    protected final CustomInventory inv;
    protected final Player player;
    private final List<InventoryItem> items;

    public InventoryContentProvider(CustomInventory inv, Player player) {
        this.inv = inv;
        this.player = player;
        this.items = new ArrayList<>(Collections.nCopies(this.getSlotCount(), null));
    }

    protected abstract void init();

    protected abstract void update();

    protected final int getRowCount() {
        return this.inv.rows();
    }

    protected final int getColCount() {
        return this.inv.cols();
    }

    protected final int getSlotCount() {
        return this.getRowCount() * this.getColCount();
    }

    protected final Optional<InventoryItem> get(int row, int col) {
        return Optional.ofNullable(this.items.get(row * this.getColCount() + col));
    }

    protected final InventoryContentProvider set(int row, int col, InventoryItem item) {
        if (row < 0 || row >= this.getRowCount()) {
            return this;
        }

        if (col < 0 || col >= this.getColCount()) {
            return this;
        }

        this.items.set(row * this.getColCount() + col, item);

        if (!InventoryService.getInstance().getOpenedPlayers(inv).contains(player)) {
            return this;
        }

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        topInventory.setItem(row * this.getColCount() + col, item.getItemStack());

        return this;
    }

    protected final InventoryContentProvider set(int row, int col, ItemStack stack) {
        return this.set(row, col, ClickableItem.of(stack));
    }

    protected final InventoryContentProvider fill(InventoryItem item) {
        for (int row = 0; row < this.getRowCount(); row++) {
            for (int col = 0; col < this.getColCount(); col++) {
                this.set(row, col, item.clone());
            }
        }

        return this;
    }

    protected final InventoryContentProvider fillRow(int row, InventoryItem item) {
        for (int col = 0; col < this.getColCount(); col++) {
            this.set(row, col, item.clone());
        }

        return this;
    }

    protected final InventoryContentProvider fillCol(int col, InventoryItem item) {
        for (int row = 0; row < this.getRowCount(); row++) {
            this.set(row, col, item.clone());
        }

        return this;
    }

    protected final InventoryContentProvider fillBorders(InventoryItem item) {
        this.fillRow(0, item);
        this.fillRow(this.getRowCount() - 1, item);
        this.fillCol(0, item);
        this.fillCol(this.getColCount() - 1, item);

        return this;
    }

    protected final InventoryContentProvider fillRect(int fromRow, int fromCol, int toRow, int toCol, InventoryItem item) {
        for (int row = fromRow; row <= toRow; row++) {
            for (int col = fromCol; col <= toCol; col++) {
                this.set(row, col, item.clone());
            }
        }

        return this;
    }

    public List<InventoryItem> items() {
        return this.items;
    }
}
