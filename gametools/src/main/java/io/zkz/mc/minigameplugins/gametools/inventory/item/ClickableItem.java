package io.zkz.mc.minigameplugins.gametools.inventory.item;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Represents an item which can be clicked on.
 */
public class ClickableItem extends InventoryItem {
    private final ItemStack stack;
    private final Consumer<InventoryClickEvent> clickHandler;

    private ClickableItem(ItemStack stack, Consumer<InventoryClickEvent> clickHandler) {
        this.stack = stack;
        this.clickHandler = clickHandler;
    }

    public static ClickableItem of(ItemStack stack) {
        return new ClickableItem(stack.clone(), e -> {});
    }

    public static ClickableItem ofEvent(ItemStack stack, Consumer<InventoryClickEvent> clickHandler) {
        return new ClickableItem(stack, clickHandler);
    }

    public static ClickableItem of(ItemStack stack, Consumer<ClickType> clickHandler) {
        return new ClickableItem(stack, e -> clickHandler.accept(e.getClick()));
    }

    @Override
    public ItemStack getItemStack() {
        return this.stack;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        this.clickHandler.accept(event);
    }

    @Override
    public InventoryItem clone() {
        return new ClickableItem(this.stack.clone(), this.clickHandler);
    }
}
