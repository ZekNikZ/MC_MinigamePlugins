package io.zkz.mc.minigameplugins.gametools.inventory.opener;

import com.google.common.base.Preconditions;
import io.zkz.mc.minigameplugins.gametools.inventory.CustomUI;
import io.zkz.mc.minigameplugins.gametools.inventory.InventoryService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class ChestInventoryOpener implements InventoryOpener {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Inventory open(CustomUI inv, Player player) {
        Preconditions.checkArgument(inv.cols() == 9,
            "The column count for the chest inventory must be 9, found: %s.", inv.cols());
        Preconditions.checkArgument(inv.rows() >= 1 && inv.rows() <= 6,
            "The row count for the chest inventory must be between 1 and 6, found: %s", inv.rows());

        Inventory handle = Bukkit.createInventory(player, inv.rows() * inv.cols(), inv.title());

        fill(handle, InventoryService.getInstance().getContents(player).get());

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }
}
