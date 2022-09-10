package io.zkz.mc.minigameplugins.lobby;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Client.Bukkit.Event.SubStartedEvent;
import net.ME1312.SubServers.Client.Bukkit.Event.SubStoppedEvent;
import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import net.ME1312.SubServers.Client.Common.Network.API.SubCreator;
import net.ME1312.SubServers.Client.Common.Network.API.SubServer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
        this.resetPodiums();

        if (this.currentMinigameId == null) {
            return;
        }

        String minigameId = this.currentMinigameId;
        this.currentMinigameId = null;
        this.db.addAction(conn -> {
            List<String> keys = List.of("minigameId", "roundNumber");

            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO mm_minigame_state (id, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = ?;"
            )) {
                conn.setAutoCommit(false);

                for (String key : keys) {
                    statement.setString(1, key);
                    statement.setString(2, null);
                    statement.setString(3, key);
                    statement.addBatch();
                }

                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
            }
        });
        this.removeServer(minigameId);
    }

    public void resetPodiums() {
        Map<GameTeam, Double> scores = TeamService.getInstance().getAllNonSpectatorTeams().stream().collect(Collectors.toMap(team -> team, team -> 0.0));
        ScoreService.getInstance().loadAllData();
        ScoreService.getInstance().getEventTeamScoreSummary().forEach((team, score) -> {
            if (scores.containsKey(team)) {
                scores.put(team, score);
            }
        });
        List<Map.Entry<GameTeam, Double>> entries = scores.entrySet().stream()
            .sorted(Comparator.comparing((Function<Map.Entry<GameTeam, Double>, Double>) Map.Entry::getValue).reversed().thenComparing(entry -> entry.getKey().getDisplayName()))
            .toList();
        WorldEditService we = WorldEditService.getInstance();
        var weWorld = we.wrapWorld(Bukkit.getWorld("world"));
        for (int i = 0; i < entries.size(); i++) {
            Pair<BlockVector3, BlockVector3> podium = Podiums.PODIUMS.get(i);
            we.replaceRegion(
                weWorld,
                we.createCuboidRegion(podium.first(), podium.second()),
                we.createMask(weWorld, BlockUtils.allConcretes().toArray(new Material[0])),
                we.createPattern(entries.get(i).getKey().getConcreteColor())
            );
        }
    }

    @EventHandler
    private void onSubServerStart(SubStartedEvent event) {
        if (this.currentMinigameId != null && event.getServer().equals(this.currentMinigameId)) {
            this.sendPlayersToServer(this.currentMinigameId);
        }
    }

    @EventHandler
    private void onSubServerStop(SubStoppedEvent event) {
        ScoreService.getInstance().loadAllData();
        this.resetPodiums();
        SpinnerService.getInstance().resetSpinner();
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
            this.db = new MySQLDataManager<>(this, (conn) -> {
            })
        );
    }

    public record MinigameData(int spinnerId, String id, String name, String icon, boolean selected) {
    }

    public @Nullable List<MinigameData> getMinigames() {
        try (Connection conn = MySQLService.getInstance().getConnection(); PreparedStatement preparedStatement = conn.prepareStatement("SELECT * from mm_minigames;")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<MinigameData> minigames = new ArrayList<>();
            while (resultSet.next()) {
                minigames.add(new MinigameData(
                    resultSet.getInt("minigameSpinnerId"),
                    resultSet.getString("minigameId"),
                    resultSet.getString("minigameName"),
                    resultSet.getString("minigameIcon"),
                    resultSet.getBoolean("minigameSelected")
                ));
            }
            minigames.sort(Comparator.comparing(MinigameData::spinnerId));
            return minigames;
        } catch (SQLException e) {
            return null;
        }
    }

    public void chooseNextMinigame(MinigameData minigame) {
        TitleUtils.broadcastTitle("" + ChatColor.GOLD + ChatColor.BOLD + minigame.name(), ChatColor.AQUA + "You will be teleported in a few minutes.", 20, 120);
        SoundUtils.playSound(StandardSounds.GOAL_MET_MAJOR, 1, 1);
        this.db.addAction(conn -> {
            try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE mm_minigames SET minigameSelected=? WHERE minigameId=?;"
            )) {
                statement.setBoolean(1, true);
                statement.setString(2, minigame.id());
                statement.executeUpdate();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
            }
        });
        this.startMinigame(minigame.id());
    }

    public void resetAllMinigames() {
        this.db.addAction(conn -> {
            try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE mm_minigames SET minigameSelected=?;"
            )) {
                statement.setBoolean(1, false);
                statement.executeUpdate();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
            }
        });
        SpinnerService.getInstance().resetSpinner();
        this.resetPodiums();
    }
}
