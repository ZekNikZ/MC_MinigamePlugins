package io.zkz.mc.minigameplugins.gametools.inventory.opener;

import io.zkz.mc.minigameplugins.gametools.inventory.CustomUI;
import io.zkz.mc.minigameplugins.gametools.inventory.UIContents;
import io.zkz.mc.minigameplugins.gametools.inventory.item.InventoryItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.List;

public interface InventoryOpener {
    Inventory open(CustomUI inv, Player player);

    boolean supports(InventoryType type);

    default void fill(Inventory handle, UIContents contents) {
        List<InventoryItem> items = contents.items();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                handle.setItem(i, items.get(i).getItemStack());
            }
        }
    }
}
