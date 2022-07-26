package io.zkz.mc.minigameplugins.gametools.event;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class CustomEventService extends GameToolsService {
    private static final CustomEventService INSTANCE = new CustomEventService();

    public static CustomEventService getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent((Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent((Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent((Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(event.getPlayer(), event.getPlayer().getInventory(), event));
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(event.getPlayer(), event.getPlayer().getInventory(), event));
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(event.getPlayer(), event.getPlayer().getInventory()));
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent((Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onUseBucket(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInHand().getType() != Material.BUCKET && event.getPlayer().getInventory().getItemInHand().getType() != Material.WATER_BUCKET) {
            return;
        }

        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(event.getPlayer(), event.getPlayer().getInventory(), event));
    }
}
