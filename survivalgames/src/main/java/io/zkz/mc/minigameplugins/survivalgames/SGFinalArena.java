package io.zkz.mc.minigameplugins.survivalgames;

import org.bukkit.Location;

public record SGFinalArena(String name, Location spectatorSpawnLocation, Location gameMasterSpawnLocation, Location team1SpawnLocation, Location team2SpawnLocation) {
}