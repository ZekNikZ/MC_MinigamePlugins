package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Objects;

public final class SGFinalArena {
    private String name;
    private BlockVector3 spectatorSpawnLocation;
    private BlockVector3 participantSpawnLocation;

    SGFinalArena(String name, BlockVector3 spectatorSpawnLocation, BlockVector3 participantSpawnLocation) {
        this.name = name;
        this.spectatorSpawnLocation = spectatorSpawnLocation;
        this.participantSpawnLocation = participantSpawnLocation;
    }

    public String name() {
        return name;
    }

    public BlockVector3 spectatorSpawnLocation() {
        return spectatorSpawnLocation;
    }

    public BlockVector3 participantSpawnLocation() {
        return participantSpawnLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SGFinalArena) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.spectatorSpawnLocation, that.spectatorSpawnLocation) &&
            Objects.equals(this.participantSpawnLocation, that.participantSpawnLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, spectatorSpawnLocation, participantSpawnLocation);
    }

    @Override
    public String toString() {
        return "SGFinalArena[" +
            "name=" + name + ", " +
            "spectatorSpawnLocation=" + spectatorSpawnLocation + ", " +
            "participantSpawnLocation=" + participantSpawnLocation + ']';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpectatorSpawnLocation(BlockVector3 spectatorSpawnLocation) {
        this.spectatorSpawnLocation = spectatorSpawnLocation;
    }

    public void setParticipantSpawnLocation(BlockVector3 participantSpawnLocation) {
        this.participantSpawnLocation = participantSpawnLocation;
    }
}