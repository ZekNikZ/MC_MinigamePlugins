package io.zkz.mc.minigameplugins.survivalgames;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ParkourService extends PluginService<SGPlugin> {
    private final Map<UUID, Location> parkourCheckpoints = new HashMap<>();
    private final Map<UUID, Boolean> isInParkour = new HashMap<>();

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.isInParkour.put(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.parkourCheckpoints.remove(player.getUniqueId());
        this.isInParkour.remove(player.getUniqueId());
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        // Player checkpoint
        if (loc.add(0, -1, 0).getBlock().getType() == Material.GOLD_BLOCK) {
            Location checkpoint = loc.getBlock().getLocation().add(0.5, 1, 0.5);
            if (!Objects.equals(this.parkourCheckpoints.get(player.getUniqueId()), checkpoint)) {
                this.parkourCheckpoints.put(player.getUniqueId(), checkpoint);
                Chat.sendAlert(player, ChatType.ACTIVE_INFO, "Checkpoint reached!");
            }
        }

        // Nether star
        com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(weLoc);
        boolean isInParkour = false;
        for (ProtectedRegion protectedRegion : set) {
            if (protectedRegion.getId().equals("parkour")) {
                isInParkour = true;
                break;
            }
        }
        if (isInParkour && !this.isInParkour.get(player.getUniqueId())) {
            player.getInventory().addItem(ISB.material(Material.NETHER_STAR).name("Teleport to last checkpoint").lore("Right click this item to teleport", "to your last checkpoint!").build());
        } else if (!isInParkour && this.isInParkour.get(player.getUniqueId())) {
            player.getInventory().clear();
        }
        this.isInParkour.put(player.getUniqueId(), isInParkour);
    }

    @EventHandler
    private void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (this.parkourCheckpoints.get(player.getUniqueId()) != null) {
                player.teleport(this.parkourCheckpoints.get(player.getUniqueId()));
            }
        }
    }
}
