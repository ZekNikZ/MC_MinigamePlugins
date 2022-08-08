package io.zkz.mc.minigameplugins.bingo.map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class BingoCardMap {
    private static final MapView MAP_VIEW;
    private static final BingoCardMapRenderer RENDERER;

    static {
        MAP_VIEW = Bukkit.createMap(Bukkit.getWorlds().get(0));
        MAP_VIEW.getRenderers().clear();
        MAP_VIEW.setTrackingPosition(false);
        MAP_VIEW.addRenderer(RENDERER = new BingoCardMapRenderer());
    }

    public static ItemStack makeMap() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) Bukkit.getItemFactory().getItemMeta(Material.FILLED_MAP);
        mapMeta.setMapView(MAP_VIEW);
        mapItem.setItemMeta(mapMeta);
        return mapItem;
    }

    public static void markDirty() {
        RENDERER.markDirty();
    }
}
