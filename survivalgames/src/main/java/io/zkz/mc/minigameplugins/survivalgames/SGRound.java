package io.zkz.mc.minigameplugins.survivalgames;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.JSONUtils;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.*;

import static io.zkz.mc.minigameplugins.survivalgames.SGService.adjustLocation;
import static io.zkz.mc.minigameplugins.survivalgames.SGService.toLocation;

public class SGRound extends Round {
    private final String templateWorldName;
    private String actualWorldName;
    private final List<BlockVector3> spawnLocations;
    private final BlockVector3 cornLocation;
    private final long cornWorldborderSize;
    private final long mapWorldborderSize;

    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Map<UUID, Location> logOutLocations = new HashMap<>();
    private final Map<UUID, Location> assignedSpawnLocations = new HashMap<>();

    @SuppressWarnings("unchecked")
    public SGRound(TypedJSONObject<Object> json) {
        super(json.getString("name"));
        this.templateWorldName = json.getString("folder");
        this.spawnLocations = new ArrayList<BlockVector3>(json.getArray("spawnLocations").stream().map(obj -> JSONUtils.readBlockVector((List<Long>) obj)).toList());
        this.cornLocation = JSONUtils.readBlockVector(json.getList("cornLocation", Long.class));
        this.cornWorldborderSize = json.getLong("cornWorldborderSize");
        this.mapWorldborderSize = json.getLong("mapWorldborderSize");
    }

    public JSONObject toJSON() {
        return new JSONObject(Map.of(
            "folder", this.templateWorldName,
            "name", this.getMapName(),
            "spawnLocations", this.spawnLocations.stream().map(JSONUtils::toJSON).toList(),
            "cornLocation", JSONUtils.toJSON(this.cornLocation),
            "cornWorldborderSize", this.cornWorldborderSize,
            "mapWorldborderSize", this.mapWorldborderSize
        ));
    }

    @Override
    public void onSetup() {
        // Create and set up actual world
        MultiverseCore core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        this.actualWorldName = this.templateWorldName + "_active";
        worldManager.cloneWorld(this.templateWorldName, this.actualWorldName);

        World world = worldManager.getMVWorld(this.actualWorldName).getCBWorld();
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
        world.getWorldBorder().setDamageBuffer(0);
    }

    @Override
    public void onPreRound() {
        this.alivePlayers.addAll(MinigameService.getInstance().getPlayers());
        SGService.getInstance().updateGameState();

        // Assign spawn locations
        List<BlockVector3> spawnLocations = new ArrayList<>(this.spawnLocations);
        Collections.shuffle(spawnLocations);
        List<UUID> players = this.alivePlayers.stream().toList();
        for (int i = 0; i < this.alivePlayers.size(); i++) {
            this.assignedSpawnLocations.put(players.get(i), adjustLocation(toLocation(spawnLocations.get(i % spawnLocations.size()), this.actualWorldName)));
        }

        // Teleport players to spawn locations
        this.getAliveOnlinePlayers().forEach(player -> player.teleport(this.assignedSpawnLocations.get(player.getUniqueId())));
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnd() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
    }

    @Override
    public void onPostRound() {

    }

    @Override
    public void onCleanup() {
        MultiverseCore core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = core.getMVWorldManager();
        worldManager.deleteWorld(this.actualWorldName);
        this.actualWorldName = null;
    }

    public String getTemplateWorldName() {
        return this.templateWorldName;
    }

    public @Nullable String getActualWorldName() {
        return this.actualWorldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(this.actualWorldName);
    }

    public Collection<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public Collection<? extends Player> getAliveOnlinePlayers() {
        return this.alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
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

    public void markDead(Player player) {
        this.alivePlayers.remove(player.getUniqueId());
    }

    public boolean isTeamAlive(GameTeam team) {
        return this.alivePlayers.stream().anyMatch(playerId -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(playerId), team));
    }

    public void startSuddenDeath() {
        this.getWorld().getWorldBorder().setSize(this.cornWorldborderSize, 120);
    }
}
