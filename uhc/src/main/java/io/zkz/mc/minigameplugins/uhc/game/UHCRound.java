package io.zkz.mc.minigameplugins.uhc.game;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountupTimer;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.Round;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.state.PlayerState;
import io.zkz.mc.minigameplugins.uhc.overrides.RecipeOverrides;
import io.zkz.mc.minigameplugins.uhc.schematic.SchematicLoader;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TeamStatus;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TimeCycle;
import io.zkz.mc.minigameplugins.uhc.settings.enums.WeatherCycle;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmArgs;

public class UHCRound extends Round {
    private static final Vector LOBBY_TP_LOCATION = new Vector(0, 201, 0);
    private Location centerLocation;
    private double currentWorldBorderSpeed;
    private double currentWorldBorderTarget;
    private final Map<UUID, Location> assignedSpawnLocations = new HashMap<>();
    private AbstractTimer gameEventTimer;

    @Override
    public void onSetup() {
        // Determine center location
        centerLocation = getHighestBlock(Bukkit.getWorlds().get(0), 0, 0);

        // Setup lobby
        SchematicLoader.loadLobby();

        // Teleport players and set their spawns
        World world = Bukkit.getWorlds().get(0);
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);

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
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5);
        WorldSyncUtils.setWorldBorderSize(3000);
        WorldSyncUtils.setWorldBorderWarningTime(60);

        // Difficulty
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);

        // Set up default teams
        TeamService.getInstance().setupDefaultTeams();

        // Timer on scoreboard
        MinigameService.getInstance().changeTimer(null, mm("Game starts in:"));
    }

    @Override
    protected void onEnterPreRound() {
        // Setup world border
        int initialBorderSize = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_1.value();
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5);
        WorldSyncUtils.setWorldBorderSize(initialBorderSize);
        WorldSyncUtils.setWorldBorderWarningTime(180);
        WorldSyncUtils.setWorldBorderWarningDistance(10);
        WorldSyncUtils.setWorldBorderDamageBuffer(2);
        WorldSyncUtils.setWorldBorderDamageAmount(0.2);

        // Setup gamerules
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, SettingsManager.SETTING_TIME_CYCLE.value() == TimeCycle.NORMAL);
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, SettingsManager.SETTING_WEATHER_CYCLE.value() == WeatherCycle.NORMAL);
        WorldSyncUtils.setGameRule(GameRule.DO_INSOMNIA, SettingsManager.SETTING_SPAWN_PHANTOMS.value());

        // Assign spread player locations
        this.assignedSpawnLocations.clear();
        World world = Bukkit.getWorlds().get(0);
        List<UUID> competitors = new ArrayList<>(this.getAlivePlayers());
        int numCompetitors = competitors.size();
        double spreadRadius = initialBorderSize / 2.0 - 150;
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

                this.assignedSpawnLocations.put(competitors.get(i), loc.add(.5, 100, .5));
            }
        } else {
            List<GameTeam> teams = new ArrayList<>(this.getAliveTeams().keySet());
            int numTeams = teams.size();
            for (int i = 0; i < numTeams; i++) {
                AtomicReference<Double> thisRadius = new AtomicReference<>(spreadRadius);
                int j = i;
                teams.get(i).getAllMembers().forEach(playerId -> {
                    int x, z;
                    Location loc;
                    do {
                        x = (int) (thisRadius.get() * Math.cos(2 * Math.PI * j / numTeams));
                        z = (int) (thisRadius.get() * Math.sin(2 * Math.PI * j / numTeams));
                        loc = getHighestBlock(world, x, z);
                        thisRadius.updateAndGet(v -> v - 1);
                    } while (loc.getBlock().isLiquid());
                    this.assignedSpawnLocations.put(playerId, loc.add(.5, 100, .5));
                });
            }
        }

        // Setup & TP players
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);

        // Clear lobby
        SchematicLoader.clearLobby();
    }

    @Override
    public void onRoundStart() {
        // Effects
        this.getOnlineAlivePlayers().forEach(player -> {
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 300 * 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1, 1));
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(20);

            // TODO: give team-tracking compasses for proximity chat
        });

        // World stuff
        WorldSyncUtils.setDifficulty(Difficulty.HARD);
        WorldSyncUtils.setTime(0);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, true);

        // Start world border
        this.currentWorldBorderTarget = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value();
        this.currentWorldBorderSpeed = (SettingsManager.SETTING_WORLD_BORDER_DISTANCE_1.value() - SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value()) / 2d / (SettingsManager.SETTING_WORLD_BORDER_TIME_1.value() * 60);
        WorldSyncUtils.setWorldBorderSize(SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value(), SettingsManager.SETTING_WORLD_BORDER_TIME_1.value() * 60);
        Chat.sendMessage(ChatType.ACTIVE_INFO, mmArgs("The world border will now shrink to <legacy_aqua><0></legacy_aqua> blocks in diameter over <legacy_aqua><1></legacy_aqua> minutes (<legacy_yellow>about 1 block every <2> second(s)</legacy_yellow>).", (int) this.currentWorldBorderTarget, SettingsManager.SETTING_WORLD_BORDER_TIME_1.value(), String.format("%.1f", 1 / this.currentWorldBorderSpeed)));

        // Elapsed time timer
        MinigameService.getInstance().changeTimer(new GameCountupTimer(UHCService.getInstance().getPlugin(), 10), mm("Game time:"));
        this.gameEventTimer = new GameCountupTimer(UHCService.getInstance().getPlugin(), 5);
        if (SettingsManager.SETTING_PERMADAY_TIME.value() > 0) {
            this.gameEventTimer.scheduleEvent((long) (SettingsManager.SETTING_PERMADAY_TIME.value() - 5) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Permaday will be enabled in <legacy_aqua>5</legacy_aqua> minutes."));
            });
            this.gameEventTimer.scheduleEvent((SettingsManager.SETTING_PERMADAY_TIME.value() - 1) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Permaday will be enabled in <legacy_aqua>1</legacy_aqua> minute."));
            });
            this.gameEventTimer.scheduleEvent(SettingsManager.SETTING_PERMADAY_TIME.value() * 60000L, () -> {
                WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                WorldSyncUtils.setTime(6000);
            });
        }
        if (SettingsManager.SETTING_PEACEFUL_TIME.value() > 0) {
            this.gameEventTimer.scheduleEvent((long) (SettingsManager.SETTING_PEACEFUL_TIME.value() - 5) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Peaceful mode will be enabled in <legacy_aqua>5</legacy_aqua> minutes."));
            });
            this.gameEventTimer.scheduleEvent((SettingsManager.SETTING_PEACEFUL_TIME.value() - 1) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Peaceful mode will be enabled in <legacy_aqua>1</legacy_aqua> minute."));
            });
            this.gameEventTimer.scheduleEvent(SettingsManager.SETTING_PEACEFUL_TIME.value() * 60000L, () -> {
                WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);
            });
        }
        if (SettingsManager.SETTING_SHIELDLESS_TIME.value() > 0) {
            this.gameEventTimer.scheduleEvent((long) (SettingsManager.SETTING_SHIELDLESS_TIME.value() - 5) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Shields will be disabled in <legacy_aqua>5</legacy_aqua> minutes."));
            });
            this.gameEventTimer.scheduleEvent((SettingsManager.SETTING_SHIELDLESS_TIME.value() - 1) * 60000L, () -> {
                Chat.sendMessage(ChatType.GAME_INFO, mm("Shields will be disabled in <legacy_aqua>1</legacy_aqua> minute."));
            });
            this.gameEventTimer.scheduleEvent(SettingsManager.SETTING_SHIELDLESS_TIME.value() * 60000L, () -> {
                BukkitUtils.forEachPlayer(player -> player.getInventory().remove(Material.SHIELD));
                RecipeOverrides.removeShieldRecipe(UHCService.getInstance().getPlugin());
            });
        }
        this.gameEventTimer.start();

        // Sounds
        SoundUtils.playSound(Bukkit.getOnlinePlayers(), StandardSounds.GAME_OVER, 1, 1);
    }

    @Override
    public void onPreRoundTimerTick(long currentTimeMillis) {
        int time = (int) Math.ceil(currentTimeMillis / 1000.0);
        if (time > 0) {
            Chat.sendMessage(ChatType.ACTIVE_INFO, mm("Game starting in " + time + " seconds..."));
        }
    }

    @Override
    public void onPhase1End() {
        BukkitUtils.runNextTick(this::triggerPhase2Start);
    }

    @Override
    public void onPhase2Start() {
        if (SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value() == 0 || SettingsManager.SETTING_WORLD_BORDER_TIME_2.value() == 0) {
            BukkitUtils.runNextTick(this::triggerPhase2End);
            return;
        }

        // Start second world border
        this.currentWorldBorderTarget = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value();
        this.currentWorldBorderSpeed = (SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value() - SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value()) / 2d / (SettingsManager.SETTING_WORLD_BORDER_TIME_2.value() * 60);
        WorldSyncUtils.setWorldBorderSize(SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value(), SettingsManager.SETTING_WORLD_BORDER_TIME_2.value() * 60);
        Chat.sendMessage(ChatType.ACTIVE_INFO, mmArgs("The world border will now shrink to <legacy_aqua><0></legacy_aqua> blocks in diameter over <legacy_aqua><1></legacy_aqua> minutes (<legacy_yellow>about 1 block every <2> second(s)</legacy_yellow>).", (int) this.currentWorldBorderTarget, SettingsManager.SETTING_WORLD_BORDER_TIME_2.value(), String.format("%.1f", 1 / this.currentWorldBorderSpeed)));
    }

    @Override
    public void onPhase2End() {
        if (SettingsManager.SETTING_SUDDEN_DEATH_ENABLED.value()) {
            MinigameService.getInstance().changeTimer(new GameCountdownTimer(UHCService.getInstance().getPlugin(), 20, SettingsManager.SETTING_PARLAY_TIME.value(), TimeUnit.MINUTES, this::triggerPhase3Start), mm("Sudden death in:"));
            Chat.sendMessage(ChatType.ACTIVE_INFO, mmArgs("Sudden death will begin in <legacy_aqua><0></legacy_aqua> minutes.", SettingsManager.SETTING_PARLAY_TIME.value()));
        }
    }

    @Override
    public void onPhase3Start() {
        MinigameService.getInstance().changeTimer(null, null);
        // TODO: phase 3 start
        // TODO: start sudden death
        WorldSyncUtils.setWorldBorderSize(1, SettingsManager.SETTING_SUDDEN_DEATH_TIME.value() * 60);
        Chat.sendMessage(ChatType.ACTIVE_INFO, mmArgs("The world border is now a cube and will shrink to <legacy_aqua>1</legacy_aqua> block in diameter over <legacy_aqua><0></legacy_aqua> minutes.", SettingsManager.SETTING_SUDDEN_DEATH_TIME.value()));
    }

    @Override
    public void onEnterPostRound() {
        WorldSyncUtils.stopWorldBorder();
        Bukkit.getOnlinePlayers().forEach(player -> player.setAllowFlight(true));
    }

    @Override
    public void onPause() {
        // TODO: pause border
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 4, false, false));
        });
        this.gameEventTimer.pause();
    }

    @Override
    public void onUnpause() {
        // TODO: unpause border
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        });
        this.gameEventTimer.unpause();
    }

    public int getCurrentWorldborderSize() {
        return (int) WorldSyncUtils.getWorldBorderSize();
    }

    @Override
    protected void onPlayerRespawn(UUID playerId) {
        ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
    }

    @Override
    protected void onPlayerDeath(UUID playerId) {
        BukkitUtils.runNextTick(() -> this.setupPlayer(playerId));

        ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
        GameStats.getInstance().handlePlayerElimination(playerId);
        Chat.sendMessage(ChatType.ELIMINATION, mm("<0> has been eliminated! <1> players remain.", Bukkit.getPlayer(playerId).displayName(), Component.text(this.getAlivePlayers().size())));
        SoundUtils.playSound(Bukkit.getOnlinePlayers(), StandardSounds.PLAYER_ELIMINATION, 1, 1);

        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(playerId);
            if (team != null) {
                GameStats.getInstance().handleTeamElimination(team.id());
                Chat.sendMessage(ChatType.TEAM_ELIMINATION, mm("<0> has been eliminated! <1> players remain.", team.getDisplayName(), Component.text(this.getAliveTeams().size())));
            }

            // End game
            if (this.getAliveTeams().size() <= 1) {
                this.triggerRoundEnd();
            }
        } else {
            // End game
            if (this.getAlivePlayers().size() <= 1) {
                this.triggerRoundEnd();
            }
        }
    }

    @Override
    protected void onPlayerSetup(Player player, PlayerState playerState) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();

        switch (currentState) {
            case SERVER_STARTING:
            case LOADING:
            case SETUP:
            case WAITING_FOR_PLAYERS:
            case RULES:
            case WAITING_TO_BEGIN:
                // Clear inventory
                player.getInventory().clear();

                // Health & saturation
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setFireTicks(0);

                // Game mode
                player.setGameMode(GameMode.ADVENTURE);

                // Teleport
                player.teleport(LOBBY_TP_LOCATION.toLocation(player.getWorld()));
                break;
            case PAUSED:
                if (playerState == PlayerState.ALIVE) {
                    player.setGameMode(GameMode.ADVENTURE);
                }
                break;
            case PRE_ROUND:
                // Clear inventory
                player.getInventory().clear();

                // Health & saturation
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setFireTicks(0);

                // Game mode and teleport
                if (playerState == PlayerState.ALIVE) {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.teleport(this.assignedSpawnLocations.get(player.getUniqueId()));
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.teleport(LOBBY_TP_LOCATION.toLocation(player.getWorld()));
                }
                break;
            case IN_GAME:
            case MID_GAME:
            case IN_GAME_2:
            case MID_GAME_2:
            case IN_GAME_3:
                if (playerState == PlayerState.ALIVE) {
                    player.setGameMode(GameMode.SURVIVAL);
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.teleport(LOBBY_TP_LOCATION.toLocation(player.getWorld()));
                }
                break;
            case POST_ROUND:
            case POST_GAME:
                if (playerState == PlayerState.ALIVE) {
                    player.setGameMode(GameMode.ADVENTURE);
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                }
                break;
            default:
                break;
        }
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

    public double getCurrentWorldBorderSpeed() {
        return this.currentWorldBorderSpeed;
    }

    public double getCurrentWorldBorderTarget() {
        return this.currentWorldBorderTarget;
    }
}
