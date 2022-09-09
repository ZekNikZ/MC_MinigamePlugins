package io.zkz.mc.minigameplugins.lobby;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// TODO: parkour checkpoint & initial spawn pitch and yaw
public class LobbyService extends PluginService<LobbyPlugin> {
    private static final LobbyService INSTANCE = new LobbyService();

    public static LobbyService getInstance() {
        return INSTANCE;
    }

    private final Map<UUID, Location> parkourCheckpoints = new HashMap<>();
    private final Map<UUID, Boolean> isInParkour = new HashMap<>();

    @Override
    protected void onEnable() {
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.GOLD + ChatColor.BOLD + "MC Tournament 1");
        scoreboard.addEntry(new EventTeamScoresScoreboardEntry());
        ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 115, 36, -180, 0));
        player.setHealth(20);
        player.setSaturation(20);
        player.setGameMode(GameMode.ADVENTURE);
        this.isInParkour.put(player.getUniqueId(), false);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.isInParkour.remove(player.getUniqueId());
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setDamage(0);
        }
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
