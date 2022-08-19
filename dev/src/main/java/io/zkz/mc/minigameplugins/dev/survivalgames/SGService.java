package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.dev.DevPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONArray;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.JSONUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SGService extends PluginService<DevPlugin> {
    private static final SGService INSTANCE = new SGService();

    public static SGService getInstance() {
        return INSTANCE;
    }

    private final List<SGArena> arenas = new ArrayList<>();
    private BlockVector3 lobbySpawnLocation = BlockVector3.ZERO, gulagSpawnLocation = BlockVector3.ZERO;
    private final List<SGFinalArena> finalArenas = new ArrayList<>();

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("arenas.json"), this::saveData, this::loadData)
        );
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> json) {
        this.arenas.addAll(json.getArray("arenas").stream().map(arenaObj -> {
            TypedJSONObject<Object> arena = new TypedJSONObject<Object>((JSONObject) arenaObj);
            return new SGArena(
                arena.getString("name"),
                arena.getString("folder"),
                arena.getArray("spawnLocations").stream().map(locObj -> JSONUtils.readBlockVector((List<Long>) locObj)).toList(),
                JSONUtils.readBlockVector(arena.getList("cornLocation", Long.class)),
                (int) arena.getLong("cornWorldborderSize"),
                (int) arena.getLong("mapWorldborderSize"),
                arena.getArray("chests").stream().map(chestObj -> {
                    TypedJSONObject<Object> chest = new TypedJSONObject<Object>((JSONObject) chestObj);
                    return new SGChest(
                        JSONUtils.readBlockVector(chest.getList("pos", Long.class)),
                        chest.getString("lootTable")
                    );
                }).toList()
            );
        }).toList());
        this.lobbySpawnLocation = JSONUtils.readBlockVector(json.getList("lobbySpawnLocation", Long.class));
        this.gulagSpawnLocation = JSONUtils.readBlockVector(json.getList("gulagSpawnLocation", Long.class));
        this.finalArenas.addAll(json.getArray("finalArenas").stream().map(arenaObj -> {
            TypedJSONObject<Object> arena = new TypedJSONObject<Object>((JSONObject) arenaObj);
            return new SGFinalArena(
                arena.getString("name"),
                JSONUtils.readBlockVector(arena.getList("spectatorSpawnLocation", Long.class)),
                JSONUtils.readBlockVector(arena.getList("participantSpawnLocation", Long.class))
            );
        }).toList());
    }

    private JSONObject saveData() {
        return new JSONObject(Map.of(
            "arenas", new TypedJSONArray<>(this.arenas.stream().map(arena -> new JSONObject(Map.of(
                "name", arena.name(),
                "folder", arena.folder(),
                "spawnLocations", new TypedJSONArray<>(arena.spawnLocations().stream().map(JSONUtils::toJSON).toList()),
                "cornLocation", JSONUtils.toJSON(arena.cornLocation()),
                "cornWorldborderSize", arena.cornWorldborderSize(),
                "mapWorldborderSize", arena.mapWorldborderSize(),
                "chests", new TypedJSONArray<>(arena.chests().stream().map(chest -> new JSONObject(Map.of(
                    "pos", JSONUtils.toJSON(chest.pos()),
                    "lootTable", chest.lootTable()
                ))).toList())
            ))).toList()),
            "lobbySpawnLocation", JSONUtils.toJSON(this.lobbySpawnLocation),
            "gulagSpawnLocation", JSONUtils.toJSON(this.gulagSpawnLocation),
            "finalArenas", new TypedJSONArray<>(this.finalArenas.stream().map(finalArena -> new JSONObject(Map.of(
                "name", finalArena.name(),
                "spectatorSpawnLocation", JSONUtils.toJSON(finalArena.spectatorSpawnLocation()),
                "participantSpawnLocation", JSONUtils.toJSON(finalArena.participantSpawnLocation())
            ))).toList())
        ));
    }

    public void createFinalArena(String name) {
        this.finalArenas.add(new SGFinalArena(
            name,
            BlockVector3.ZERO,
            BlockVector3.ZERO
        ));
    }

    public void removeFinalArena(String name) {
        this.finalArenas.removeIf(finalArena -> finalArena.name().equals(name));
    }

    public void listFinalArenas(CommandSender sender) {
        sender.sendMessage("Final arenas:");
        this.finalArenas.forEach(finalArena -> sender.sendMessage(" - " + finalArena.name() + " - spec=" + finalArena.spectatorSpawnLocation() + " participant=" + finalArena.participantSpawnLocation()));
    }

    public void setFinalArenaPosSpec(String name, BlockVector3 location) {
        this.finalArenas.replaceAll(finalArena -> finalArena.name().equals(name) ? new SGFinalArena(
            finalArena.name(),
            location,
            finalArena.participantSpawnLocation()
        ) : finalArena);
    }

    public void setFinalArenaPosParticipant(String name, BlockVector3 location) {
        this.finalArenas.replaceAll(finalArena -> finalArena.name().equals(name) ? new SGFinalArena(
            finalArena.name(),
            finalArena.spectatorSpawnLocation(),
            location
        ) : finalArena);
    }

    public void clearMapSpawns(String map) {
        this.arenas.stream().filter(arena -> arena.folder().equals(map)).forEach(arena -> arena.spawnLocations().clear());
    }

    public void addMapSpawn(String map, BlockVector3 location) {
        this.arenas.stream().filter(arena -> arena.folder().equals(map)).forEach(arena -> arena.spawnLocations().add(location));
    }

    public void setMapMiddle(String map, BlockVector3 location) {
        this.arenas.stream().filter(arena -> arena.folder().equals(map)).forEach(arena -> arena.setCornLocation(location));
    }

    public void setMapWorldborder(String map, int min, int max) {
        this.arenas.stream().filter(arena -> arena.folder().equals(map)).forEach(arena -> {
            arena.setCornWorldborderSize(min);
            arena.setMapWorldborderSize(max);
        });
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || !(event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST)) {
            return;
        }

