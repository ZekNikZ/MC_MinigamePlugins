package io.zkz.mc.minigameplugins.tgttos;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.PlayerAliveDeadRound;
import io.zkz.mc.minigameplugins.gametools.score.ScoreEntry;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.tgttos.round.RoundType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public abstract class TGTTOSRound extends PlayerAliveDeadRound {
    private final String worldName;
    private final BlockVector3 glassWallMin;
    private final BlockVector3 glassWallMax;
    private final BlockVector3 spawnLocation;
    private final int deathYLevel;
    private final BlockVector3 endMin;
    private final BlockVector3 endMax;
    private final RoundType type;
    private final String regionName;
    private int playerPlacement = 0;
    private int teamPlacement = 0;

    public TGTTOSRound(RoundType type, TypedJSONObject<Object> json) {
        super(json.getString("mapName"), json.getString("mapBy"));
        this.type = type;
        this.worldName = json.getString("worldName");
        this.glassWallMin = JSONUtils.readBlockVector(json, "glassWallMin");
        this.glassWallMax = JSONUtils.readBlockVector(json, "glassWallMax");
        this.spawnLocation = JSONUtils.readBlockVector(json, "spawnLocation");
        this.deathYLevel = (int) json.getLong("deathYLevel");
        this.endMin = JSONUtils.readBlockVector(json, "endMin");
        this.endMax = JSONUtils.readBlockVector(json, "endMax");
        this.regionName = this.getMapName() + "_end";
    }

    @Override
    public void onSetup() {
        World world = WorldEditService.getInstance().wrapWorld(Bukkit.getWorld(this.worldName));

        // WorldGuard region
        if (RegionService.getInstance().getRegion(world, this.regionName) != null) {
            RegionService.getInstance().removeProtectedRegion(world, this.regionName);
        }
        RegionService.getInstance().createProtectedRegion(world, this.regionName, this.endMin, this.endMax);

        // TP players
        BukkitUtils.forEachPlayer(this::setupPlayerLocation);

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        WorldSyncUtils.setGameRule(GameRule.FALL_DAMAGE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_TILE_DROPS, false);
    }

    @Override
    public void onEnterPreRound() {
        super.onEnterPreRound();
        BukkitUtils.forEachPlayer(this::setupPlayer);
    }

    @Override
    public void onPreRoundTimerTick(long currentTimeMillis) {
        if (currentTimeMillis > 5000) {
            return;
        }

        Material mat;
        if (currentTimeMillis <= 1000) {
            mat = Material.GREEN_STAINED_GLASS;
        } else if (currentTimeMillis <= 2000) {
            mat = Material.LIME_STAINED_GLASS;
        } else if (currentTimeMillis <= 3000) {
            mat = Material.YELLOW_STAINED_GLASS;
        } else if (currentTimeMillis <= 4000) {
            mat = Material.ORANGE_STAINED_GLASS;
        } else {
            mat = Material.RED_STAINED_GLASS;
        }
        WorldEditService we = WorldEditService.getInstance();
        we.fillRegion(
                we.wrapWorld(Bukkit.getWorld(this.worldName)),
                this.createGlassWallRegion(),
                we.createPattern(mat)
        );
    }

    @Override
    public void onRoundStart() {
        // Remove barrier
        WorldEditService we = WorldEditService.getInstance();
        we.fillRegion(
                we.wrapWorld(Bukkit.getWorld(this.worldName)),
                this.createGlassWallRegion(),
                we.createPattern(Material.AIR)
        );

        // Setup timers
        MinigameService.getInstance().changeTimer(new GameCountdownTimer(TGTTOSService.getInstance().getPlugin(), 20, 120, TimeUnit.SECONDS, this::roundIsOver));
        MinigameService.getInstance().getTimer().addHook(new Runnable() {
            boolean warning30 = false;
            boolean warning20 = false;
            boolean warning10 = false;

            @Override
            public void run() {
                if (MinigameService.getInstance().getTimer() == null) {
                    return;
                }

                long currentTime = MinigameService.getInstance().getTimer().getCurrentTime(TimeUnit.MILLISECONDS);

                if (currentTime < 30000 && !warning30) {
                    warning30 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "30 seconds remain.");
                } else if (currentTime < 20000 && !warning20) {
                    warning20 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "20 seconds remain.");
                } else if (currentTime < 10000 && !warning10) {
                    warning10 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "10 seconds remain.");
                } else if (currentTime < 10000) {
                    SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);
                }
            }
        });
    }

    @Override
    public void onEnterPostRound() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
        BukkitUtils.forEachPlayer(player -> {
            double points = ScoreService.getInstance().getRoundEntries(player, MinigameService.getInstance().getCurrentRoundIndex()).stream().mapToDouble(ScoreEntry::points).sum();
            Chat.sendMessage(player, " ");
            Chat.sendAlertFormatted(player, ChatType.ACTIVE_INFO, "You earned " + ChatColor.GREEN + ChatColor.BOLD + "%.1f" + Chat.Constants.POINT_CHAR + " this round.", points);
        });
    }

    @Override
    protected void onPlayerSetup(Player player, PlayerState playerState) {
        this.setupPlayerLocation(player);
        player.setGameMode(switch (playerState) {
            case ALIVE -> GameMode.SURVIVAL;
            case DEAD, SPEC -> GameMode.SPECTATOR;
        });
        if (playerState == PlayerState.ALIVE) {
            player.getInventory().clear();
            this.setupPlayerInventory(player);
        } else {
            player.getInventory().clear();
        }
    }

    protected abstract void setupPlayerInventory(Player player);

    public BlockVector3 getSpawnLocation() {
        return this.spawnLocation;
    }

    public int getDeathYLevel() {
        return this.deathYLevel;
    }

    public RoundType getType() {
        return this.type;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public Region createGlassWallRegion() {
        return WorldEditService.getInstance().createCuboidRegion(this.glassWallMin, this.glassWallMax);
    }

    public boolean isPlayerInEndRegion(Player player) {
        BlockVector3 vec = WorldEditService.getInstance().wrapLocation(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(WorldEditService.getInstance().wrapWorld(player.getWorld()));
        return regions.getRegion(this.regionName).contains(vec);
    }

    public void setupPlayerLocation(Player player) {
        player.teleport(new Location(Bukkit.getWorld(this.worldName), this.spawnLocation.getX(), this.spawnLocation.getY(), this.spawnLocation.getZ()));
    }

    public void onPlayerFallOff(Player player) {
        this.setupPlayerLocation(player);
        this.setupPlayerInventory(player);

        Chat.sendAlert(ChatType.ELIMINATION, player.getDisplayName() + ChatColor.GRAY + Constants.randomDeathMessage());
        SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);
    }

    public void onPlayerFinishCourse(Player player) {
        this.setDead(player);
        player.setGameMode(GameMode.SPECTATOR);

        // Compute score and placement
        int points = Points.getPlayerPlacementPointValue(this.playerPlacement);
        String placementOrdinal = NumberUtils.ordinal(this.playerPlacement + 1);
        MinigameService.getInstance().earnPoints(player, "completion", points);

        // Chat message
        Chat.sendAlert(player, ChatType.SUCCESS, "You completed the course, finishing in " + ChatColor.AQUA + ChatColor.BOLD + placementOrdinal + ChatColor.GREEN + ChatColor.BOLD + " place!", points);
        Chat.sendAlert(BukkitUtils.allPlayersExcept(player), ChatType.ACTIVE_INFO, player.getDisplayName() + ChatColor.GRAY + " completed the course, finishing in " + ChatColor.AQUA + ChatColor.BOLD + placementOrdinal + ChatColor.GREEN + ChatColor.BOLD + " place!");
        SoundUtils.playSound(player, StandardSounds.GOAL_MET_MINOR, 1, 1);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);

        // Increment placement
        this.playerPlacement++;
        TGTTOSService.getInstance().updateFinishedPlayerCount();

        // Check if the whole team is now done
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (!this.isTeamAlive(team)) {
            // Compute score and placement
            int teamPoints = TeamService.getInstance().getTeamMembers(team).size() > 0 ? Points.getTeamPlacementPointValue(this.teamPlacement) / TeamService.getInstance().getTeamMembers(team).size() : 0;
            String teamPlacementOrdinal = NumberUtils.ordinal(this.teamPlacement + 1);
            MinigameService.getInstance().earnPoints(player, "team completion", points);

            // Chat message
            Chat.sendAlert(player, ChatType.SUCCESS, team.getDisplayName() + " was the " + ChatColor.AQUA + ChatColor.BOLD + teamPlacementOrdinal + ChatColor.GREEN + ChatColor.BOLD + " full team to finish!", teamPoints);
            Chat.sendAlert(BukkitUtils.allPlayersExcept(TeamService.getInstance().getOnlineTeamMembers(team)), ChatType.SUCCESS, team.getDisplayName() + " was the " + ChatColor.AQUA + ChatColor.BOLD + teamPlacementOrdinal + ChatColor.GREEN + ChatColor.BOLD + " full team to finish!");
            SoundUtils.playSound(TeamService.getInstance().getOnlineTeamMembers(team), StandardSounds.GOAL_MET_MAJOR, 1, 1);
            TeamService.getInstance().getOnlineTeamMembers(team).forEach(p -> {
                p.spawnParticle(Particle.TOTEM, p.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);
            });

            this.teamPlacement++;
        }

        // Check if round is over
        if (this.getAlivePlayers().isEmpty()) {
            this.roundIsOver();
        }
    }

    private void roundIsOver() {
        Chat.sendAlert(this.getOnlineAlivePlayers(), ChatType.WARNING, "You did not finish the course, so you have earned 0 points this round.");

        this.triggerRoundEnd();
    }

    @Override
    public @NotNull String getMapName() {
        return super.getMapName();
    }
}
