package io.zkz.mc.minigameplugins.survivalgames;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountupTimer;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.JSONUtils;
import io.zkz.mc.minigameplugins.gametools.util.PlayerUtils;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.zkz.mc.minigameplugins.survivalgames.SGService.adjustLocation;
import static io.zkz.mc.minigameplugins.survivalgames.SGService.toLocation;

public class SGRound extends Round {
    private final String templateWorldName;
    private final List<BlockVector3> spawnLocations;
    private final BlockVector3 cornLocation;
    private final long cornWorldborderSize;
    private final long mapWorldborderSize;
    private final List<SGChest> chests;

    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Map<UUID, Location> logOutLocations = new HashMap<>();
    private final Map<UUID, Location> assignedSpawnLocations = new HashMap<>();
    private final Map<String, List<String>> kills = new HashMap<>();

    private AbstractTimer eventTimer;
    private boolean inSuddenDeath = false;

    @SuppressWarnings("unchecked")
    public SGRound(TypedJSONObject<Object> json) {
        super(json.getString("name"));
        this.templateWorldName = json.getString("folder");
        this.spawnLocations = new ArrayList<BlockVector3>(json.getArray("spawnLocations").stream().map(obj -> JSONUtils.readBlockVector((List<Long>) obj)).toList());
        this.cornLocation = JSONUtils.readBlockVector(json.getList("cornLocation", Long.class));
        this.cornWorldborderSize = json.getLong("cornWorldborderSize");
        this.mapWorldborderSize = json.getLong("mapWorldborderSize");
        this.chests = json.getArray("chests").stream().map(obj -> {
            TypedJSONObject<Object> chest = new TypedJSONObject<>(((JSONObject) obj));
            return new SGChest(
                JSONUtils.readBlockVector(chest.getList("pos", Long.class)),
                chest.getString("lootTable")
            );
        }).toList();
    }

    @Override
    public void onSetup() {
        // Create and set up actual world
        World world = Bukkit.getWorld(this.templateWorldName);
        world.setDifficulty(Difficulty.NORMAL);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(0);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.getWorldBorder().setCenter(this.cornLocation.getX(), this.cornLocation.getZ());
        world.getWorldBorder().setSize(this.mapWorldborderSize);
        world.getWorldBorder().setDamageBuffer(3);
        world.getWorldBorder().setDamageAmount(0.2);
    }

    @Override
    public void onEnterPreRound() {
        MinigameService.getInstance().refreshGlowing();
        PlayerUtils.showAllPlayers(SGService.getInstance().getPlugin());

        this.alivePlayers.addAll(MinigameService.getInstance().getPlayers());
        SGService.getInstance().updateGameState();

        // Assign spawn locations
        Random random = new Random();
        Map<GameTeam, Long> aliveTeams = this.getAliveTeams();
        int numTeams = aliveTeams.size();
        int numPlayers = (int) aliveTeams.values().stream().mapToLong(x -> x).sum();
        int numSpawnLocations = this.spawnLocations.size();
        int gap = (numSpawnLocations - numPlayers) / numTeams;
        int gapExtra = (numSpawnLocations - numPlayers) % numTeams;
        int offset = random.nextInt(numSpawnLocations);
        var orderedAliveTeams = new ArrayList<>(aliveTeams.entrySet());
        Collections.shuffle(orderedAliveTeams);
        int nextPos = 0;
        for (var entry : orderedAliveTeams) {
            var playerIdsOnTeam = TeamService.getInstance().getTeamMembers(entry.getKey());
            for (UUID playerId : playerIdsOnTeam) {
                this.assignedSpawnLocations.put(playerId, adjustLocation(toLocation(this.spawnLocations.get((nextPos + offset) % numSpawnLocations), this.templateWorldName)));
                ++nextPos;
            }
            nextPos += gap;
            if (gapExtra > 0) {
                ++nextPos;
                --gapExtra;
            }
        }

        // Chests
        this.fillChests();

        // Teleport players to spawn locations
        this.getAliveOnlinePlayers().forEach(player -> {
            player.teleport(this.assignedSpawnLocations.get(player.getUniqueId()));
            player.getInventory().clear();
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            player.setFoodLevel(20);
            player.setTotalExperience(0);
            player.setLevel(4);
            player.setExp(0);
        });
    }

