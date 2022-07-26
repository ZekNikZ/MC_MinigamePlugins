package io.zkz.mc.minigameplugins.gametools.readyup;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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

    private void displayInitialReadyMessage(Player player) {
        player.sendMessage(MinigameConstantsService.getInstance().getPrefix() + "Are you ready? Type /ready to confirm.");
        player.playSound(player.getLocation(), StandardSounds.ALERT_INFO, 1, 1);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (this.sessions.values().stream().anyMatch(s -> s.isPlayerTracked(event.getPlayer()))) {
            displayInitialReadyMessage(event.getPlayer());
        }
    }
}
