package io.zkz.mc.minigameplugins.battlebox;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.util.JSONUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

class TeamConfig {
    private final BlockVector3 kitSpawn, arenaSpawn, wallMin, wallMax;

    public TeamConfig(TypedJSONObject<Object> json) {
        this.kitSpawn = JSONUtils.readBlockVector(json, "kitSpawn");
        this.arenaSpawn = JSONUtils.readBlockVector(json, "arenaSpawn");
        this.wallMin = JSONUtils.readBlockVector(json, "wallMin");
        this.wallMax = JSONUtils.readBlockVector(json, "wallMax");
    }

    public BlockVector3 kitSpawn() {
        return this.kitSpawn;
    }

    public BlockVector3 arenaSpawn() {
        return this.arenaSpawn;
    }

    public CuboidRegion wall() {
        return WorldEditService.getInstance().createCuboidRegion(this.wallMin, this.wallMax);
    }

    public CuboidRegion wall(BlockVector3 adjust) {
        return WorldEditService.getInstance().createCuboidRegion(this.wallMin.add(adjust), this.wallMax.add(adjust));
    }
}

class KitConfig {
    private final String key;
    private final BlockVector3[] teamPillars = new BlockVector3[2];

    public KitConfig(String key, TypedJSONObject<Object> json) {
        this.key = key;
        this.teamPillars[0] = JSONUtils.readBlockVector(json, "team1");
        this.teamPillars[1] = JSONUtils.readBlockVector(json, "team2");
    }

    public String getKey() {
        return this.key;
    }

    public BlockVector3[] teamPillars() {
        return this.teamPillars;
    }
}

class MapConfig {
    private final BlockVector3 woolMin, woolMax, specSpawn;
    private final List<BlockVector3> potions;
    private final TeamConfig[] teams = new TeamConfig[2];
    private final Material[] validPlacementBlocks;
    private final Material[] validWoolPlacementBlocks;
    private final Material leafBlock;
    private final Map<String, KitConfig> kits;
    private final @Nullable String author;

    public MapConfig(TypedJSONObject<Object> json) {
        this.woolMin = JSONUtils.readBlockVector(json, "woolMin");
        this.woolMax = JSONUtils.readBlockVector(json, "woolMax");
        this.specSpawn = JSONUtils.readBlockVector(json, "specSpawn");
        this.teams[0] = new TeamConfig(new TypedJSONObject<>(json.getObject("team1"), Object.class));
        this.teams[1] = new TeamConfig(new TypedJSONObject<>(json.getObject("team2"), Object.class));
        this.validPlacementBlocks = json.getList("validPlacementBlocks", String.class).stream().map(Material::matchMaterial).filter(Objects::nonNull).toArray(Material[]::new);
        this.validWoolPlacementBlocks = json.getList("validWoolPlacementBlocks", String.class).stream().map(Material::matchMaterial).filter(Objects::nonNull).toArray(Material[]::new);
        this.leafBlock = Material.matchMaterial(json.getString("leafBlock"));
        this.kits = TypedJSONObject.asObjects(json.getObject("kits")).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new KitConfig(entry.getKey(), new TypedJSONObject<>(entry.getValue(), Object.class))
            ));
        this.potions = json.getArray("arenas", List.class).stream()
            .map(JSONUtils::readBlockVector)
            .toList();
        this.author = json.getString("author");
    }

    public CuboidRegion wool(BlockVector3 base) {
        return WorldEditService.getInstance().createCuboidRegion(this.woolMin.add(base), this.woolMax.add(base));
    }

    public TeamConfig[] teams() {
        return this.teams;
    }

    public Material[] validPlacementBlocks() {
        return this.validPlacementBlocks;
    }

    public Material[] validWoolPlacementBlocks() {
        return this.validWoolPlacementBlocks;
    }

    public BlockVector3 specSpawn() {
        return this.specSpawn;
    }

    public Material leafBlock() {
        return this.leafBlock;
    }

    public Map<String, KitConfig> kits() {
        return this.kits;
    }

    public List<BlockVector3> potions() {
        return this.potions;
    }

    public @Nullable String author() {
        return this.author;
    }
}

public class GameConfig {
    private final Map<String, MapConfig> maps = new HashMap<>();
    private final String selectedMap, world;
    private final List<BlockVector3> arenas;

    public GameConfig(TypedJSONObject<Object> json) {
        this.selectedMap = json.getString("selectedMap");
        this.world = json.getString("world");
        this.arenas = json.getArray("arenas", List.class).stream()
            .map(JSONUtils::readBlockVector)
            .toList();
    }

    public Map<String, MapConfig> getMaps() {
        return this.maps;
    }

    public MapConfig map() {
        return this.maps.get(this.selectedMap);
    }

    public World world() {
        return Bukkit.getWorld(this.world);
    }

    public List<BlockVector3> arenas() {
        return this.arenas;
    }

    private BlockVector3 adjustBlockVector(BlockVector3 base, int arenaIndex) {
        return base.add(this.arenas.get(arenaIndex));
    }

    private Location computeLocation(int arenaIndex, BlockVector3 blockVec) {
        BlockVector3 vec = this.adjustBlockVector(blockVec, arenaIndex);
        return new Location(this.world(), vec.getX(), vec.getY(), vec.getZ());
    }

    public Location computedSpecSpawn() {
        return this.computedSpecSpawn(0);
    }

    public Location computedSpecSpawn(int arenaIndex) {
        return this.computeLocation(arenaIndex, this.map().specSpawn());
    }

    public Location computedTeamKitSpawn(int arenaIndex, int teamIndex) {
        return this.computeLocation(arenaIndex, this.map().teams()[teamIndex].kitSpawn());
    }

    public Location computedTeamArenaSpawn(int arenaIndex, int teamIndex) {
        return this.computeLocation(arenaIndex, this.map().teams()[teamIndex].arenaSpawn());
    }

    public Region kitSelectionPodiumRegion(String kit, int arenaIndex, int teamIndex) {
        return WorldEditService.getInstance().createCuboidRegion(
            this.adjustBlockVector(this.map().kits().get(kit).teamPillars()[teamIndex], arenaIndex).add(0, 1, 0),
            this.adjustBlockVector(this.map().kits().get(kit).teamPillars()[teamIndex], arenaIndex).add(0, -1, 0)
        );
    }

    public Region woolRegion(int arenaIndex) {
        return this.map().wool(this.arenas.get(arenaIndex));
    }

    public List<Location> potions() {
        World world = this.world();
        return this.map().potions().stream().map(vec -> new Location(world, vec.getX() + 0.5, vec.getY() + 0.5, vec.getZ() + 0.5)).toList();
    }

    public List<CuboidRegion> allWalls() {
        return Arrays.stream(this.map().teams()).flatMap(team -> this.arenas().stream().map(team::wall)).toList();
    }

    public String selectedMapName() {
        return this.selectedMap;
    }
}