    @Override
    public void onRoundStart() {
        // every 5 minutes
        this.eventTimer = new GameCountupTimer(SGService.getInstance().getPlugin(), 1200) {
//            private static final long CHEST_REFILL = 15;
//            private static final long SUDDEN_DEATH = 21;
//
//            @Override
//            protected void onUpdate() {
//                final long min = this.getModuloCurrentTime(TimeUnit.MINUTES);
//                if (min == CHEST_REFILL - 5) {
//                    Chat.sendAlert(ChatType.ALERT, "Chest refill in 5 minutes.");
//                } else if (min == CHEST_REFILL - 1) {
//                    Chat.sendAlert(ChatType.ALERT, "Chest refill in 1 minute.");
//                } else if (min == CHEST_REFILL) {
//                    SGRound.this.refillChests();
//                } else if (min == SUDDEN_DEATH - 5) {
//                    Chat.sendAlert(ChatType.ALERT, "Sudden death in 5 minutes.");
//                } else if (min == SUDDEN_DEATH - 2) {
//                    Chat.sendAlert(ChatType.ALERT, "Sudden death in 2 minutes.");
//                } else if (min == SUDDEN_DEATH - 1) {
//                    Chat.sendAlert(ChatType.ALERT, "Sudden death in 1 minute.");
//                } else if (min == SUDDEN_DEATH) {
//                    SGRound.this.startSuddenDeath();
//                    this.stop();
//                    SGRound.this.eventTimer = null;
//                }
//            }

            private static final long SUDDEN_DEATH = 6;

            @Override
            protected void onUpdate() {
                final long min = this.getModuloCurrentTime(TimeUnit.MINUTES);
                if (min == SUDDEN_DEATH - 5) {
                    Chat.sendAlert(ChatType.ALERT, "World border closing in 5 minutes.");
                } else if (min == SUDDEN_DEATH - 2) {
                    Chat.sendAlert(ChatType.ALERT, "World border closing in 2 minutes.");
                } else if (min == SUDDEN_DEATH - 1) {
                    Chat.sendAlert(ChatType.ALERT, "World border closing in 1 minute.");
                } else if (min == SUDDEN_DEATH) {
                    SGRound.this.startSuddenDeath();
                    this.stop();
                    SGRound.this.eventTimer = null;
                }
            }
        }.start();
    }

