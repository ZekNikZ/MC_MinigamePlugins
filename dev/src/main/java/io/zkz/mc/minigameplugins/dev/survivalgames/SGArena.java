package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SGArena {
    private String name;
    private String folder;
    private List<BlockVector3> spawnLocations;
    private BlockVector3 cornLocation;
    private int cornWorldborderSize;
    private int mapWorldborderSize;
    private List<SGChest> chests;

    SGArena(String name, String folder, List<BlockVector3> spawnLocations, BlockVector3 cornLocation,
            int cornWorldborderSize, int mapWorldborderSize, List<SGChest> chests) {
        this.name = name;
        this.folder = folder;
        this.spawnLocations = new ArrayList<>(spawnLocations);
        this.cornLocation = cornLocation;
        this.cornWorldborderSize = cornWorldborderSize;
        this.mapWorldborderSize = mapWorldborderSize;
        this.chests = new ArrayList<>(chests);
    }

    public String name() {
        return name;
    }

    public String folder() {
        return folder;
    }

    public List<BlockVector3> spawnLocations() {
        return spawnLocations;
    }

    public BlockVector3 cornLocation() {
        return cornLocation;
    }

    public int cornWorldborderSize() {
        return cornWorldborderSize;
    }

    public int mapWorldborderSize() {
        return mapWorldborderSize;
    }

    public List<SGChest> chests() {
        return chests;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SGArena) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.folder, that.folder) &&
            Objects.equals(this.spawnLocations, that.spawnLocations) &&
            Objects.equals(this.cornLocation, that.cornLocation) &&
            this.cornWorldborderSize == that.cornWorldborderSize &&
            this.mapWorldborderSize == that.mapWorldborderSize &&
            Objects.equals(this.chests, that.chests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, folder, spawnLocations, cornLocation, cornWorldborderSize, mapWorldborderSize, chests);
    }

    @Override
    public String toString() {
        return "SGArena[" +
            "name=" + name + ", " +
            "folder=" + folder + ", " +
            "spawnLocations=" + spawnLocations + ", " +
            "cornLocation=" + cornLocation + ", " +
            "cornWorldborderSize=" + cornWorldborderSize + ", " +
            "mapWorldborderSize=" + mapWorldborderSize + ", " +
            "chests=" + chests + ']';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setSpawnLocations(List<BlockVector3> spawnLocations) {
        this.spawnLocations = spawnLocations;
    }

    public void setCornLocation(BlockVector3 cornLocation) {
        this.cornLocation = cornLocation;
    }

    public void setCornWorldborderSize(int cornWorldborderSize) {
        this.cornWorldborderSize = cornWorldborderSize;
    }

    public void setMapWorldborderSize(int mapWorldborderSize) {
        this.mapWorldborderSize = mapWorldborderSize;
    }

    public void setChests(List<SGChest> chests) {
        this.chests = chests;
    }
}