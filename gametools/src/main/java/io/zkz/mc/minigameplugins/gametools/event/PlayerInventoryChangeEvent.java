package io.zkz.mc.minigameplugins.gametools.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventoryChangeEvent extends AbstractEvent implements Cancellable {
    private final Player player;
    private final PlayerInventory inventory;
    private final Cancellable event;

    public PlayerInventoryChangeEvent(Player player, PlayerInventory inventory) {
        this(player, inventory, null);
    }

    public <T extends Event & Cancellable> PlayerInventoryChangeEvent(Player player, PlayerInventory inventory, T event) {
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

    public Player getPlayer() {
        return this.player;
    }

    public PlayerInventory getInventory() {
        return this.inventory;
    }
}
