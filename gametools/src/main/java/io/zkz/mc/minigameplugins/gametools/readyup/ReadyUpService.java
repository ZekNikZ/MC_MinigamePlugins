package io.zkz.mc.minigameplugins.gametools.readyup;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ReadyUpService extends GameToolsService {
    private static final ReadyUpService INSTANCE = new ReadyUpService();

    public static ReadyUpService getInstance() {
        return INSTANCE;
    }

    private int nextId = 0;
    private final Map<Integer, ReadyUpSession> sessions = new ConcurrentHashMap<>();

    public int waitForReady(Collection<UUID> players, Runnable onAllReady) {
        return this.waitForReady(players, onAllReady, null);
    }

    public int waitForReady(Collection<UUID> players, Runnable onAllReady, BiConsumer<Player, ReadyUpSession> onPlayerReady) {
        int id = this.nextId++;
        ReadyUpSession session = new ReadyUpSession(id, players, Objects.requireNonNull(onAllReady), onPlayerReady);
        this.sessions.put(id, session);
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                displayInitialReadyMessage(player);
            }
        });
        return id;
    }

    public void cancelReadyWait(int id, boolean runHandler) {
        ReadyUpSession session = this.sessions.get(id);
        if (session != null) {
            if (runHandler) {
                session.complete();
            } else {
                session.cancel();
            }
        }
    }

    /**
     * Called by ReadyUpSession#cancel()
     * @param id the session id
     */
    void cleanupSession(int id) {
        this.sessions.remove(id);
    }

    public boolean recordReady(Player player) {
        boolean success = false;
        for (ReadyUpSession session : this.sessions.values()) {
            success |= session.markPlayerAsReady(player);
        }

        return success;
    }

    public boolean undoReady(UUID playerId) {
        boolean success = false;
        for (ReadyUpSession session : this.sessions.values()) {
            success |= session.undoReady(playerId);
        }

        return success;
    }

    private void displayInitialReadyMessage(Player player) {
        Chat.sendAlert(player, ChatType.GAME_INFO, "Are you ready? Type " + ChatColor.AQUA + "/ready" + ChatColor.RESET + " to confirm.");
        player.playSound(player.getLocation(), StandardSounds.ALERT_INFO, 1, 1);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (this.sessions.values().stream().anyMatch(s -> s.isPlayerTracked(event.getPlayer()))) {
            displayInitialReadyMessage(event.getPlayer());
        }
    }

    public void sendStatus(CommandSender sender) {
        this.sessions.forEach((id, session) -> {
            Chat.sendMessageFormatted(sender, ChatColor.GOLD + "Session %d:", id);
            Chat.sendMessage(sender, ChatColor.GREEN + " - ready: " + String.join(", ", session.getReadyPlayerNames()));
            Chat.sendMessage(sender, ChatColor.RED + " - not ready: " + String.join(", ", session.getNotReadyPlayerNames()));
        });
    }
}
