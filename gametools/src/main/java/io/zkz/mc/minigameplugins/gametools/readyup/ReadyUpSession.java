package io.zkz.mc.minigameplugins.gametools.readyup;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ReadyUpSession {
    private final int sessionId;
    private final Map<UUID, Boolean> readyPlayers = new ConcurrentHashMap<>();
    private final Runnable onAllReady;
    private final BiConsumer<Player, ReadyUpSession> onPlayerReady;
    private final int taskId;

    ReadyUpSession(int sessionId, Collection<UUID> players, Runnable onAllReady, @Nullable BiConsumer<Player, ReadyUpSession> onPlayerReady) {
        this.sessionId = sessionId;
        this.onAllReady = onAllReady;
        this.onPlayerReady = onPlayerReady;
        players.forEach(playerId -> this.readyPlayers.put(playerId, false));
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ReadyUpService.getInstance().getPlugin(), this::displayReadyActionbarMessages, 10, 10);
    }

    public long getReadyPlayerCount() {
        return this.readyPlayers.values().stream().filter(Boolean::booleanValue).count();
    }

    public long getTotalPlayerCount() {
        return this.readyPlayers.size();
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.taskId);
        ReadyUpService.getInstance().cleanupSession(this.sessionId);
    }

    public void complete() {
        this.onAllReady.run();
        this.cancel();
    }

    /**
     * Mark a player as ready
     * @param player the player
     * @return whether or not the player was marked as ready (= whether the player was tracked and not already ready)
     */
    public boolean markPlayerAsReady(Player player) {
        if (!this.isPlayerTracked(player) || this.readyPlayers.get(player.getUniqueId())) {
            return false;
        }

        this.readyPlayers.put(player.getUniqueId(), true);

        // Player ready callback
        if (this.onPlayerReady != null) {
            this.onPlayerReady.accept(player, this);
        }

        // Chat message
        this.readyPlayers.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(p -> {
            // Don't send chat message to self
            if (player.equals(p)) {
                return;
            }

            GameTeam playerTeam = TeamService.getInstance().getTeamOfPlayer(player);
            if (playerTeam != null) {
                Chat.sendAlert(ChatType.PASSIVE_INFO, "" + playerTeam.getFormatCode() + ChatColor.BOLD + playerTeam.getPrefix() + " " + playerTeam.getFormatCode() + player.getDisplayName() + ChatColor.RESET + " is ready!");
            } else {
                Chat.sendAlert(ChatType.PASSIVE_INFO, player.getDisplayName() + " is ready!");
            }
        });

        // Check if this was the last player
        if (this.getReadyPlayerCount() == this.getTotalPlayerCount()) {
            this.complete();
        }

        return true;
    }

    private void displayReadyActionbarMessages() {
        this.readyPlayers.entrySet().stream().filter(entry -> !entry.getValue()).forEach(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                TitleUtils.sendActionBarMessage(player, ChatColor.GOLD + "Are you ready? Type /ready to confirm.");
            }
        });
    }

    public boolean isPlayerTracked(Player player) {
        return this.readyPlayers.containsKey(player.getUniqueId());
    }
}
