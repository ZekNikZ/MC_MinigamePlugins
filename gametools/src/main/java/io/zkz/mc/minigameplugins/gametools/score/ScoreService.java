package io.zkz.mc.minigameplugins.gametools.score;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLDataManager;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.IObservable;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class ScoreService extends PluginService<GameToolsPlugin> implements IObservable {
    private static final ScoreService INSTANCE = new ScoreService();

    public static ScoreService getInstance() {
        return INSTANCE;
    }

    private final List<ScoreEntry> entries = new ArrayList<>();

    private final Map<UUID, Double> roundPlayerScores = new HashMap<>();
    private final Map<GameTeam, Double> roundTeamScores = new HashMap<>();

    private final Map<UUID, Double> gamePlayerScores = new HashMap<>();
    private final Map<GameTeam, Double> gameTeamScores = new HashMap<>();

    private MySQLDataManager<ScoreService> db;

    private double multiplier = 1.0;

    public void earnPoints(UUID playerId, String reason, double points, int roundIndex) {
        ScoreEntry entry = new ScoreEntry(playerId, MinigameConstantsService.getInstance().getMinigameID(), roundIndex, reason, points, this.multiplier);
        this.entries.add(entry);
        this.putEntry(entry);

        // Player score
        this.roundPlayerScores.putIfAbsent(playerId, 0.0);
        this.gamePlayerScores.putIfAbsent(playerId, 0.0);
        this.roundPlayerScores.compute(playerId, (p, s) -> s + points * entry.multiplier());
        this.gamePlayerScores.compute(playerId, (p, s) -> s + points * entry.multiplier());

        // Team score
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(playerId);
        this.roundTeamScores.compute(team, (t, s) -> s + points * entry.multiplier());
        this.gameTeamScores.compute(team, (t, s) -> s + points * entry.multiplier());

        this.notifyObservers();
    }

    public void earnPoints(Player player, String reason, double points, int roundIndex) {
        this.earnPoints(player.getUniqueId(), reason, points, roundIndex);
    }

    public void earnPointsUUID(Collection<UUID> playerIds, String reason, double points, int roundIndex) {
        playerIds.forEach(playerId -> this.earnPoints(playerId, reason, points, roundIndex));
    }

    public void earnPoints(Collection<? extends Player> players, String reason, double points, int roundIndex) {
        players.forEach(player -> this.earnPoints(player, reason, points, roundIndex));
    }

    public Map<UUID, Double> getRoundPlayerScoreSummary() {
        return this.roundPlayerScores;
    }

    public List<ScoreEntry> getRoundEntries(Player player, int roundIndex) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(MinigameConstantsService.getInstance().getMinigameID()))
            .filter(entry -> entry.round() == roundIndex)
            .filter(entry -> Objects.equals(entry.playerId(), player.getUniqueId()))
            .toList();
    }

    public Map<UUID, Double> getRoundTeamMemberScoreSummary(GameTeam team, int roundIndex) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(MinigameConstantsService.getInstance().getMinigameID()))
            .filter(entry -> entry.round() == roundIndex)
            .filter(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()) == team)
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<GameTeam, Double> getRoundTeamScoreSummary() {
        return this.roundTeamScores;
    }

    public Map<UUID, Double> getGamePlayerScoreSummary() {
        return this.gamePlayerScores;
    }

    public List<ScoreEntry> getGameEntries(Player player) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(MinigameConstantsService.getInstance().getMinigameID()))
            .filter(entry -> Objects.equals(entry.playerId(), player.getUniqueId()))
            .toList();
    }

    public Map<UUID, Double> getGameTeamMemberScoreSummary(GameTeam team) {
        // TODO: probably not very efficient to be computing this so often, cache it
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(MinigameConstantsService.getInstance().getMinigameID()))
            .filter(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()) == team)
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<GameTeam, Double> getGameTeamScoreSummary() {
        return this.gameTeamScores;
    }

    public Map<GameTeam, Double> getEventTeamScoreSummary() {
        return this.entries.stream()
            .filter(e -> TeamService.getInstance().getTeamOfPlayer(e.playerId()) != null)
            .collect(Collectors.groupingBy(
                entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()),
                Collectors.summingDouble(ScoreEntry::getTotalPoints)
            ));
    }

    @SuppressWarnings("rawtypes")
    private final List<IObserver> listeners = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    @Override
    public void addListener(IObserver observer) {
        this.listeners.add(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void removeListener(IObserver observer) {
        this.listeners.remove(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<IObserver> getListeners() {
        return this.listeners;
    }

    public void resetRoundScores(Collection<GameTeam> teams) {
        this.roundPlayerScores.clear();

        this.roundTeamScores.clear();
        teams.forEach(team -> this.roundTeamScores.put(team, 0.0));
    }

    public void resetGameScores(Collection<GameTeam> teams) {
        this.gamePlayerScores.clear();

        this.gameTeamScores.clear();
        teams.forEach(team -> this.gameTeamScores.put(team, 0.0));
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        this.db = new MySQLDataManager<>(this, this::loadDB);
        return List.of(
            this.db
        );
    }

    private void putEntry(ScoreEntry entry) {
        this.db.addAction(conn -> {
            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO mm_score (playerId, minigame, round, reason, points, multiplier) VALUES (?, ?, ?, ?, ?, ?);"
            )) {
                statement.setString(1, entry.playerId().toString());
                statement.setString(2, entry.minigame());
                statement.setInt(3, entry.round());
                statement.setString(4, entry.reason());
                statement.setDouble(5, entry.points());
                statement.setDouble(6, entry.multiplier());
                statement.executeUpdate();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not load score data", e);
            }
        });
    }

    private void loadDB(Connection conn) {
        this.entries.clear();

        try (PreparedStatement statement = conn.prepareStatement(
            "SELECT * from mm_score;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ScoreEntry entry = new ScoreEntry(
                    UUID.fromString(resultSet.getString("playerId")),
                    resultSet.getString("minigame"),
                    resultSet.getInt("round"),
                    resultSet.getString("reason"),
                    resultSet.getDouble("points"),
                    resultSet.getDouble("multiplier")
                );
                this.entries.add(entry);
            }
        } catch (SQLException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not load score data", e);
        }

        this.notifyObservers();
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return this.multiplier;
    }
}
