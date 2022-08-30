package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class VanishingService extends PluginService<GameToolsPlugin> {
    private static final VanishingService INSTANCE = new VanishingService();

    public static VanishingService getInstance() {
        return INSTANCE;
    }

    private final Set<UUID> globallyHiddenPlayers = new HashSet<>();

    /**
     * Map of player to players they cannot see.
     */
    private final Map<UUID, Set<UUID>> hiddenPlayers = new HashMap<>();
    
    public Set<UUID> getHiddenPlayers(UUID playerId) {
        if (!this.hiddenPlayers.containsKey(playerId)) {
            this.hiddenPlayers.put(playerId, new HashSet<>());
        }
        
        return this.hiddenPlayers.get(playerId);
    }

    /**
     * Check if player can see target.
     */
    public boolean canSee(Player player, Player target) {
        return this.canSee(player.getUniqueId(), target.getUniqueId());
    }

    /**
     * Check if player can see target.
     */
    public boolean canSee(UUID playerId, UUID targetId) {
        return !this.globallyHiddenPlayers.contains(targetId) && !this.getHiddenPlayers(playerId).contains(targetId);
    }
    
    public void hidePlayer(Player player) {
        this.hidePlayer(player, Bukkit.getOnlinePlayers());
    }
    public void hidePlayer(UUID playerId) {
        this.globallyHiddenPlayers.add(playerId);
    }
    public void hidePlayer(Player player, Collection<? extends Player> otherPlayers) {
        this.hidePlayer(player.getUniqueId(), otherPlayers.stream().map(Player::getUniqueId).toList());
    }
    public void hidePlayer(UUID playerId, Collection<UUID> otherPlayerIds) {
        otherPlayerIds.forEach(op -> this.getHiddenPlayers(op).add(playerId));
    }

    public void showPlayer(Player player) {
        this.showPlayer(player.getUniqueId());
    }
    public void showPlayer(UUID playerId) {
        this.globallyHiddenPlayers.remove(playerId);
        this.hiddenPlayers.forEach((p, s) -> s.remove(playerId));
    }
    public void showPlayer(Player player, Collection<? extends Player> otherPlayers) {
        this.showPlayer(player.getUniqueId(), otherPlayers.stream().map(Player::getUniqueId).toList());
    }
    public void showPlayer(UUID playerId, Collection<UUID> otherPlayerIds) {
        otherPlayerIds.forEach(op -> this.getHiddenPlayers(op).remove(playerId));
    }

    public void togglePlayer(Player player) {
        this.togglePlayer(player.getUniqueId());
    }
    public void togglePlayer(UUID playerId) {
        // if they are globally hidden, show them
        if (this.globallyHiddenPlayers.contains(playerId)) {
            this.showPlayer(playerId);
        } else {
            this.hidePlayer(playerId);
        }
    }
    public void togglePlayer(Player player, Player otherPlayer) {
        this.togglePlayer(player.getUniqueId(), otherPlayer.getUniqueId());
    }
    public void togglePlayer(UUID playerId, UUID otherPlayerId) {
        // if they are globally hidden, show them
        if (this.getHiddenPlayers(otherPlayerId).contains(playerId)) {
            this.showPlayer(playerId, List.of(otherPlayerId));
        } else {
            this.hidePlayer(playerId, List.of(otherPlayerId));
        }
    }
}
