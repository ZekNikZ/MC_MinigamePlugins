package io.zkz.mc.minigameplugins.tntrun;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.tntrun.service.TNTRunService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class TNTRunRound extends Round {
    private final BlockVector3 arenaMin;
    private final BlockVector3 arenaMax;
    private final BlockVector3 spawnLocation;
    private final int deathYLevel;

    public TNTRunRound(BlockVector3 arenaMin, BlockVector3 arenaMax, BlockVector3 spawnLocation, int deathYLevel, String mapName) {
        super(mapName);
        this.arenaMin = arenaMin;
        this.arenaMax = arenaMax;
        this.spawnLocation = spawnLocation;
        this.deathYLevel = deathYLevel;
    }

    @Override
    public void onSetup() {
        TNTRunService.getInstance().setupArena(this);
    }

    @Override
    public void onPreRound() {
        Bukkit.getOnlinePlayers().forEach(player -> player.teleport(new Location(Bukkit.getWorlds().get(0), this.getSpawnLocation().getX(), this.getSpawnLocation().getY(), this.getSpawnLocation().getZ())));
    }

    public BlockVector3 getArenaMax() {
        return this.arenaMax;
    }

    public BlockVector3 getArenaMin() {
        return this.arenaMin;
    }

    public BlockVector3 getSpawnLocation() {
        return this.spawnLocation;
    }

    public int getDeathYLevel() {
        return this.deathYLevel;
    }

    public void resetArena() {
//        WorldEditService we = WorldEditService.getInstance();
//        World world = we.wrapWorld(Bukkit.getWorlds().get(0));
//
//        Region region = we.createCuboidRegion(BlockVector3.at(-50, 99, -50), BlockVector3.at(50, 99, 50));
//        we.fillRegion(world, region, we.createPattern(Material.TNT));
//
//        region = we.createCuboidRegion(BlockVector3.at(-50, 100, -50), BlockVector3.at(50, 100, 50));
//        we.fillRegion(world, region, we.createPattern(Material.GRAVEL));
    }
}
