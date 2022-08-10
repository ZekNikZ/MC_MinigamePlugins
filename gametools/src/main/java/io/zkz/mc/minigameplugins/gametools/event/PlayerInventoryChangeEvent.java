package io.zkz.mc.minigameplugins.gametools.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventoryChangeEvent extends AbstractEvent implements Cancellable {
    public enum Reason {
        INVENTORY_INTERACT_GENERAL,
        INVENTORY_INTERACT_CLICK,
        INVENTORY_INTERACT_DRAG,
        PLAYER_ITEM_DROP,
        PLAYER_ITEM_CONSUME,
        PLAYER_ITEM_BREAK,
        PLAYER_ITEM_CRAFT,
        PLAYER_INTERACT_ENTITY,
        PLAYER_ITEM_PICK_UP,
    }

    private final Reason reason;
    private final Player player;
    private final PlayerInventory inventory;
    private final Cancellable event;

    public PlayerInventoryChangeEvent(Reason reason, Player player, PlayerInventory inventory) {
        this(reason, player, inventory, null);
    }

    public <T extends Event & Cancellable> PlayerInventoryChangeEvent(Reason reason, Player player, PlayerInventory inventory, T event) {
        this.reason = reason;
        this.player = player;
        this.inventory = inventory;
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return this.event != null && this.event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        if (this.event != null) {
            this.event.setCancelled(cancel);
        }
    }

    public Reason getReason() {
        return this.reason;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerInventory getInventory() {
        return this.inventory;
    }
}
