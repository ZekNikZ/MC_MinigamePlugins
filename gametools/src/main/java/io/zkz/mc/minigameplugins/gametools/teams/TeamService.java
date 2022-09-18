package io.zkz.mc.minigameplugins.gametools.teams;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLDataManager;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamCreateEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamRemoveEvent;
import io.zkz.mc.minigameplugins.gametools.util.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service(value = GameToolsPlugin.PLUGIN_NAME, priority = 8)
public class TeamService extends GameToolsService {
    private static final TeamService INSTANCE = new TeamService();

    public static TeamService getInstance() {
        return INSTANCE;
    }

    private final Map<String, GameTeam> teams = new HashMap<>();
    private final Map<UUID, String> players = new HashMap<>();
    private boolean friendlyFire = false;
    private Team.OptionStatus collisionRule = Team.OptionStatus.NEVER;

    private MySQLDataManager<TeamService> db;

    @Override
    protected void setup() {

    }

    /**
     * Checks if all of the specified players are on the same team. If an empty collection is passed, true is returned.
     *
     * @param players the players to query
     * @return whether all of the players are on the same team
     */
    public boolean allSameTeam(Collection<UUID> players) {
        if (players.isEmpty()) {
            return true;
        }

        boolean flag = false;
        GameTeam first = null;
        for (UUID playerId : players) {
            if (!flag) {
                first = this.getTeamOfPlayer(playerId);
                flag = true;
                continue;
            }

            if (this.getTeamOfPlayer(playerId) != first) {
                return false;
            }
        }

        return true;
    }

    public boolean areAllNonSpectatorsOnline() {
        return this.getTrackedPlayers().stream()
            .filter(p -> {
                GameTeam team = this.getTeamOfPlayer(p);
                return team != null && !team.isSpectator();
            })
            .allMatch(p -> Bukkit.getPlayer(p) != null);
    }

    public static class TeamCreationException extends RuntimeException {
        public TeamCreationException(String problem) {
            super("Could not create team: " + problem);
        }
    }

    public void createTeam(GameTeam team) throws TeamCreationException {
        this.createTeam(team, false);
    }

    void createTeam(GameTeam team, boolean suppressEvent) throws TeamCreationException {
        // Ensure that the team does not already exist
        if (this.teams.containsKey(team.getId())) {
            throw new TeamCreationException("team ID already exists");
        }

        // Save the team
        this.teams.put(team.getId(), team);

        // Call event
        if (!suppressEvent) {
            Bukkit.getServer().getPluginManager().callEvent(new TeamCreateEvent(team));
        }
    }

