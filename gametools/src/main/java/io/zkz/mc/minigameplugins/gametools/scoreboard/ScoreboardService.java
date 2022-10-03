package io.zkz.mc.minigameplugins.gametools.scoreboard;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamCreateEvent;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamRemoveEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

// TODO: reduce how often vanilla scoreboard team and team entries are updated
@Service(priority = 7)
public class ScoreboardService extends PluginService<GameToolsPlugin> {
    private static final ScoreboardService INSTANCE = new ScoreboardService();

    public static ScoreboardService getInstance() {
        return INSTANCE;
    }

    private GameScoreboard globalScoreboard = null;
    private final Map<String, GameScoreboard> teamScoreboards = new HashMap<>();
    private final Map<UUID, GameScoreboard> playerScoreboards = new HashMap<>();

    public void setGlobalScoreboard(GameScoreboard scoreboard) {
        this.setGlobalScoreboard(scoreboard, true);
    }

    @Override
    protected void onEnable() {
        this.setupGlobalTeams();
    }

    public void setGlobalScoreboard(GameScoreboard scoreboard, boolean cleanup) {
        if (cleanup && this.globalScoreboard != null && teamScoreboards.values().stream().noneMatch(s -> s.equals(this.globalScoreboard)) && playerScoreboards.values().stream().noneMatch(s -> s.equals(this.globalScoreboard))) {
            this.globalScoreboard.cleanup();
        }

        if (scoreboard != null) {
            this.setupGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
            this.updateGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
        }
        this.globalScoreboard = scoreboard;

        Bukkit.getOnlinePlayers().forEach(this::updatePlayerScoreboard);
    }

    public void setTeamScoreboard(String teamId, GameScoreboard scoreboard) {
        GameScoreboard current = this.teamScoreboards.get(teamId);
        if (current != null && !Objects.equals(this.globalScoreboard, current) && teamScoreboards.values().stream().noneMatch(s -> Objects.equals(s, current)) && playerScoreboards.values().stream().noneMatch(s -> Objects.equals(s, current))) {
            current.cleanup();
        }

        if (scoreboard != null) {
            this.setupGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
            this.updateGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
            this.teamScoreboards.put(teamId, scoreboard);
        } else {
            this.teamScoreboards.remove(teamId);
        }

        TeamService.getInstance().getOnlineTeamMembers(teamId).forEach(this::updatePlayerScoreboard);
    }

    public void setPlayerScoreboard(UUID uuid, GameScoreboard scoreboard) {
        GameScoreboard current = this.playerScoreboards.get(uuid);
        if (current != null && !this.globalScoreboard.equals(current) && teamScoreboards.values().stream().noneMatch(s -> s.equals(current)) && playerScoreboards.values().stream().noneMatch(s -> s.equals(current))) {
            current.cleanup();
        }

        if (scoreboard != null) {
            this.setupGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
            this.updateGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
            this.playerScoreboards.put(uuid, scoreboard);
        } else {
            this.playerScoreboards.remove(uuid);
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            this.updatePlayerScoreboard(player);
        }
    }

    public Collection<GameScoreboard> getAllScoreboards() {
        Set<GameScoreboard> scoreboards = new HashSet<>();
        if (this.globalScoreboard != null) {
            scoreboards.add(this.globalScoreboard);
        }
        this.teamScoreboards.values().forEach(s -> {
            if (s != null) {
                scoreboards.add(s);
            }
        });
        this.playerScoreboards.values().forEach(s -> {
            if (s != null) {
                scoreboards.add(s);
            }
        });
        return scoreboards;
    }

    public GameScoreboard createNewScoreboard(Component title) {
        GameScoreboard scoreboard = new GameScoreboard(title);
        this.setupGlobalTeamsOnScoreboard(scoreboard.getScoreboard());
        return scoreboard;
    }

    private void updatePlayerScoreboard(Player player) {
        // Choose the scoreboard
        GameScoreboard playerScoreboard = playerScoreboards.get(player.getUniqueId());
        if (playerScoreboard == null) {
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
            if (team != null) {
                playerScoreboard = teamScoreboards.get(team.id());
            }
            if (playerScoreboard == null) {
                playerScoreboard = globalScoreboard;
            }
        }

        // Apply the scoreboard
        if (playerScoreboard != null) {
            player.setScoreboard(playerScoreboard.getScoreboard());
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void setupGlobalTeams() {
        this.setupGlobalTeamsOnScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        this.getAllScoreboards().forEach(scoreboard -> this.setupGlobalTeamsOnScoreboard(scoreboard.getScoreboard()));
        this.updateGlobalPlayerTeams();
    }

    private void setupGlobalTeamsOnScoreboard(Scoreboard scoreboard) {
        TeamService.getInstance().getAllTeams().forEach(gameTeam -> {
            Team oldTeam = scoreboard.getTeam(gameTeam.id());
            if (oldTeam != null) {
                oldTeam.unregister();
            }
            Team team = scoreboard.registerNewTeam(gameTeam.id());
            team.prefix(mm(gameTeam.formatTag() + "<0> ", gameTeam.prefix()));
            team.color(gameTeam.scoreboardColor());
            team.suffix(mm(""));
            team.setCanSeeFriendlyInvisibles(true);
            team.setAllowFriendlyFire(TeamService.getInstance().getFriendlyFire());
            team.setOption(Team.Option.COLLISION_RULE, TeamService.getInstance().getCollisionRule());
        });
    }

    public void updateGlobalPlayerTeams() {
        this.updateGlobalTeamsOnScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        this.getAllScoreboards().forEach(scoreboard -> this.updateGlobalTeamsOnScoreboard(scoreboard.getScoreboard()));
    }

    private void updateGlobalTeamsOnScoreboard(Scoreboard scoreboard) {
        TeamService.getInstance().getAllTeams().forEach(gameTeam -> {
            Team team = scoreboard.getTeam(gameTeam.id());
            if (team != null) {
                team.getEntries().forEach(team::removeEntry);
                gameTeam.getAllMembers().forEach(uuid -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    if (offlinePlayer.getName() == null) {
                        return;
                    }
                    team.addEntry(offlinePlayer.getName());
                });
            }
        });

        Bukkit.getOnlinePlayers().forEach(this::updatePlayerScoreboard);
    }

    public void resetAllScoreboards() {
        this.getAllScoreboards().forEach(GameScoreboard::cleanup);
        this.globalScoreboard = null;
        this.teamScoreboards.clear();
        this.playerScoreboards.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.updatePlayerScoreboard(event.getPlayer());
        this.setupGlobalTeams();
    }

    @EventHandler
    private void onTeamCreate(TeamCreateEvent event) {
        // Setup team colors on scoreboards
        this.setupGlobalTeams();
    }

    @EventHandler
    private void onTeamRemove(TeamRemoveEvent event) {
        // Remove obsolete team scoreboards
        event.getTeams().stream()
            .map(GameTeam::id)
            .forEach(teamId -> this.setTeamScoreboard(teamId, null));

        // Setup team colors on scoreboards
        this.setupGlobalTeams();
    }

    @EventHandler
    private void onTeamChange(TeamChangeEvent event) {
        // Potentially update displayed scoreboard
        event.getPlayers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(this::updatePlayerScoreboard);

        // Update player colors on scoreboards
        this.setupGlobalTeams();
    }
}
