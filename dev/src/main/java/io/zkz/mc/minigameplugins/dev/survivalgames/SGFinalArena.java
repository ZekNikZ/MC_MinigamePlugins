package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Objects;

public final class SGFinalArena {
    private String name;
    private BlockVector3 spectatorSpawnLocation;
    private BlockVector3 gameMasterSpawnLocation;
    private BlockVector3 team1SpawnLocation;
    private BlockVector3 team2SpawnLocation;

    SGFinalArena(String name, BlockVector3 spectatorSpawnLocation, BlockVector3 gameMasterSpawnLocation, BlockVector3 team1SpawnLocation, BlockVector3 team2SpawnLocation) {
        this.name = name;
        this.spectatorSpawnLocation = spectatorSpawnLocation;
        this.gameMasterSpawnLocation = gameMasterSpawnLocation;
        this.team1SpawnLocation = team1SpawnLocation;
        this.team2SpawnLocation = team2SpawnLocation;
    }

    public String name() {
        return this.name;
    }

    public BlockVector3 spectatorSpawnLocation() {
        return this.spectatorSpawnLocation;
    }

    public BlockVector3 gameMasterSpawnLocation() {
        return this.gameMasterSpawnLocation;
    }

    public BlockVector3 team1SpawnLocation() {
        return this.team1SpawnLocation;
    }

    public BlockVector3 team2SpawnLocation() {
        return this.team2SpawnLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SGFinalArena that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(spectatorSpawnLocation, that.spectatorSpawnLocation) && Objects.equals(gameMasterSpawnLocation, that.gameMasterSpawnLocation) && Objects.equals(team1SpawnLocation, that.team1SpawnLocation) && Objects.equals(team2SpawnLocation, that.team2SpawnLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, spectatorSpawnLocation, gameMasterSpawnLocation, team1SpawnLocation, team2SpawnLocation);
    }

    @Override
    public String toString() {
        return "SGFinalArena{" +
            "name='" + name + '\'' +
            ", spectatorSpawnLocation=" + spectatorSpawnLocation +
            ", gameMasterSpawnLocation=" + gameMasterSpawnLocation +
            ", team1SpawnLocation=" + team1SpawnLocation +
            ", team2SpawnLocation=" + team2SpawnLocation +
            '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpectatorSpawnLocation(BlockVector3 spectatorSpawnLocation) {
        this.spectatorSpawnLocation = spectatorSpawnLocation;
    }

    public void setGameMasterSpawnLocation(BlockVector3 gameMasterSpawnLocation) {
        this.gameMasterSpawnLocation = gameMasterSpawnLocation;
    }

    public void setTeam1SpawnLocation(BlockVector3 team1SpawnLocation) {
        this.team1SpawnLocation = team1SpawnLocation;
    }

    public void setTeam2SpawnLocation(BlockVector3 team2SpawnLocation) {
        this.team2SpawnLocation = team2SpawnLocation;
    }
}