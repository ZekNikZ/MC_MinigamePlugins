package io.zkz.mc.uhc.game;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.Round;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.state.PlayerState;
import io.zkz.mc.uhc.schematic.SchematicLoader;
import io.zkz.mc.uhc.settings.SettingsManager;
import io.zkz.mc.uhc.settings.enums.TeamStatus;
import io.zkz.mc.uhc.settings.enums.TimeCycle;
import io.zkz.mc.uhc.settings.enums.WeatherCycle;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class UHCRound extends Round {
    // TODO: Sudden death will begin in X minutes/seconds
    // TODO: World border will now shrink to X blocks wide over Y minutes (Z blocks/sec)
    // TODO: Sudden death has begun! The world border will shrink to a cube over the next X minutes.
    // TODO: WB warnings

    private static final Vector LOBBY_TP_LOCATION = new Vector(0, 201, 0);
    private Location centerLocation;
    private double currentWorldBorderSpeed;
    private double currentWorldBorderTarget;

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

        // Clear teams
        TeamService.getInstance().clearTeams();

        // Create spectator team if it does not exist
        if (TeamService.getInstance().getTeam(DefaultTeams.SPECTATOR.id()) == null) {
            TeamService.getInstance().createTeam(DefaultTeams.SPECTATOR);
        }
    }

    @Override
    protected void onEnterPreRound() {
        // TODO: spread players
        // TODO: clear lobby

        // Setup world border
        int initialBorderSize = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_1.value();
        WorldSyncUtils.setWorldBorderCenter(0.5, 0.5);
        WorldSyncUtils.setWorldBorderSize(initialBorderSize);
        WorldSyncUtils.setWorldBorderWarningTime(180);
        WorldSyncUtils.setWorldBorderWarningDistance(10);
        WorldSyncUtils.setWorldBorderDamageBuffer(5);
        WorldSyncUtils.setWorldBorderDamageAmount(0.2);

        // Setup gamerules
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, SettingsManager.SETTING_TIME_CYCLE.value() == TimeCycle.NORMAL);
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, SettingsManager.SETTING_WEATHER_CYCLE.value() == WeatherCycle.NORMAL);

        // TODO: Assign spread player locations
