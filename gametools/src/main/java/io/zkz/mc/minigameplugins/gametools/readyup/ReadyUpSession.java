package io.zkz.mc.minigameplugins.gametools.readyup;

import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class ReadyUpSession {
    private final int sessionId;
    private final Map<UUID, Boolean> readyPlayers = new ConcurrentHashMap<>();
    private final Runnable onAllReady;
    private final BiConsumer<Player, ReadyUpSession> onPlayerReady;
    private final int taskId;
    private final BossBar bossBar;

    ReadyUpSession(int sessionId, Collection<UUID> players, Runnable onAllReady, @Nullable BiConsumer<Player, ReadyUpSession> onPlayerReady) {
        this.sessionId = sessionId;
        this.onAllReady = onAllReady;
        this.onPlayerReady = onPlayerReady;
        players.forEach(playerId -> this.readyPlayers.put(playerId, false));
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ReadyUpService.getInstance().getPlugin(), this::displayReadyActionbarMessages, 10, 10);
        this.bossBar = Bukkit.createBossBar("Ready Up: 0/" + players.size() + " players ready", BarColor.GREEN, BarStyle.SOLID);
        this.updateBossbar();
        this.bossBar.setVisible(true);
        players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(this.bossBar::addPlayer);
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
        this.bossBar.removeAll();
    }

    public void complete() {
        this.onAllReady.run();
        this.cancel();
    }

    /**
     * Mark a player as ready
     *
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
            Chat.sendMessage(p, ChatType.PASSIVE_INFO, mm("<0> is ready!", player.displayName()));
        });

        this.updateBossbar();

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

    public boolean undoReady(UUID playerId) {
        this.readyPlayers.put(playerId, false);
        this.updateBossbar();
        return true;
    }

    public List<Component> getReadyPlayerDisplayNames() {
        return this.readyPlayers.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(entry -> {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    return player.displayName();
                }

                return mm("<dark_red>" + Bukkit.getOfflinePlayer(entry.getKey()).getName() + " (offline)");
            })
            .toList();
    }

    public List<Component> getReadyPlayerNames() {
        return this.readyPlayers.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(entry -> {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    return player.name();
                }

                return mm(Bukkit.getOfflinePlayer(entry.getKey()).getName());
            })
            .toList();
    }

    public List<Component> getNotReadyPlayerDisplayNames() {
        return this.readyPlayers.entrySet().stream()
            .filter(key -> !key.getValue())
            .map(entry -> {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    return player.displayName();
                }

                return mm("<dark_red>" + Bukkit.getOfflinePlayer(entry.getKey()).getName() + " (offline)");
            })
            .toList();
    }

    private void updateBossbar() {
        this.bossBar.setTitle("Ready Up: " + this.getReadyPlayerCount() + "/" + this.getTotalPlayerCount() + " players ready");
        this.bossBar.setProgress((double) this.getReadyPlayerCount() / this.getTotalPlayerCount());
    }
}