    public void setupDefaultTeams() {
        // Clear the existing teams
        this.clearTeams();

        // Add all the default teams
        DefaultTeams.addAll();

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamCreateEvent(this.teams.values()));
    }

    public void removeTeam(String id) {
        // If the team doesn't exist, this is a no-op
        if (!this.teams.containsKey(id)) {
            return;
        }

        // Remove the team
        GameTeam team = this.teams.remove(id);
        if (team == null) {
            return;
        }

        this.clearTeam(id);

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamRemoveEvent(team));
    }

    public GameTeam getTeam(String id) {
        return this.teams.get(id);
    }

    public Collection<GameTeam> getAllTeams() {
        return this.teams.values().stream().toList();
    }

    public Collection<GameTeam> getAllNonSpectatorTeams() {
        return this.teams.values().stream().filter(team -> !team.isSpectator()).toList();
    }

    /**
     * Remove all teams.
     */
    public void clearTeams() {
        // Clear players
        this.clearAllPlayersFromTeams();

        // Clear teams
        if (!this.teams.isEmpty()) {
            Bukkit.getServer().getPluginManager().callEvent(new TeamRemoveEvent(this.teams.values()));
        }
        this.teams.clear();
    }

    public void clearTeam(String teamId) {
        // Ensure the team exists
        GameTeam team = this.getTeam(teamId);
        if (team == null) {
            return;
        }

        // Remove the team members
        Collection<UUID> teamMembers = this.getTeamMembers(teamId);
        if (teamMembers.isEmpty()) {
            return;
        }
        teamMembers.forEach(this.players::remove);

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamChangeEvent(
            team,
            null,
            teamMembers
        ));
    }

    /**
     * Remove all players from teams.
     */
    public void clearAllPlayersFromTeams() {
        this.teams.keySet().forEach(this::clearTeam);
    }

    public static class TeamJoinException extends RuntimeException {
        public TeamJoinException(String problem) {
            super("Could not join team: " + problem);
        }
    }

    public void joinTeam(Player player, GameTeam team) throws TeamJoinException {
        this.joinTeam(player.getUniqueId(), team.getId());
    }

    public void joinTeam(Player player, GameTeam team, boolean suppressEvent) throws TeamJoinException {
        this.joinTeam(player.getUniqueId(), team.getId(), suppressEvent);
    }

    public void joinTeam(UUID playerId, String teamId) throws TeamJoinException {
        this.joinTeam(playerId, teamId, false);
    }

    void joinTeam(UUID playerId, String teamId, boolean suppressEvent) throws TeamJoinException {
        GameTeam newTeam = this.getTeam(teamId);
        if (newTeam == null) {
            throw new TeamJoinException("Team does not exist");
        }

        // Get old team
        GameTeam oldTeam = this.getTeamOfPlayer(playerId);

        // Join the team
        this.players.put(playerId, teamId);

        // Call event
        if (!suppressEvent) {
            Bukkit.getServer().getPluginManager().callEvent(new TeamChangeEvent(
                oldTeam,
                newTeam,
                playerId
            ));
        }
    }

    public void leaveTeam(Player player) {
        this.leaveTeam(player.getUniqueId());
    }

    public void leaveTeam(UUID playerId) {
        // Ensure the player is already on a team
        String currentTeam = this.players.get(playerId);
        if (currentTeam == null) {
            return;
        }

        // Leave the team
        this.players.remove(playerId);

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamChangeEvent(
            this.getTeam(currentTeam),
            null,
            playerId
        ));
    }

    public GameTeam getTeamOfPlayer(Player player) {
        return this.getTeamOfPlayer(player.getUniqueId());
    }

    @Nullable
    public GameTeam getTeamOfPlayer(UUID playerId) {
        String team = this.players.get(playerId);
        return team == null ? null : this.teams.get(team);
    }

    public Collection<UUID> getTeamMembers(String teamId) {
        return this.players.entrySet().stream()
            .filter((entry) -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }

    public Collection<UUID> getTeamMembers(GameTeam team) {
        return this.getTeamMembers(team.getId());
    }

    public Collection<Player> getOnlineTeamMembers(String teamId) {
        return this.players.entrySet().stream()
            .filter((entry) -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .toList();
    }

    public Collection<Player> getOnlineTeamMembers(GameTeam team) {
        return this.getOnlineTeamMembers(team.getId());
    }

    public Collection<UUID> getTrackedPlayers() {
        return this.players.keySet();
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        ScoreboardService.getInstance().setupGlobalTeams();
    }

    public boolean getFriendlyFire() {
        return this.friendlyFire;
    }

    public void setCollisionRule(Team.OptionStatus collisionRule) {
        this.collisionRule = collisionRule;
    }

    public Team.OptionStatus getCollisionRule() {
        return this.collisionRule;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    // lowest so that the player's team is updated before other services get access to their team information
    private void onPlayerJoin(PlayerJoinEvent event) {
        // reload team entry in database
        this.reloadPlayerTeamEntry(event.getPlayer().getUniqueId());

        GameTeam team = this.getTeamOfPlayer(event.getPlayer());
        if (team != null) {
            event.getPlayer().displayName(mm(team.getFormatTag() + "<0> <1>", team.getPrefix(), event.getPlayer().name()));
        }

        event.joinMessage(mm("<yellow><0> joined the game.", event.getPlayer().displayName()));
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(mm("<yellow><0> left the game.", event.getPlayer().displayName()));
    }

    @EventHandler
    private void onPlayerChat(AsyncChatEvent event) {
        event.message(mm("<0>: <1>", event.getPlayer().displayName(), event.originalMessage()));
    }

    @EventHandler
    private void onTeamCreate(TeamCreateEvent event) {
        // Log message
        GameToolsPlugin.logger().info("Team(s) created: " + PlainTextComponentSerializer.plainText().serialize(event.getTeams().stream().map(GameTeam::getName).collect(ComponentUtils.joining(mm(", ")))));

        // Update database
        this.db.addAction(c -> this.createTeamsInDB(c, event.getTeams()));
    }

    @EventHandler
    private void onTeamDelete(TeamRemoveEvent event) {
        // Log message
        GameToolsPlugin.logger().info("Team(s) deleted: " + PlainTextComponentSerializer.plainText().serialize(event.getTeams().stream().map(GameTeam::getName).collect(ComponentUtils.joining(mm(", ")))));

        // Update database
        this.db.addAction(c -> this.removeTeamsFromDB(c, event.getTeams()));
    }

    @EventHandler
    private void onPlayerTeamChange(TeamChangeEvent event) {
        Component oldTeam = event.getOldTeam() != null ? event.getOldTeam().getName() : mm("\\<none>");
        Component newTeam = event.getNewTeam() != null ? event.getNewTeam().getName() : mm("\\<none>");
        GameToolsPlugin.logger().info("Team changed: " + PlainTextComponentSerializer.plainText().serialize(oldTeam) + " -> " + PlainTextComponentSerializer.plainText().serialize(newTeam) + " for player(s) " + event.getPlayers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", ")));

        // Update database
        this.db.addAction(c -> this.changePlayerTeamInDB(c, event.getPlayers(), event.getNewTeam()));

        // Update display name
        event.getPlayers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> {
                if (event.getNewTeam() != null) {
                    player.displayName(mm(event.getNewTeam().getFormatTag() + "<0> <1>", event.getNewTeam().getPrefix(), player.name()));
                } else {
                    player.displayName(player.name());
                }
            });
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            this.db = new MySQLDataManager<>(this, this::loadDB)
        );
    }

    private void loadDB(Connection conn) {
        // Clear existing data
        this.teams.clear();
        this.players.clear();

        // Load teams
        try (PreparedStatement statement = conn.prepareStatement(
            "SELECT * from gt_teams;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                GameTeam team = new GameTeam(
                    resultSet.getString("teamId"),
                    GsonComponentSerializer.gson().deserialize(resultSet.getString("teamName")),
                    GsonComponentSerializer.gson().deserialize(resultSet.getString("teamPrefix"))
                );
                team.setScoreboardColor(NamedTextColor.namedColor(resultSet.getInt("teamScoreboardColor")));
                team.setFormatTag(resultSet.getString("teamFormatCode"));
                team.setColor(new Color(resultSet.getInt("teamColor")));
                team.setSpectator(resultSet.getBoolean("teamIsSpectator"));
                this.createTeam(team, true);
            }
        } catch (SQLException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not load team data", e);
        }

        // Load player teams
        try (PreparedStatement statement = conn.prepareStatement(
            "SELECT * from gt_player_teams;"
        )) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                this.joinTeam(
                    UUID.fromString(resultSet.getString("playerId")),
                    resultSet.getString("teamId"),
                    true
                );
            }
        } catch (SQLException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not load team data", e);
        }
    }

    private void reloadPlayerTeamEntry(UUID playerId) {
        this.db.addAction(conn -> {
            try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM gt_player_teams WHERE playerId = ?;"
            )) {
                statement.setString(1, playerId.toString());

                ResultSet resultSet = statement.executeQuery();
                boolean found = false;
                while (resultSet.next()) {
                    found = true;
                    this.joinTeam(
                        playerId,
                        resultSet.getString("teamId"),
                        true
                    );
                }

                if (!found) {
                    this.leaveTeam(playerId);
                }
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not load team data", e);
            }
        });
    }

    private void createTeamsInDB(Connection conn, List<GameTeam> teams) {
        try (PreparedStatement statement = conn.prepareStatement(
            "INSERT INTO gt_teams (teamId, teamName, teamPrefix, teamFormatCode, teamColor, teamScoreboardColor, teamIsSpectator) VALUES (?, ?, ?, ?, ?, ?, ?);"
        )) {
            conn.setAutoCommit(false);

            for (GameTeam team : teams) {
                statement.setString(1, team.getId());
                statement.setString(2, GsonComponentSerializer.gson().serialize(team.getName()));
                statement.setString(3, GsonComponentSerializer.gson().serialize(team.getPrefix()));
                statement.setString(4, String.valueOf(team.getFormatTag()));
                statement.setInt(5, team.getColor().getRGB());
                statement.setInt(6, team.getScoreboardColor().value());
                statement.setBoolean(7, team.isSpectator());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not create team(s)", e);
        }
    }

    private void removeTeamsFromDB(Connection conn, List<GameTeam> teams) {
        try (PreparedStatement statement = conn.prepareStatement(
            "DELETE FROM gt_teams WHERE teamId = ?;"
        )) {
            conn.setAutoCommit(false);

            for (GameTeam team : teams) {
                statement.setString(1, team.getId());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not create team(s)", e);
        }
    }

    private void changePlayerTeamInDB(Connection conn, List<UUID> players, GameTeam newTeam) {
        if (newTeam != null) {
            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO gt_player_teams (playerId, teamId) VALUES (?, ?) ON DUPLICATE KEY UPDATE teamId = ?;"
            )) {
                conn.setAutoCommit(false);

                for (UUID playerId : players) {
                    statement.setString(1, playerId.toString());
                    statement.setString(2, newTeam.getId());
                    statement.setString(3, newTeam.getId());
                    statement.addBatch();
                }
                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not create team(s)", e);
            }
        } else {
            try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM gt_player_teams WHERE playerId = ?;"
            )) {
                conn.setAutoCommit(false);

                for (UUID playerId : players) {
                    statement.setString(1, playerId.toString());
                    statement.addBatch();
                }
                statement.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                GameToolsPlugin.logger().log(Level.SEVERE, "Could not create team(s)", e);
            }
        }
    }
}
