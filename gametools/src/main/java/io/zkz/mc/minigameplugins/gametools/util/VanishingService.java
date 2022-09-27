package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Service
public class VanishingService extends PluginService<GameToolsPlugin> {
    private static final VanishingService INSTANCE = new VanishingService();

    public static VanishingService getInstance() {
        return INSTANCE;
    }
    /**
     * Map of player to players they cannot see.
     */
    private final Map<UUID, Set<String>> hiddenPlayers = new HashMap<>();

    public Set<UUID> getHiddenPlayers() {
        return this.hiddenPlayers.keySet();
    }

    /**
     * Check if player can see target.
     */
    public boolean canSee(Player player) {
        return this.canSee(player.getUniqueId());
    }

    /**
     * Check if player can see target.
     */
    public boolean canSee(UUID playerId) {
        return !this.hiddenPlayers.containsKey(playerId);
    }

    public void hidePlayer(Player player, String reason) {
        this.hidePlayer(player.getUniqueId(), reason);
    }

    public void hidePlayer(UUID playerId, String reason) {
        this.hiddenPlayers.computeIfAbsent(playerId, u -> new HashSet<>());
        this.hiddenPlayers.get(playerId).add(reason);
    }

    public void showPlayer(Player player, String reason) {
        this.showPlayer(player.getUniqueId(), reason);
    }

    public void showPlayer(UUID playerId, String reason) {
        if (!this.hiddenPlayers.containsKey(playerId)) {
            return;
        }

        this.hiddenPlayers.get(playerId).remove(reason);
    }

    public void togglePlayer(Player player, String reason) {
        this.togglePlayer(player.getUniqueId(), reason);
    }

    public void togglePlayer(UUID playerId, String reason) {
        if (this.canSee(playerId)) {
            this.hidePlayer(playerId, reason);
        } else {
            this.showPlayer(playerId, reason);
        }
    }

    public @NotNull Optional<Set<String>> getPlayerHiddenReasons(Player player) {
        return this.getPlayerHiddenReasons(player.getUniqueId());
    }

    public @NotNull Optional<Set<String>> getPlayerHiddenReasons(UUID playerId) {
        return Optional.ofNullable(this.hiddenPlayers.get(playerId));
    }
}
