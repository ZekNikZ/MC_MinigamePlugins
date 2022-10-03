package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Service
public class ActionBarService extends PluginService<GameToolsPlugin> {
    private static final ActionBarService INSTANCE = new ActionBarService();

    public static ActionBarService getInstance() {
        return INSTANCE;
    }

    private BukkitTask task;
    private int index = 0;
    private final Map<UUID, List<String>> messageOrder = new HashMap<>();
    private final Map<UUID, Map<String, Component>> messages = new HashMap<>();

    @Override
    protected void onEnable() {
        this.task = Bukkit.getScheduler().runTaskTimer(this.getPlugin(), this::displayActionBarMessages, 40, 40);
    }

    @Override
    protected void onDisable() {
        this.task.cancel();
        this.task = null;
    }

    @SuppressWarnings("java:S3824")
    public void addMessage(UUID playerId, String key, Component message) {
        if (!this.messages.containsKey(playerId)) {
            this.messages.put(playerId, new HashMap<>());
            this.messageOrder.put(playerId, new ArrayList<>());
        }
        this.messages.get(playerId).put(key, message);
        this.messageOrder.get(playerId).add(key);
    }

    public void removeMessage(UUID playerId, String key) {
        if (!this.messages.containsKey(playerId)) {
            return;
        }

        this.messages.get(playerId).remove(key);
        this.messageOrder.get(playerId).remove(key);
        if (this.messages.get(playerId).isEmpty()) {
            this.messages.remove(playerId);
            this.messageOrder.remove(playerId);
        }
    }

    public void displayActionBarMessages() {
        this.messages.keySet().forEach(playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                return;
            }

            player.sendActionBar(this.messages.get(playerId).get(this.messageOrder.get(playerId).get(this.index % this.messageOrder.get(playerId).size())));
        });

        ++this.index;
    }
}
