package io.zkz.mc.minigameplugins.tntrun;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TNTRunService extends PluginService<TNTRunPlugin> {
    private static final TNTRunService INSTANCE = new TNTRunService();

    public static TNTRunService getInstance() {
        return INSTANCE;
    }

    private final Set<UUID> alivePlayers = new HashSet<>();
    private final ObservableValue<Integer> alivePlayerCount = new ObservableValue<>(-1);

    private final List<TNTRunRound> rounds = new ArrayList<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();

        MinigameConstantsService.getInstance().setMinigameID("tntrun");
        MinigameConstantsService.getInstance().setMinigameName("TNT Run");

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(400);
        minigame.setPostRoundDelay(400);
        minigame.setPostGameDelay(600);

        // Player states
        BasicPlayerState adventureMode = new BasicPlayerState(GameMode.ADVENTURE);
        BasicPlayerState spectatorMode = new BasicPlayerState(GameMode.SPECTATOR);
        minigame.registerPlayerState(adventureMode,
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.IN_GAME,
            MinigameState.PAUSED
        );
        minigame.registerPlayerState(spectatorMode,
            MinigameState.POST_ROUND,
            MinigameState.POST_GAME
        );

        // Round setup handlers
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            // Setup alive players
            this.alivePlayers.clear();
            this.alivePlayers.addAll(minigame.getPlayers());
            this.alivePlayerCount.set(minigame.getPlayers().size());
        });

        // State tasks
        minigame.addTask(MinigameState.IN_GAME, FloorRemovalTask::new);

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round starts in 20 seconds", ChatColor.GOLD + "Find a good starting position!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round over!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_GAME, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Game over!", ChatColor.GOLD + "Check the chat for score information.", 10, 70, 20);
        });

        // In game scoreboard
        BiConsumer<MinigameState, GameScoreboard> scoreboardModifier = (state, scoreboard) -> {
            scoreboard.addSpace();
            scoreboard.addEntry(new ObservableValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Players Alive: " + ChatColor.RESET + "%s/" + MinigameService.getInstance().getPlayers().size(), this.alivePlayerCount));
        };
        minigame.registerScoreboard(MinigameState.PRE_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.IN_GAME, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.PAUSED, scoreboardModifier);
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("arenas.json"), this::saveData, this::loadData)
        );
    }

    private JSONObject saveData() {
        return new JSONObject(Map.of(
            "arenas", new TypedJSONArray<>(this.rounds.stream().map(round -> new JSONObject(Map.of(
                "pos1", JSONUtils.toJSON(round.getArenaMin()),
                "pos2", JSONUtils.toJSON(round.getArenaMax()),
                "spawn", JSONUtils.toJSON(round.getSpawnLocation()),
                "deathLevel", round.getDeathYLevel(),
                "mapName", round.getMapName()
            ))).toList())
        ));
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> object) {
        this.rounds.clear();
        this.rounds.addAll(object.getArray("arenas").stream().map(obj -> {
            TypedJSONObject<Object> round = new TypedJSONObject<Object>((JSONObject) obj);
            return new TNTRunRound(
                JSONUtils.readBlockVector(round.getList("pos1")),
                JSONUtils.readBlockVector(round.getList("pos2")),
                JSONUtils.readBlockVector(round.getList("spawn")),
                round.getInteger("deathLevel"),
                round.getString("mapName")
            );
        }).toList());
    }

    @Override
    protected void onEnable() {
        // Register rounds
        MinigameService.getInstance().registerRounds(this.rounds.toArray(TNTRunRound[]::new));
//        MinigameService.getInstance().randomizeRoundOrder();
    }

    public TNTRunRound getCurrentRound() {
        return (TNTRunRound) MinigameService.getInstance().getCurrentRound();
    }

    public void setupArena(TNTRunRound round) {
        World world = WorldEditService.getInstance().wrapWorld(Bukkit.getWorlds().get(0));
        round.resetArena();

        // WorldGuard region
        if (RegionService.getInstance().getRegion(world, "arena") != null) {
            RegionService.getInstance().removeProtectedRegion(world, "arena");
        }
        ProtectedRegion protectedRegion = RegionService.getInstance().createProtectedRegion(world, "arena", round.getArenaMin(), round.getArenaMax());

        protectedRegion.setFlags(Map.of(
            Flags.BUILD, StateFlag.State.DENY,
            Flags.EXIT, StateFlag.State.DENY
        ));

        // TP players
        Bukkit.getOnlinePlayers().forEach(player -> player.teleport(new Location(Bukkit.getWorlds().get(0), round.getSpawnLocation().getX(), round.getSpawnLocation().getY(), round.getSpawnLocation().getZ())));

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    }

    void setDead(Player player) {
        player.teleport(new Location(Bukkit.getWorlds().get(0), this.getCurrentRound().getSpawnLocation().getX(), this.getCurrentRound().getSpawnLocation().getY(), this.getCurrentRound().getSpawnLocation().getZ()));
        player.setGameMode(GameMode.SPECTATOR);
        SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);
        this.alivePlayers.remove(player.getUniqueId());

        // Assign points to alive players
        this.alivePlayers.forEach(p -> MinigameService.getInstance().earnPoints(p, "survival", Points.PLAYER_ELIMINATION));

        // Chat message
        Collection<? extends Player> noPointsPlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        Collection<? extends Player> pointsPlayers = this.getAlivePlayers();
        noPointsPlayers.removeAll(pointsPlayers);
        Chat.sendAlert(noPointsPlayers, ChatType.ELIMINATION, player.getDisplayName() + " fell and was eliminated.");
        SoundUtils.playSound(pointsPlayers, StandardSounds.GOAL_MET_MINOR, 1, 1);
        Chat.sendAlert(pointsPlayers, ChatType.ELIMINATION, player.getDisplayName() + " fell and was eliminated.", Points.PLAYER_ELIMINATION);

        // Team elimination message
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (this.alivePlayers.stream().noneMatch(playerId -> TeamService.getInstance().getTeamOfPlayer(playerId) == team)) {
            Chat.sendAlert(noPointsPlayers, ChatType.TEAM_ELIMINATION, team.getDisplayName() + " was eliminated.");
        }

        // Last player messages
        if (this.alivePlayers.size() <= 5 && this.alivePlayers.size() > 0) {
            Chat.sendAlert(ChatType.ALERT, this.alivePlayers.size() + " players remaining.");
        }

        // Assign points to top 3
        if (this.alivePlayers.size() == 2) {
            SoundUtils.playSound(pointsPlayers, StandardSounds.GOAL_MET_MINOR, 1, 1);
            MinigameService.getInstance().earnPoints(player, "third place", Points.THIRD_PLACE);
            Chat.sendAlert(player, ChatType.SUCCESS, "You were awarded bonus points for coming in third place!", Points.THIRD_PLACE);
        } else if (this.alivePlayers.size() == 1) {
            SoundUtils.playSound(pointsPlayers, StandardSounds.GOAL_MET_MINOR, 1, 1);
            MinigameService.getInstance().earnPoints(player, "second place", Points.SECOND_PLACE);
            Chat.sendAlert(player, ChatType.SUCCESS, "You were awarded bonus points for coming in second place!", Points.SECOND_PLACE);
        } else if (this.alivePlayers.size() == 0) {
            SoundUtils.playSound(pointsPlayers, StandardSounds.GOAL_MET_MINOR, 1, 1);
            MinigameService.getInstance().earnPoints(player, "first place", Points.FIRST_PLACE);
            Chat.sendAlert(player, ChatType.SUCCESS, "You were awarded bonus points for coming in first place!", Points.FIRST_PLACE);
        }

        this.updateGameState();
    }

    private void updateGameState() {
        // Update scoreboard
        this.alivePlayerCount.set(this.alivePlayers.size());

        // Check if round is over
        if (TeamService.getInstance().allSameTeam(this.alivePlayers)) {
            Collection<? extends Player> players = this.getAlivePlayers();

            // Award first place points
            ScoreService.getInstance().earnPointsUUID(this.alivePlayers, "first place", Points.FIRST_PLACE, MinigameService.getInstance().getCurrentRoundIndex());
            Chat.sendAlert(players, ChatType.SUCCESS, "You were awarded bonus points for coming in first place!", Points.FIRST_PLACE);

            // Win message
            SoundUtils.playSound(players, StandardSounds.GOAL_MET_MAJOR, 1, 1);
            if (players.size() > 1) {
                Chat.sendAlert(ChatType.GAME_INFO, "The winners of this round were " + players.stream().map(Player::getDisplayName).collect(Collectors.joining(" and ")));
            } else {
                Chat.sendAlert(ChatType.GAME_INFO, "The winner of this round was " + players.stream().map(Player::getDisplayName).collect(Collectors.joining(" and ")));
            }

            MinigameService.getInstance().endRound();
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();

        BlockVector3 vec = this.getCurrentRound().getSpawnLocation();
        event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), vec.getX(), vec.getY(), vec.getZ()));

        if (currentState == MinigameState.IN_GAME && this.alivePlayers.contains(event.getPlayer().getUniqueId())) {
            this.setDead(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();

        if (currentState == MinigameState.IN_GAME && this.alivePlayers.contains(event.getPlayer().getUniqueId())) {
            this.setDead(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (MinigameService.getInstance().getCurrentState() != MinigameState.IN_GAME || !this.alivePlayers.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getTo().getY() < this.getCurrentRound().getDeathYLevel()) {
            this.setDead(event.getPlayer());
        }
    }

    public Collection<? extends Player> getAlivePlayers() {
        return this.alivePlayers.stream().map(Bukkit::getPlayer).toList();
    }
}
