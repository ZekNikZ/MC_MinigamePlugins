package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLDataManager;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Client.Bukkit.Event.SubStartedEvent;
import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import net.ME1312.SubServers.Client.Common.Network.API.SubCreator;
import net.ME1312.SubServers.Client.Common.Network.API.SubServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TournamentManager extends PluginService<LobbyPlugin> {
    private static final TournamentManager INSTANCE = new TournamentManager();

    public static TournamentManager getInstance() {
        return INSTANCE;
    }

    private String currentMinigameId = null;
    private MySQLDataManager<TournamentManager> db;

    public void createAndStartServer(String templateId) {
        // Note: this assumes only one host
        SubAPI.getInstance().getHosts(hosts -> {
            SubCreator creator = hosts.get("~").getCreator();
            SubCreator.ServerTemplate template = creator.getTemplate(templateId);
            creator.create(templateId, template, Version.fromString("1.19.2"), null, i -> currentMinigameId = templateId);
        });
    }

    public void sendPlayersToServer(String minigameId) {
        SubAPI.getInstance().getRemotePlayers(players -> {
            players.forEach((playerId, player) -> {
                player.transfer(minigameId);
            });
        });
    }

    private void removeServer(String minigameId) {
        SubAPI.getInstance().getSubServer(minigameId, SubServer::stop);
    }

    public void startMinigame(String minigameId) {
        this.currentMinigameId = minigameId;
        this.db.addAction(conn -> {
            Map<String, String> values = Map.of(
                "minigameId", minigameId,
                "roundNumber", "0"
            );

            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO mm_minigame_state (id, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = ?;"
            )) {
                conn.setAutoCommit(false);

                for (Map.Entry<String, String> entry : values.entrySet()) {
                    String id = entry.getKey();
                    String value = entry.getValue();
                    statement.setString(1, id);
                    statement.setString(2, value);
                    statement.setString(3, id);
                    statement.addBatch();
                }

                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
            }
        });
        this.createAndStartServer(this.currentMinigameId);
    }

    public void resetMinigame() {
        String minigameId = this.currentMinigameId;
        this.currentMinigameId = null;
        this.db.addAction(conn -> {

        });
        this.removeServer(minigameId);
    }

    @EventHandler
    private void onSubServerStart(SubStartedEvent event) {
        if (this.currentMinigameId != null && event.getServer().equals(this.currentMinigameId)) {
            this.sendPlayersToServer(this.currentMinigameId);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (this.currentMinigameId != null) {
            Chat.sendAlert(event.getPlayer(), ChatType.ACTIVE_INFO, "A minigame is currently in progress. Sending you to that server in 10 seconds...");
            BukkitUtils.runLater(() -> {
                SubAPI.getInstance().getRemotePlayer(event.getPlayer().getUniqueId(), rp -> {
                    rp.transfer(this.currentMinigameId);
                });
            }, 200);
        }
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            this.db = new MySQLDataManager<>(this, (conn) -> {})
        );
    }
}
