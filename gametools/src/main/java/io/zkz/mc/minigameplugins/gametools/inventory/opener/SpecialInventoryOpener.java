package io.zkz.mc.minigameplugins.gametools.inventory.opener;

import fr.minuskube.inv.InventoryManager;
import io.zkz.mc.minigameplugins.gametools.inventory.CustomInventory;
import io.zkz.mc.minigameplugins.gametools.inventory.InventoryService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class SpecialInventoryOpener implements InventoryOpener {
    private static final List<InventoryType> SUPPORTED = List.of(
        InventoryType.FURNACE,
        InventoryType.WORKBENCH,
        InventoryType.DISPENSER,
        InventoryType.DROPPER,
        InventoryType.ENCHANTING,
        InventoryType.BREWING,
        InventoryType.ANVIL,
        InventoryType.BEACON,
        InventoryType.HOPPER
    );

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Inventory open(CustomInventory inv, Player player) {
        Inventory handle = Bukkit.createInventory(player, inv.type(), inv.title());

        fill(handle, InventoryService.getInstance().getContents(player).get());

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return SUPPORTED.contains(type);
    }
}
