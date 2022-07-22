package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamCreateEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.stream.Collectors;

// TODO: scoreboard service handles team service events instead of being explicitly called
// team create event (teams)
// team remove event (teams)
// team join event (players, oldTeam, newTeam) - teams can be nullable

public class TeamService extends GameToolsService {
    private static final TeamService INSTANCE = new TeamService();

    public static TeamService getInstance() {
        return INSTANCE;
    }

    private final Map<String, GameTeam> teams = new HashMap<>();
    private final Map<UUID, String> players = new HashMap<>();

    @Override
    protected void setup() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public static class TeamCreationException extends RuntimeException {
        public TeamCreationException(String problem) {
            super("Could not create team: " + problem);
        }
    }

    public void createTeam(GameTeam team) throws TeamCreationException {
        this.createTeam(team, true);
    }

    void createTeam(GameTeam team, boolean callEvent) throws TeamCreationException {
        // Ensure that the team does not already exist
        if (this.teams.containsKey(team.getId())) {
            throw new TeamCreationException("team ID already exists");
        }

        // Save the team
        this.teams.put(team.getId(), team);

        // Call event
        if (callEvent) {
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

        // this.players.values().remove(id); would only remove the first instance
        this.players.values().removeAll(Collections.singleton(id));

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamDeleteEvent(team));
    }

    public GameTeam getTeam(String id) {
        return this.teams.get(id);
    }

    public List<GameTeam> getAllTeams() {
        return this.teams.values().stream().toList();
    }

    /**
     * Remove all teams.
     */
    public void clearTeams() {
        // Clear players
        this.clearAllPlayersFromTeams();

        // Clear teams
        if (!this.teams.isEmpty()) {
            Bukkit.getServer().getPluginManager().callEvent(new TeamDeleteEvent(this.teams.values()));
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
        List<UUID> teamMembers = this.getTeamMembers(teamId);
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

    public void joinTeam(UUID playerId, String teamId) throws TeamJoinException {
        GameTeam newTeam = this.getTeam(teamId);
        if (newTeam == null) {
            throw new TeamJoinException("Team does not exist");
        }

        // Get old team
        GameTeam oldTeam = this.getTeamOfPlayer(playerId);

        // Join the team
        this.players.put(playerId, teamId);

        // Call event
        Bukkit.getServer().getPluginManager().callEvent(new TeamChangeEvent(
            oldTeam,
            newTeam,
            playerId
        ));
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

    public GameTeam getTeamOfPlayer(UUID playerId) {
        String team = this.players.get(playerId);
        return team == null ? null : this.teams.get(team);
    }

    public List<UUID> getTeamMembers(String teamId) {
        return this.players.entrySet().stream()
            .filter((entry) -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .toList();
    }

    public List<UUID> getTeamMembers(GameTeam team) {
        return this.getTeamMembers(team.getId());
    }

    public List<Player> getOnlineTeamMembers(String teamId) {
        return this.players.entrySet().stream()
            .filter((entry) -> teamId.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .toList();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        GameTeam team = this.getTeamOfPlayer(event.getPlayer());
        if (team != null) {
            event.setFormat("<" + team.getFormatCode() + ChatColor.BOLD + team.getPrefix() + ChatColor.RESET + team.getFormatCode() + " %1$s" + ChatColor.RESET + "> %2$s");
        }
    }

    @EventHandler
    public static void onTeamCreate(TeamCreateEvent event) {
        GameToolsPlugin.logger().info("Team(s) created: " + event.getTeams().stream().map(GameTeam::getName).collect(Collectors.joining(", ")));
    }

    @EventHandler
    public static void onTeamDelete(TeamDeleteEvent event) {
        GameToolsPlugin.logger().info("Team(s) deleted: " + event.getTeams().stream().map(GameTeam::getName).collect(Collectors.joining(", ")));
    }

    @EventHandler
    public static void onPlayerTeamChange(TeamChangeEvent event) {
        String oldTeam = event.getOldTeam() != null ? event.getOldTeam().getName() : "<none>";
        String newTeam = event.getNewTeam() != null ? event.getNewTeam().getName() : "<none>";
        GameToolsPlugin.logger().info("Team changed: " + oldTeam + " -> " + newTeam + " for player(s) " + event.getPlayers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(", ")));
    }
}