//        World world = Bukkit.getWorlds().get(0);
//        List<Player> competitors = this.getInitialCompetitors();
//        int numCompetitors = competitors.size();
//        double spreadRadius = initialBorderSize / 2d - 150;
//        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.INDIVIDUAL_GAME || !SettingsManager.SETTING_TEAMS_SPAWN_TOGETHER.value()) {
//            for (int i = 0; i < numCompetitors; i++) {
//                int x, z;
//                Location loc;
//                double thisRadius = spreadRadius;
//                do {
//                    x = (int) (thisRadius * Math.cos(2 * Math.PI * i / numCompetitors));
//                    z = (int) (thisRadius * Math.sin(2 * Math.PI * i / numCompetitors));
//                    loc = getHighestBlock(world, x, z);
//                    thisRadius -= 1;
//                } while (loc.getBlock().isLiquid());
//                competitors.get(i).teleport(loc.add(.5, 100, .5));
//            }
//
//            this.competitors = competitors.stream().map(Player::getUniqueId).distinct().collect(Collectors.toList());
//            this.aliveCompetitors = new ArrayList<>(this.competitors);
//            this.aliveTeams = null;
//        } else {
//            List<GameTeam> teams = this.getInitialTeams();
//            int numTeams = teams.size();
//            for (int i = 0; i < numTeams; i++) {
//                AtomicReference<Double> thisRadius = new AtomicReference<>(spreadRadius);
//                int j = i;
//                teams.get(0).getAllOnlineMembers().forEach(player -> {
//                    int x, z;
//                    Location loc;
//                    do {
//                        x = (int) (thisRadius.get() * Math.cos(2 * Math.PI * j / numTeams));
//                        z = (int) (thisRadius.get() * Math.sin(2 * Math.PI * j / numTeams));
//                        loc = getHighestBlock(world, x, z);
//                        thisRadius.updateAndGet(v -> v - 1);
//                    } while (loc.getBlock().isLiquid());
//                    player.teleport(loc.add(.5, 100, .5));
//                });
//            }
//
//            this.competitors = competitors.stream().map(Player::getUniqueId).distinct().collect(Collectors.toList());
//            this.aliveCompetitors = new ArrayList<>(this.competitors);
//            this.aliveTeams = teams.stream().map(GameTeam::id).collect(Collectors.toMap(t -> t, t -> TeamService.getInstance().getOnlineTeamMembers(t).stream().map(Player::getUniqueId).collect(Collectors.toCollection(ArrayList::new))));
//        }

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

            // TODO: give team-tracking compasses
        });

        // World stuff
        WorldSyncUtils.setDifficulty(Difficulty.HARD);
        WorldSyncUtils.setTime(0);
        WorldSyncUtils.setWeatherClear();

        // Start world border
        this.currentWorldBorderTarget = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value();
        this.currentWorldBorderSpeed = (SettingsManager.SETTING_WORLD_BORDER_DISTANCE_1.value() - SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value()) / 2d / (SettingsManager.SETTING_WORLD_BORDER_TIME_1.value() * 60);
        WorldSyncUtils.setWorldBorderSize(SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value(), SettingsManager.SETTING_WORLD_BORDER_TIME_1.value());
    }

    @Override
    public void onPreRoundTimerTick(long currentTimeMillis) {
        Chat.sendMessage(Bukkit.getServer(), ChatType.ACTIVE_INFO, mm("Game starting in " + currentTimeMillis / 1000 + " seconds..."));
        Bukkit.getServer().sendMessage(mm("<legacy_gold>Game starting in " + currentTimeMillis / 1000 + " seconds..."));
    }

    @Override
    public void onPhase1End() {
        // TODO: phase 1 end
        // TODO: start phase 2 / sudden death timer (mid_game_2)
    }

    @Override
    public void onPhase2Start() {
        // Start second world border
        this.currentWorldBorderTarget = SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value();
        this.currentWorldBorderSpeed = (SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value() - SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value()) / 2d / (SettingsManager.SETTING_WORLD_BORDER_TIME_2.value() * 60);
        WorldSyncUtils.setWorldBorderSize(SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value(), SettingsManager.SETTING_WORLD_BORDER_TIME_2.value());
    }

    @Override
    public void onPhase2End() {
        // TODO: phase 2 end
        // TODO: start sudden death timer (mid_game_2)
    }

    @Override
    public void onPhase3Start() {
        // TODO: phase 3 start
        // TODO: start sudden death
    }

    @Override
    public void onEnterPostRound() {
        WorldSyncUtils.stopWorldBorder();
        Bukkit.getOnlinePlayers().forEach(player -> player.setAllowFlight(true));
    }

    @Override
    public void onPause() {
        // TODO: pause
        // TODO: pause border
    }

    @Override
    public void onUnpause() {
        // TODO: unpause
        // TODO: unpause border
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
        ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
        GameStats.getInstance().handlePlayerElimination(playerId);
        Chat.sendMessage(Bukkit.getServer(), ChatType.ELIMINATION, mm("<0> has been eliminated! <1> players remain.", Bukkit.getPlayer(playerId).displayName(), Component.text(this.getAlivePlayers().size())));

        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(playerId);
            if (team != null) {
                GameStats.getInstance().handleTeamElimination(team.id());
                Chat.sendMessage(Bukkit.getServer(), ChatType.TEAM_ELIMINATION, mm("<0> has been eliminated! <1> players remain.", team.getDisplayName(), Component.text(this.getAliveTeams().size())));
            }

            // End game
            if (this.getAliveTeams().size() == 1) {
                this.triggerRoundEnd();
            }
        } else {
            // End game
            if (this.getAlivePlayers().size() == 1) {
                this.triggerRoundEnd();
            }
        }

    }

    @Override
    protected void onPlayerSetup(Player player, PlayerState playerState) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // Clear inventory
        player.getInventory().clear();

        // Health & saturation
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);

        // Pre-game
        if (playerState == PlayerState.SPEC || team == null || team.spectator() || List.of(MinigameState.SETUP, MinigameState.WAITING_FOR_PLAYERS, MinigameState.RULES, MinigameState.WAITING_TO_BEGIN).contains(currentState)) {
            var loc = LOBBY_TP_LOCATION.toLocation(player.getWorld());
            player.teleport(loc);
            player.setBedSpawnLocation(loc, true);
            return;
        }

        // Pre-round
        if (currentState == MinigameState.PRE_ROUND) {
//            player.teleport(null);
            return;
        }

        // In-game
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