//        event.getPlayer().sendMessage("Clicked on " + event.getClickedBlock());

        String lootTable = switch (event.getPlayer().getInventory().getItemInMainHand().getType()) {
            case FLINT -> "";
            case IRON_INGOT -> "survivalgames:chests/tier1";
            case GOLD_INGOT -> "survivalgames:chests/tier2";
            case DIAMOND -> "survivalgames:chests/tier3";
            case COPPER_INGOT -> "survivalgames:chests/tier1_or_2";
            case EMERALD -> "survivalgames:chests/tier2_or_3";
            default -> null;
        };

//        event.getPlayer().sendMessage("Selected loot table: " + lootTable + " (" + event.getPlayer().getInventory().getItemInMainHand() + ")");

        if (lootTable == null) {
            return;
        }

//        event.getPlayer().sendMessage("World: " + event.getPlayer().getWorld().getName());

        this.arenas.stream().filter(arena -> arena.folder().equals(event.getPlayer().getWorld().getName())).forEach(arena -> {
            BlockVector3 pos = WorldEditService.getInstance().wrapLocation(event.getClickedBlock().getLocation());
            arena.chests().removeIf(chest -> chest.pos().equals(pos));
            if (!lootTable.equals("")) {
                arena.chests().add(new SGChest(pos, lootTable));
                event.getPlayer().sendMessage("Set loot table of chest at " + pos + " to " + lootTable);
            } else {
                event.getPlayer().sendMessage("Reset loot table of chest at " + pos);
            }
            event.setCancelled(true);
        });
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        world.setDifficulty(Difficulty.HARD);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, Boolean.FALSE);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.FALSE);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(0);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, Boolean.FALSE);
        world.setGameRule(GameRule.MOB_GRIEFING, Boolean.FALSE);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, Boolean.FALSE);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.CREATIVE);
    }
}
