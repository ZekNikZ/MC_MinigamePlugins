package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.MySQLDataManager;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamCreateEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamRemoveEvent;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.ComponentUtils;
import io.zkz.mc.minigameplugins.gametools.util.GTColor;
import io.zkz.mc.minigameplugins.gametools.util.VanishingService;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service(priority = 8)
public class TeamService extends PluginService<GameToolsPlugin> {
    private static final TeamService INSTANCE = new TeamService();

    public static TeamService getInstance() {
        return INSTANCE;
    }

    private final Map<String, GameTeam> teams = new HashMap<>();
    private final Map<UUID, String> players = new HashMap<>();
    private boolean friendlyFire = false;
    private boolean glowing = true;
    private Team.OptionStatus collisionRule = Team.OptionStatus.NEVER;

    private MySQLDataManager<TeamService> db;

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

            if (!Objects.equals(this.getTeamOfPlayer(playerId), first)) {
                return false;
            }
        }

        return true;
    }

    public boolean areAllNonSpectatorsOnline() {
        return this.getTrackedPlayers().stream()
            .filter(p -> {
                GameTeam team = this.getTeamOfPlayer(p);
                return team != null && !team.spectator();
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
        if (this.teams.containsKey(team.id())) {
            throw new TeamCreationException("team ID already exists");
        }

        // Save the team
        this.teams.put(team.id(), team);

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
        return this.teams.values().stream().filter(team -> !team.spectator()).toList();
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
        this.joinTeam(player.getUniqueId(), team.id());
    }

    public void joinTeam(Player player, GameTeam team, boolean suppressEvent) throws TeamJoinException {
        this.joinTeam(player.getUniqueId(), team.id(), suppressEvent);
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
            .filter(entry -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }

    public Collection<UUID> getTeamMembers(GameTeam team) {
        return this.getTeamMembers(team.id());
    }

    public Collection<Player> getOnlineTeamMembers(String teamId) {
        return this.players.entrySet().stream()
            .filter(entry -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .toList();
    }

    public Collection<Player> getOnlineTeamMembers(GameTeam team) {
        return this.getOnlineTeamMembers(team.id());
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

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;

        BukkitUtils.forEachPlayer(player -> {
            VanishingService.getInstance().hidePlayer(player, "glow-refresh");
            VanishingService.getInstance().showPlayer(player, "glow-refresh");
        });
    }

    public boolean isGlowingEnabled() {
        return this.glowing;
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
            event.getPlayer().displayName(mm(team.formatTag() + "<0> <1>", team.prefix(), event.getPlayer().name()));
        } else {
            event.getPlayer().displayName(event.getPlayer().name());
        }

        event.joinMessage(mm("<yellow><0> joined the game.", event.getPlayer().displayName()));
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(mm("<yellow><0> left the game.", event.getPlayer().displayName()));
    }

    @EventHandler
    private void onTeamCreate(TeamCreateEvent event) {
        // Log message
        GameToolsPlugin.logger().info(() -> "Team(s) created: " + PlainTextComponentSerializer.plainText().serialize(event.getTeams().stream().map(GameTeam::name).collect(ComponentUtils.joining(mm(", ")))));

        // Update database
        this.db.addAction(c -> this.createTeamsInDB(c, event.getTeams()));
    }

    @EventHandler
    private void onTeamDelete(TeamRemoveEvent event) {
        // Log message
        GameToolsPlugin.logger().info(() -> "Team(s) deleted: " + PlainTextComponentSerializer.plainText().serialize(event.getTeams().stream().map(GameTeam::name).collect(ComponentUtils.joining(mm(", ")))));

        // Update database
        this.db.addAction(c -> this.removeTeamsFromDB(c, event.getTeams()));
    }

    @EventHandler
    private void onPlayerTeamChange(TeamChangeEvent event) {
        Component oldTeam = event.getOldTeam() != null ? event.getOldTeam().name() : mm("\\<none>");
        Component newTeam = event.getNewTeam() != null ? event.getNewTeam().name() : mm("\\<none>");
        GameToolsPlugin.logger().info(() -> "Team changed: " + PlainTextComponentSerializer.plainText().serialize(oldTeam) + " -> " + PlainTextComponentSerializer.plainText().serialize(newTeam) + " for player(s) " + event.getPlayers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", ")));

        // Update database
        this.db.addAction(c -> this.changePlayerTeamInDB(c, event.getPlayers(), event.getNewTeam()));

        // Update display name
        event.getPlayers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> {
                if (event.getNewTeam() != null) {
                    player.displayName(mm(event.getNewTeam().formatTag() + "<0> <1>", event.getNewTeam().prefix(), player.name()));
                } else {
                    player.displayName(player.name());
                }
            });
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        this.db = new MySQLDataManager<>(this, this::loadDB);
        return List.of(
            this.db
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
                    GsonComponentSerializer.gson().deserialize(resultSet.getString("teamPrefix")),
                    resultSet.getString("teamFormatCode"),
                    new GTColor(resultSet.getInt("teamColor")),
                    NamedTextColor.namedColor(resultSet.getInt("teamScoreboardColor")),
                    resultSet.getBoolean("teamIsSpectator")
                );
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
                statement.setString(1, team.id());
                statement.setString(2, GsonComponentSerializer.gson().serialize(team.name()));
                statement.setString(3, GsonComponentSerializer.gson().serialize(team.prefix()));
                statement.setString(4, String.valueOf(team.formatTag()));
                statement.setInt(5, team.color().rgb());
                statement.setInt(6, team.scoreboardColor().value());
                statement.setBoolean(7, team.spectator());
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
                statement.setString(1, team.id());
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
                    statement.setString(2, newTeam.id());
                    statement.setString(3, newTeam.id());
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
