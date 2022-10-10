package io.zkz.mc.uhc.game;

import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import io.zkz.mc.minigameplugins.gametools.util.ListUtils;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.uhc.GameState;
import io.zkz.mc.uhc.UHCPlugin;
import io.zkz.mc.uhc.lobby.SchematicLoader;
import io.zkz.mc.uhc.settings.SettingsManager;
import io.zkz.mc.uhc.settings.enums.TeamStatus;
import io.zkz.mc.uhc.settings.enums.TimeCycle;
import io.zkz.mc.uhc.settings.enums.WeatherCycle;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class GameManager extends PluginService<UHCPlugin> {
    private static final GameManager INSTANCE = new GameManager();

    public static GameManager getInstance() {
        return INSTANCE;
    }

    private static final Vector LOBBY_TP_LOCATION = new Vector(0, 201, 0);
    private static final Vector[] SUDDEN_DEATH_TP_LOCATIONS = new Vector[]{
        new Vector(-18, 249, -18),
        new Vector(18, 249, 18),
        new Vector(-18, 249, 18),
        new Vector(18, 249, -18),
        new Vector(0, 253, 17),
        new Vector(0, 253, -17),
        new Vector(17, 253, 0),
        new Vector(-17, 253, 0),
    };

    private final SharedReference<GameState> stateRef = new SharedReference<>(GameState.UNKNOWN);

    private AbstractTimer timer = null;
    private final SharedReference<Integer> worldborderSize = new SharedReference<>(0);
    private List<Integer> worldborderTransitions;
    private List<Integer> worldborderTimes;
    private double worldBorderSpeed = 0d;

    private List<UUID> competitors;
    private List<UUID> aliveCompetitors;
    private Map<String, List<UUID>> aliveTeams;

    private long suddenDeathTime;

    private final AtomicBoolean hasSetToDay = new AtomicBoolean(false);

    @Override
    protected void onEnable() {
        this.getPlugin().getServer().getPluginManager().registerEvents(new GameEventsListener(), this.getPlugin());
        UHCScoreboards.setup(this.getPlugin());
    }

    public List<UUID> getCompetitors() {
        return this.competitors;
    }

    public List<UUID> getAliveCompetitors() {
        return this.aliveCompetitors;
    }

    public void setState(GameState newState) {
        this.stateRef.setAndNotify(newState);
    }

    public GameState getState() {
        return this.stateRef.get();
    }

    public void enterSetupPhase() {
        this.setState(GameState.SETUP);

        // Clear SD
        SchematicLoader.clearSuddenDeath();

        // Scoreboard
        UHCScoreboards.setupLobbyScoreboard();
        TeamService.getInstance().setupDefaultTeams();

        // Teleport players and set their spawns
        World world = Bukkit.getWorlds().get(0);
        Bukkit.getOnlinePlayers().forEach(player -> this.setupLobbyPlayer(player, world));

        // Time and weather
        world.setTime(0);
        world.setStorm(false);

        // Spawnpoint
        world.setSpawnLocation(LOBBY_TP_LOCATION.getBlockX(), LOBBY_TP_LOCATION.getBlockY(), LOBBY_TP_LOCATION.getBlockZ());

        // Gamerules
        WorldSyncUtils.setGameRule(GameRule.NATURAL_REGENERATION, false);
        WorldSyncUtils.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        WorldSyncUtils.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        Bukkit.getOnlinePlayers().forEach(player -> player.setAllowFlight(false));

        // Worldborder
        WorldSyncUtils.setWorldBorderCenter(0, 0);
        WorldSyncUtils.setWorldBorderSize(3000);
        WorldSyncUtils.setWorldBorderWarningTime(60);

        // Difficulty
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);

        // Clear teams
        TeamService.getInstance().clearTeams();
        UHCScoreboards.updateCompetitors();

        // Create spectator team if it does not exist
        if (TeamService.getInstance().getTeam(DefaultTeams.SPECTATOR.id()) == null) {
            TeamService.getInstance().createTeam(DefaultTeams.SPECTATOR);
        }

        // Setup timer
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = new GameStopwatch(this.getPlugin(), 20);

        this.hasSetToDay.set(false);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        switch (this.getState()) {
            case SETUP:
                this.setupLobbyPlayer(event.getPlayer(), Bukkit.getWorlds().get(0));
                UHCScoreboards.updateCompetitors();
                break;
            default:
                break;
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        switch (this.getState()) {
            case SETUP:
                UHCScoreboards.updateCompetitors();
                break;
            default:
                break;
        }
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        switch (this.getState()) {
            case SETUP:
            case PRE_GAME:
            case PAUSED:
            case POST_GAME:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        switch (this.getState()) {
            case SETUP:
            case PRE_GAME:
            case PAUSED:
            case POST_GAME:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    private void setupLobbyPlayer(Player player, World world) {
        // Teleport
        Location loc = LOBBY_TP_LOCATION.toLocation(world);
        loc.setYaw(90);
        player.teleport(loc);

        // Spawnpoint
        player.setBedSpawnLocation(loc, true);

        // Game mode
        player.setGameMode(GameMode.ADVENTURE);

        // Clear inventory
        player.getInventory().clear();

        // Health & saturation
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }

    public void enterPregamePhase() {
        this.setState(GameState.PRE_GAME);

        // Setup worldborder
        int initialBorderSize = SettingsManager.getInstance().worldborderDistances().get(0).get();
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5);
        WorldSyncUtils.setWorldBorderSize(initialBorderSize);
        WorldSyncUtils.setWorldBorderWarningTime(180);
        WorldSyncUtils.setWorldBorderWarningDistance(10);
        WorldSyncUtils.setWorldBorderDamageBuffer(5);
        WorldSyncUtils.setWorldBorderDamageAmount(0.2);
        this.worldborderSize.setAndNotify(initialBorderSize);

        // Setup gamerules
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, SettingsManager.SETTING_TIME_CYCLE.value() == TimeCycle.NORMAL);
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, SettingsManager.SETTING_WEATHER_CYCLE.value() == WeatherCycle.NORMAL);

        // Spread players
        World world = Bukkit.getWorlds().get(0);
        List<Player> competitors = this.getInitialCompetitors();
        int numCompetitors = competitors.size();
        double spreadRadius = initialBorderSize / 2d - 150;
        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.INDIVIDUAL_GAME || !SettingsManager.SETTING_TEAMS_SPAWN_TOGETHER.value()) {
            for (int i = 0; i < numCompetitors; i++) {
                int x, z;
                Location loc;
                double thisRadius = spreadRadius;
                do {
                    x = (int) (thisRadius * Math.cos(2 * Math.PI * i / numCompetitors));
                    z = (int) (thisRadius * Math.sin(2 * Math.PI * i / numCompetitors));
                    loc = getHighestBlock(world, x, z);
                    thisRadius -= 1;
                } while (loc.getBlock().isLiquid());
                competitors.get(i).teleport(loc.add(.5, 100, .5));
            }

            this.competitors = competitors.stream().map(Player::getUniqueId).distinct().collect(Collectors.toList());
            this.aliveCompetitors = new ArrayList<>(this.competitors);
            this.aliveTeams = null;
        } else {
            List<GameTeam> teams = this.getInitialTeams();
            int numTeams = teams.size();
            for (int i = 0; i < numTeams; i++) {
                AtomicReference<Double> thisRadius = new AtomicReference<>(spreadRadius);
                int j = i;
                teams.get(0).getAllOnlineMembers().forEach(player -> {
                    int x, z;
                    Location loc;
                    do {
                        x = (int) (thisRadius.get() * Math.cos(2 * Math.PI * j / numTeams));
                        z = (int) (thisRadius.get() * Math.sin(2 * Math.PI * j / numTeams));
                        loc = getHighestBlock(world, x, z);
                        thisRadius.updateAndGet(v -> v - 1);
                    } while (loc.getBlock().isLiquid());
                    player.teleport(loc.add(.5, 100, .5));
                });
            }

            this.competitors = competitors.stream().map(Player::getUniqueId).distinct().collect(Collectors.toList());
            this.aliveCompetitors = new ArrayList<>(this.competitors);
            this.aliveTeams = teams.stream().map(GameTeam::id).collect(Collectors.toMap(t -> t, t -> TeamService.getInstance().getOnlineTeamMembers(t).stream().map(Player::getUniqueId).collect(Collectors.toCollection(ArrayList::new))));
        }

        // Clear lobby
        SchematicLoader.clearLobby();

        // Setup scoreboards
        StringValueEntry readyEntry = UHCScoreboards.setupPregameScoreboard(worldborderSize);
        worldborderSize.setAndNotify(initialBorderSize);

        // Wait for ready up
        ReadyUpService.getInstance().waitForReady(this.getInitialCompetitors().stream().map(Player::getUniqueId).collect(Collectors.toSet()), this::startGame, (uuid, session) -> {
            readyEntry.setValue(session.getReadyPlayerCount() + "/" + session.getTotalPlayerCount());
        });
    }

    private Location getHighestBlock(World world, int x, int z) {
        int i = 255;
        while (i > 0) {
            if (new Location(world, x, i, z).getBlock().getType() != Material.AIR)
                return new Location(world, x, i, z);
            i--;
        }
        return new Location(world, x, 0, z);
    }

    public void startGame() {
        // Effects
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().clear();
        });

        // World stuff
        WorldSyncUtils.setDifficulty(Difficulty.HARD);
        WorldSyncUtils.setTime(0);
        WorldSyncUtils.setWeatherClear();

        // Determine WB order
        this.worldborderTransitions = new ArrayList<>();
        this.worldborderTimes = new ArrayList<>();
        List<Integer> distances = SettingsManager.getInstance().worldborderDistances().stream().map(IntRangeSetting::get).collect(Collectors.toList());
        List<Integer> times = SettingsManager.getInstance().worldborderTimes().stream().map(IntRangeSetting::get).collect(Collectors.toList());
        if (times.get(1) == 0) {
            this.worldborderTransitions.addAll(ListUtils.of(distances.get(1)));
            this.worldborderTimes.addAll(ListUtils.of());
        } else if (times.get(2) == 0) {
            this.worldborderTransitions.addAll(ListUtils.of(distances.get(1), distances.get(2)));
            this.worldborderTimes.addAll(ListUtils.of(times.get(1)));
        } else {
            this.worldborderTransitions.addAll(ListUtils.of(distances.get(1), distances.get(2), 100));
            this.worldborderTimes.addAll(ListUtils.of(times.get(1), times.get(2)));
        }

        // Add WB listeners
        int warningHookId = this.timer.addHook(this::giveWorldBorderWarnings);
        this.timer.addTempHook((hookId) -> {
            int newWorldBorderSize = (int) WorldSyncUtils.getWorldBorderSize();

            if (newWorldBorderSize <= 600 && !hasSetToDay.get()) {
                SettingsManager.SETTING_TIME_CYCLE.value(TimeCycle.DAY_ONLY);
                this.hasSetToDay.set(true);
            }

            if (newWorldBorderSize < this.worldborderSize.get()) {
                this.worldborderSize.setAndNotify(newWorldBorderSize);

                if (!this.worldborderTransitions.isEmpty() && newWorldBorderSize == this.worldborderTransitions.get(0)) {
                    if (this.worldborderTransitions.size() > 1) {
                        this.handleWorldBorderTransition();
                    } else {
                        this.timer.removeHook(hookId);
                        this.timer.removeHook(warningHookId);
                        this.handleWorldBorderStopped();
                    }
                }
            }
        });
        this.timer.addHook(this::updateSpectatorInventories);

        new BukkitRunnable() {
            int count = 10;

            @Override
            public void run() {
                if (this.count > 0) {
                    ChatUtils.broadcast("Game starting in " + this.count + " seconds...");
                    Bukkit.getOnlinePlayers().forEach(player ->
                        ActionBarUtils.get().sendActionBarMessage(player, ChatColor.GOLD + "Game starting in " + this.count + " seconds..."));
                    --this.count;
                } else {
                    ChatUtils.broadcast("UHC has begun! Good luck!");
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        ActionBarUtils.get().sendActionBarMessage(player, ChatColor.GOLD + "UHC has begun! Good luck!");
                        player.setGameMode(GameMode.SURVIVAL);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300 * 20, 1));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1, 1));
                        player.setHealth(20);
                        player.setFoodLevel(20);
                        player.setSaturation(20);
                    });
                    setState(GameState.WB_CLOSING);

                    // Start timer
                    timer.start();
                    this.cancel();

                    // Start worldborder
                    worldBorderSpeed = (distances.get(0) - worldborderTransitions.get(0)) / 2d / (times.get(0) * 60);
                    WorldSyncUtils.setWorldBorderSize(worldborderTransitions.get(0), times.get(0) * 60);

                    // Scoreboard
                    UHCScoreboards.updateCompetitors();
                    UHCScoreboards.setupGameScoreboard();
                    ScoreboardService.getInstance().updateGlobalPlayerTeams();

                    // Stats
                    GameStats.getInstance().resetStats();
                }
            }
        }.runTaskTimer(this.getPlugin(), 0, 20);
    }

    @EventHandler
    private void onTeamChange(TeamChangeEvent event) {
        if (this.getState() == GameState.SETUP) {
            UHCScoreboards.updateCompetitors();
        }
    }

    public void transitionToWorldBorderStopped() {
        this.setState(GameState.WB_STOPPED);
        SettingsManager.SETTING_TIME_CYCLE.value(TimeCycle.DAY_ONLY);
        int parlayTime = SettingsManager.SETTING_PARLAY_TIME.value();
        ChatUtils.broadcast("Sudden death will begin in " + parlayTime + " minutes.");
        this.suddenDeathTime = this.timer.getAbsoluteSeconds() + parlayTime * 60L;
        this.timer.addTempHook(new Consumer<Integer>() {
            private int minAlert = 5;
            private int secAlert = 30;

            @Override
            public void accept(Integer taskId) {
                long time = timer.getAbsoluteSeconds();
                long adjustedTime = suddenDeathTime - time;

                if (!getState().isInGame()) {
                    ChatUtils.broadcast("Uh oh!");
                    timer.removeHook(taskId);
                    return;
                }

                if (time >= suddenDeathTime) {
                    setupSuddenDeath();
                    timer.removeHook(taskId);
                } else if (adjustedTime <= minAlert * 60L) {
                    ChatUtils.broadcast("Sudden death will begin in " + minAlert + " minute" + (minAlert == 1 ? "" : "s") + ".");
                    --minAlert;
                } else if (secAlert == 30 && adjustedTime <= secAlert) {
                    ChatUtils.broadcast("Sudden death will begin in 30 seconds.");
                    secAlert = 10;
                } else if (adjustedTime <= secAlert) {
                    ChatUtils.broadcast("Sudden death will begin in " + secAlert + " seconds.");
                    --secAlert;
                }
            }
        });
    }

    public void setupSuddenDeath() {
        // Load schematic
        SchematicLoader.loadSuddenDeath();

        // Teleport players
        int i = 0;
        World world = Bukkit.getWorlds().get(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(SUDDEN_DEATH_TP_LOCATIONS[i % SUDDEN_DEATH_TP_LOCATIONS.length].toLocation(world));
            ++i;
        }

        // Setup worldborder
        WorldSyncUtils.setWorldBorderSize(41);
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5);

        this.suddenDeathTime = this.timer.getAbsoluteSeconds();
        this.timer.addTempHook(new Consumer<Integer>() {
            private int minAlert = 5;
            private int secAlert = 30;

            @Override
            public void accept(Integer taskId) {
                long time = timer.getAbsoluteSeconds();
                long adjustedTime = 600 - (time - suddenDeathTime);

                if (!getState().isInGame()) {
                    timer.removeHook(taskId);
                    return;
                }

                if (adjustedTime <= 0) {
                    ChatUtils.broadcast("World border shrinking to 15 blocks wide...");
                    WorldSyncUtils.setWorldBorderSize(15, 52);
                    timer.removeHook(taskId);
                } else if (adjustedTime <= minAlert * 60L) {
                    ChatUtils.broadcast("World border will shrink in " + minAlert + " minutes.");
                    --minAlert;
                } else if (secAlert == 30 && adjustedTime <= secAlert) {
                    ChatUtils.broadcast("World border will shrink in 30 seconds.");
                    secAlert = 10;
                } else if (adjustedTime <= secAlert) {
                    ChatUtils.broadcast("World border will shrink in " + secAlert + " seconds.");
                    --secAlert;
                }
            }
        });

        // Start sudden death
        ChatUtils.broadcast("Sudden death has begun!");
    }

    public void handleWorldBorderTransition() {
        ChatUtils.broadcast("World border will now shrink to " + this.worldborderTransitions.get(1) + " over " + this.worldborderTimes.get(0) + " minutes.");
        this.worldBorderSpeed = (this.worldborderTransitions.get(0) - this.worldborderTransitions.get(1)) / 2d / (this.worldborderTimes.get(0) * 60);
        WorldSyncUtils.setWorldBorderSize(this.worldborderTransitions.get(1), this.worldborderTimes.get(0) * 60);
        this.worldborderTransitions.remove(0);
        this.worldborderTimes.remove(0);
    }

    public void handleWorldBorderStopped() {
        this.transitionToWorldBorderStopped();
    }

    public List<Player> getInitialCompetitors() {
        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            return this.getInitialTeams().stream()
                .flatMap(team -> team.getAllOnlineMembers().stream())
                .collect(Collectors.toList());
        } else {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        }
    }

    public List<GameTeam> getInitialTeams() {
        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            return Bukkit.getOnlinePlayers().stream()
                .map(TeamService.getInstance()::getTeamOfPlayer)
                .filter(Objects::nonNull)
                .filter(team -> !team.equals(DefaultTeams.SPECTATOR))
                .distinct()
                .collect(Collectors.toList());
        } else {
            return ListUtils.of();
        }
    }

    public AbstractTimer getTimer() {
        return this.timer;
    }

    public double getWorldBorderSpeed() {
        return this.worldBorderSpeed;
    }

    public double getWorldBorderSize() {
        return WorldSyncUtils.getWorldBorderSize();
    }

    public void giveWorldBorderWarnings() {
        if (this.getState() == GameState.WB_CLOSING) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                double worldBorderRadius = GameManager.getInstance().getWorldBorderSize() / 2;
                double worldBorderSpeed = GameManager.getInstance().getWorldBorderSpeed();
                boolean dark = GameManager.getInstance().getTimer().getAbsoluteSeconds() % 2 == 0;
                double x = player.getLocation().getX();
                double z = player.getLocation().getZ();
                double playerRadius = Math.max(Math.abs(x), Math.abs(z));

                if (playerRadius <= SettingsManager.getInstance().worldborderDistances().get(SettingsManager.getInstance().worldborderDistances().size() - 1).get()) {
                    return;
                }

                final int leeway = 20;
                if (worldBorderRadius - playerRadius <= worldBorderSpeed * (60 + leeway)) {
                    ActionBarUtils.get().sendActionBarMessage(player, (dark ? ChatColor.DARK_RED : ChatColor.RED) + "The world border will pass you in less than 1 minute!");
                    if (GameManager.getInstance().getTimer().getAbsoluteSeconds() % 2 == 0) {
                        playWarningSoundToPlayer(player);
                    }
                } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (180 + leeway)) {
                    ActionBarUtils.get().sendActionBarMessage(player, (dark ? ChatColor.DARK_RED : ChatColor.RED) + "The world border will pass you in less than 3 minutes!");
                    if (GameManager.getInstance().getTimer().getAbsoluteSeconds() % 3 == 0) {
                        playWarningSoundToPlayer(player);
                    }
                } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (300 + leeway)) {
                    ActionBarUtils.get().sendActionBarMessage(player, (dark ? ChatColor.DARK_RED : ChatColor.RED) + "The world border will pass you in less than 5 minutes!");
                    if (GameManager.getInstance().getTimer().getAbsoluteSeconds() % 4 == 0) {
                        playWarningSoundToPlayer(player);
                    }
                }
            });
        }
    }

    public void playWarningSoundToPlayer(Player player) {
        player.playSound(player.getLocation(), StandardSounds.ALERT_INFO, 0.5f, 0.5f);
    }

    public void handlePlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();

        if (this.aliveCompetitors.contains(uuid)) {
            this.aliveCompetitors.remove(uuid);
            GameStats.getInstance().handlePlayerElimination(uuid);
            GameTeam playerTeam = TeamService.getInstance().getTeamOfPlayer(player);
            if (playerTeam != null) {
                ChatUtils.broadcast("" + playerTeam.getFormatCode() + ChatColor.BOLD + playerTeam.getPrefix() + " " + playerTeam.getFormatCode() + player.getDisplayName() + ChatColor.RESET + " has been eliminated! " + this.aliveCompetitors.size() + " players remaining.");
            } else {
                ChatUtils.broadcast(player.getDisplayName() + " has been eliminated! " + this.aliveCompetitors.size() + " players remaining.");
            }
            UHCScoreboards.updateCompetitors();

            if (this.aliveTeams != null) {
                for (String teamId : this.aliveTeams.keySet()) {
                    GameTeam team = TeamService.getInstance().getTeam(teamId);
                    if (this.aliveTeams.get(teamId).contains(uuid)) {
                        this.aliveTeams.get(teamId).remove(uuid);

                        if (this.aliveTeams.get(teamId).isEmpty()) {
                            this.aliveTeams.remove(teamId);
                            GameStats.getInstance().handleTeamElimination(teamId);
                            ChatUtils.broadcast(team.getFormatCode() + team.getName() + ChatColor.RESET + " has been eliminated! " + this.aliveTeams.size() + " teams remaining.");
                            UHCScoreboards.updateCompetitors();
                        }
                        break;
                    }
                }
                if (this.aliveTeams.size() == 1) {
                    GameStats.getInstance().handleTeamElimination(this.aliveTeams.keySet().stream().findFirst().orElse(""));
                    handleGameOver();
                }
            } else {
                if (this.aliveCompetitors.size() == 1) {
                    GameStats.getInstance().handlePlayerElimination(this.aliveCompetitors.get(0));
                    handleGameOver();
                }
            }
        }
    }

    public void handleGameOver() {
        this.setState(GameState.POST_GAME);

        Bukkit.getOnlinePlayers().forEach(player -> player.setAllowFlight(true));

        WorldSyncUtils.stopWorldBorder();
        this.timer.stop();

        // Stats
        UHCScoreboards.setupPostgameScoreboard();

        // Effects
        String title = "" + ChatColor.GOLD + ChatColor.BOLD + "GAME OVER";
        String subtitle;
        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            GameTeam team = TeamService.getInstance().getTeam(this.aliveTeams.keySet().stream().findFirst().orElse(""));
            if (team != null) {
                subtitle = team.getFormatCode() + team.getName() + ChatColor.RESET + ChatColor.AQUA + " wins !";
            } else {
                subtitle = "";
            }
        } else {
            subtitle = ChatColor.AQUA + Bukkit.getOfflinePlayer(this.aliveCompetitors.get(0)).getName() + " wins !";
        }

        ChatUtils.broadcast("Game over! " + subtitle);

        new BukkitRunnable() {
            private int fireworks = 5;

            @Override
            public void run() {
                if (this.fireworks == 0) {
                    this.cancel();
                    return;
                }

                Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(title, subtitle));

                aliveCompetitors.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(p -> {
                    //Spawn the Firework, get the FireworkMeta.
                    Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();

                    //Our random generator
                    Random r = new Random();

                    //Get the type
                    FireworkEffect.Type type = FireworkEffect.Type.values()[r.nextInt(5)];

                    //Get our random colours
                    int r1i = r.nextInt(17) + 1;
                    int r2i = r.nextInt(17) + 1;
                    Color c1 = getColor(r1i);
                    Color c2 = getColor(r2i);

                    //Create our effect with this
                    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                    //Then apply the effect to the meta
                    fwm.addEffect(effect);

                    //Generate some random power and set it
                    int rp = r.nextInt(2) + 1;
                    fwm.setPower(rp);

                    //Then apply this to our rocket
                    fw.setFireworkMeta(fwm);
                });

                this.fireworks--;
            }

            private Color getColor(int i) {
                Color c = null;
                if (i == 1) {
                    c = Color.AQUA;
                }
                if (i == 2) {
                    c = Color.BLACK;
                }
                if (i == 3) {
                    c = Color.BLUE;
                }
                if (i == 4) {
                    c = Color.FUCHSIA;
                }
                if (i == 5) {
                    c = Color.GRAY;
                }
                if (i == 6) {
                    c = Color.GREEN;
                }
                if (i == 7) {
                    c = Color.LIME;
                }
                if (i == 8) {
                    c = Color.MAROON;
                }
                if (i == 9) {
                    c = Color.NAVY;
                }
                if (i == 10) {
                    c = Color.OLIVE;
                }
                if (i == 11) {
                    c = Color.ORANGE;
                }
                if (i == 12) {
                    c = Color.PURPLE;
                }
                if (i == 13) {
                    c = Color.RED;
                }
                if (i == 14) {
                    c = Color.SILVER;
                }
                if (i == 15) {
                    c = Color.TEAL;
                }
                if (i == 16) {
                    c = Color.WHITE;
                }
                if (i == 17) {
                    c = Color.YELLOW;
                }

                return c;
            }
        }.runTaskTimer(this.getPlugin(), 20, 20);
    }

    private void updateSpectatorInventories() {
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.getGameMode() == GameMode.SPECTATOR)
            .filter(player -> player.getSpectatorTarget() != null && player.getSpectatorTarget() instanceof Player)
            .forEach(player -> {
                player.getInventory().setArmorContents(((Player) player.getSpectatorTarget()).getInventory().getArmorContents());
                player.getInventory().setContents(((Player) player.getSpectatorTarget()).getInventory().getContents());
            });
    }

    public Map<String, List<UUID>> getAliveTeams() {
        if (this.aliveTeams == null) {
            return new HashMap<>();
        }
        return new HashMap<>(this.aliveTeams);
    }
}