    @Override
    public void onEnterPostRound() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
        if (this.eventTimer != null) {
            this.eventTimer.stop();
        }
    }

    @Override
    public void onCleanup() {
        MultiverseCore core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        worldManager.unloadWorld(this.templateWorldName);
    }

    public String getTemplateWorldName() {
        return this.templateWorldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(this.templateWorldName);
    }

    public Collection<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public Collection<? extends Player> getAliveOnlinePlayers() {
        return this.alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public Map<GameTeam, Long> getAliveTeams() {
        return this.getAlivePlayers().stream()
            .collect(Collectors.groupingBy(playerId -> TeamService.getInstance().getTeamOfPlayer(playerId), Collectors.counting()));
    }

    public boolean isAlive(Player player) {
        return this.isAlive(player.getUniqueId());
    }

    public boolean isAlive(UUID playerId) {
        return this.alivePlayers.contains(playerId);
    }

    public void recordLogoutLocation(Player player) {
        this.logOutLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    public Location getLogoutLocation(Player player) {
        return this.logOutLocations.get(player.getUniqueId());
    }

    public void markDead(UUID playerId) {
        this.alivePlayers.remove(playerId);
        this.logOutLocations.remove(playerId);
    }

    public boolean isTeamAlive(GameTeam team) {
        return this.alivePlayers.stream().anyMatch(playerId -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(playerId), team));
    }

    public void startSuddenDeath() {
        if (this.inSuddenDeath) {
            return;
        }

        this.getWorld().getWorldBorder().setSize(this.cornWorldborderSize, 120);
        Chat.sendAlert(ChatType.WARNING, "The world border is closing rapidly, make your way towards the center!");
//        Chat.sendAlert(ChatType.WARNING, "Sudden death! The world border is closing rapidly, make your way towards the center!");
        SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
        this.inSuddenDeath = true;
    }

    public void recordKill(Player player, Player killer) {
        String playerTeamId = TeamService.getInstance().getTeamOfPlayer(player).id();
        String killerTeamId = TeamService.getInstance().getTeamOfPlayer(killer).id();

        if (!this.kills.containsKey(killerTeamId)) {
            this.kills.put(killerTeamId, new ArrayList<>());
        }

        this.kills.get(killerTeamId).add(playerTeamId);

        // Points
        Chat.sendAlert(killer, ChatType.ACTIVE_INFO, "You got bonus points for eliminating " + player.getDisplayName(), Points.PLAYER_KILL);
        SoundUtils.playSound(killer, StandardSounds.GOAL_MET_MINOR, 1, 1);
        killer.spawnParticle(Particle.TOTEM, killer.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);
        MinigameService.getInstance().earnPoints(killer, "killing", Points.PLAYER_KILL);
    }

    public void respawnPlayer(Player player) {
        if (this.isAlive(player)) {
            return;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // TODO: potentially set gamemode, clear inventory, etc.

        // Mark alive
        this.alivePlayers.add(player.getUniqueId());

        // Teleport
        if (this.isTeamAlive(team)) {
            Player otherPlayer = TeamService.getInstance().getOnlineTeamMembers(team.id()).stream().filter(this::isAlive).filter(p -> !p.equals(player)).findFirst().orElse(null);
            if (otherPlayer == null) {
                return;
            }
            player.teleport(otherPlayer.getLocation());
        } else if (this.assignedSpawnLocations.get(player.getUniqueId()) != null) {
            player.teleport(this.assignedSpawnLocations.get(player.getUniqueId()));
        } else {
            player.teleport(this.getCornLocation());
        }

        Chat.sendAlert(ChatType.ALERT, player.getDisplayName() + ChatColor.GREEN + ChatColor.BOLD + " has been respawned!");

        // Update game state
        SGService.getInstance().updateGameState();
    }

    @Nullable
    public Location getAssignedSpawnLocation(Player player) {
        return this.assignedSpawnLocations.get(player.getUniqueId());
    }

    public Location getCornLocation() {
        return adjustLocation(toLocation(this.cornLocation, this.templateWorldName));
    }

    public void endRound() {
//        BukkitUtils.forEachPlayer(SGService.getInstance()::setupPlayer);
        this.alivePlayers.clear();
        this.triggerRoundEnd();
    }

    private void fillChests() {
        World world = this.getWorld();
        this.chests.forEach(chest -> {
            Block block = world.getBlockAt(chest.pos().getX(), chest.pos().getY(), chest.pos().getZ());
            if (block.getState() instanceof Chest state) {
                state.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(chest.lootTable())));
                state.update();
            } else if (block.getState() instanceof Barrel state) {
                state.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(chest.lootTable())));
                state.update();
            } else {
                SGService.getInstance().getLogger().warning("Tried to fill non-existent chest at " + chest.pos());
            }
        });
    }

    public void refillChests() {
        this.fillChests();
        Chat.sendAlert(ChatType.ALERT, "Chests have been refilled!");
        SoundUtils.playSound(Sound.BLOCK_CHEST_OPEN, 1, 1);
    }

    public void setAlive(Player player) {
        this.alivePlayers.add(player.getUniqueId());
    }
}
