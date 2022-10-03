package io.zkz.mc.minigameplugins.gametools.event;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import static io.zkz.mc.minigameplugins.gametools.event.PlayerInventoryChangeEvent.Reason.*;

@Service
public class CustomEventService extends PluginService<GameToolsPlugin> {
    private static final CustomEventService INSTANCE = new CustomEventService();

    public static CustomEventService getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(INVENTORY_INTERACT_GENERAL, (Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(INVENTORY_INTERACT_CLICK, (Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(INVENTORY_INTERACT_DRAG, (Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_ITEM_DROP, event.getPlayer(), event.getPlayer().getInventory(), event));
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_ITEM_CONSUME, event.getPlayer(), event.getPlayer().getInventory(), event));
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_ITEM_BREAK, event.getPlayer(), event.getPlayer().getInventory()));
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_ITEM_CRAFT, (Player) event.getWhoClicked(), event.getWhoClicked().getInventory(), event));
    }

    @EventHandler
    public void onUseBucket(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BUCKET && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.WATER_BUCKET) {
            return;
        }

        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_INTERACT_ENTITY, event.getPlayer(), event.getPlayer().getInventory(), event));
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        BukkitUtils.dispatchNextTick(() -> new PlayerInventoryChangeEvent(PLAYER_ITEM_PICK_UP, ((Player) event.getEntity()), ((Player) event.getEntity()).getInventory(), event));
    }
}
