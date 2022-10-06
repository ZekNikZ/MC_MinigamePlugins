package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.inventory.opener.ChestInventoryOpener;
import io.zkz.mc.minigameplugins.gametools.inventory.opener.InventoryOpener;
import io.zkz.mc.minigameplugins.gametools.inventory.opener.SpecialInventoryOpener;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO: iterators
// TODO: pagination
// TODO: get shift clicking to work
// TODO: get InteractableSlotItem to work

@Service
public class InventoryService extends PluginService<GameToolsPlugin> {
    private static final InventoryService INSTANCE = new InventoryService();

    public static InventoryService getInstance() {
        return INSTANCE;
    }

    private final Map<Player, CustomInventory> inventories = new HashMap<>();

    private final Map<Player, InventoryContentProvider> contents = new HashMap<>();

    private final List<InventoryOpener> openers = new ArrayList<>();

    private final List<InventoryOpener> defaultOpeners = List.of(
        new ChestInventoryOpener(),
        new SpecialInventoryOpener()
    );

    public Optional<CustomInventory> getInventory(Player player) {
        return Optional.ofNullable(this.inventories.get(player));
    }

    public void setInventory(Player player, @Nullable CustomInventory inv) {
        if (inv == null) {
            this.inventories.remove(player);
        } else {
            this.inventories.put(player, inv);
        }
    }

    public Optional<InventoryContentProvider> getContents(Player player) {
        return Optional.ofNullable(this.contents.get(player));
    }

    public void setContents(Player player, @Nullable InventoryContentProvider contents) {
        if (contents == null) {
            this.contents.remove(player);
        } else {
            this.contents.put(player, contents);
        }
    }

    public Optional<InventoryOpener> getOpener(InventoryType type) {
        var opener = this.openers.stream()
            .filter(o -> o.supports(type))
            .findFirst();

        if (opener.isEmpty()) {
            opener = this.defaultOpeners.stream()
                .filter(o -> o.supports(type))
                .findFirst();
        }

        return opener;
    }

    public void registerOpeners(InventoryOpener... openers) {
        this.openers.addAll(Arrays.asList(openers));
    }

    public List<Player> getOpenedPlayers(CustomInventory inv) {
        return this.inventories.entrySet().stream()
            .filter(e -> inv.equals(e.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }

    @Override
    protected void onEnable() {
        new InvTask().runTaskTimer(this.getPlugin(), 1, 1);
    }
    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!inventories.containsKey(p))
            return;

        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR ||
            e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            e.getAction() == InventoryAction.NOTHING) {

            e.setCancelled(true);
            return;
        }

        if (e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
            e.setCancelled(true);

            if (e.getSlot() < 0) {
                return;
            }

            CustomInventory inv = this.inventories.get(p);

            int row = e.getSlot() / inv.cols();
            int column = e.getSlot() % inv.cols();

            if (row >= inv.rows() || column >= inv.cols())
                return;

//            inv.getListeners().stream()
//                .filter(listener -> listener.getType() == InventoryClickEvent.class)
//                .forEach(listener -> ((InventoryListener<InventoryClickEvent>) listener).accept(e));

            this.contents.get(p).get(row, column).ifPresent(item -> item.handleClick(e));

            p.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!this.inventories.containsKey(p))
            return;

        CustomInventory inv = this.inventories.get(p);

        for (int slot : e.getRawSlots()) {
            if (slot >= p.getOpenInventory().getTopInventory().getSize())
                continue;

            e.setCancelled(true);
            break;
        }

//        inv.getListeners().stream()
//            .filter(listener -> listener.getType() == InventoryDragEvent.class)
//            .forEach(listener -> ((InventoryListener<InventoryDragEvent>) listener).accept(e));
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        if (!inventories.containsKey(p))
            return;

        CustomInventory inv = inventories.get(p);

//        inv.getListeners().stream()
//            .filter(listener -> listener.getType() == InventoryOpenEvent.class)
//            .forEach(listener -> ((InventoryListener<InventoryOpenEvent>) listener).accept(e));
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (!inventories.containsKey(p))
            return;

        CustomInventory inv = inventories.get(p);

//        inv.getListeners().stream()
//            .filter(listener -> listener.getType() == InventoryCloseEvent.class)
//            .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener).accept(e));

        if (inv.closeable()) {
            e.getInventory().clear();

            inventories.remove(p);
            contents.remove(p);
        } else {
            BukkitUtils.runNow(() -> {
                p.openInventory(e.getInventory());
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (!this.inventories.containsKey(p))
            return;

        CustomInventory inv = this.inventories.get(p);

//        inv.getListeners().stream()
//            .filter(listener -> listener.getType() == PlayerQuitEvent.class)
//            .forEach(listener -> ((InventoryListener<PlayerQuitEvent>) listener).accept(e));

        this.inventories.remove(p);
        this.contents.remove(p);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(PluginDisableEvent e) {
        new HashMap<>(this.inventories).forEach((player, inv) -> {
//            inv.getListeners().stream()
//                .filter(listener -> listener.getType() == PluginDisableEvent.class)
//                .forEach(listener -> ((InventoryListener<PluginDisableEvent>) listener).accept(e));

            inv.close(player);
        });

        this.inventories.clear();
        this.contents.clear();
    }

    class InvTask extends BukkitRunnable {
        @Override
        public void run() {
            contents.values().forEach(InventoryContentProvider::update);
        }
    }

//    @EventHandler
//    private void onClick(InventoryClickEvent event) {
//        Player player = ((Player) event.getWhoClicked());
//        player.sendMessage("-----");
//        player.sendMessage("inv: " + (event.getClickedInventory() == null ? "null" : event.getClickedInventory().getClass().getName()));
//        player.sendMessage("click: " + event.getClick().name());
//        player.sendMessage("slotType: " + event.getSlotType().name());
//        player.sendMessage("slot: " + event.getSlot());
//        player.sendMessage("rawSlot: " + event.getRawSlot());
//        player.sendMessage("currentItem: " + (event.getCurrentItem() == null ? "null" : event.getCurrentItem().getType().name()));
//        player.sendMessage("cursorItem: " + (event.getCursor() == null ? "null" : event.getCursor().getType().name()));
//        player.sendMessage("action: " + event.getAction().name());
//        player.sendMessage("hotbarButton: " + event.getHotbarButton());
//        player.sendMessage("-----");
//
//        // Ignore this if no inventory attached
//        if (event.getClickedInventory() == null) {
//            return;
//        }
//
//        // If this is a FakeInventory, cancel
//        if (event.getClickedInventory().getHolder() instanceof FakeInventory) {
//            event.setCancelled(true);
//        }
//    }
}
