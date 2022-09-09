package io.zkz.mc.minigameplugins.potionpanic;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Locations {
    public static final Location SPEC_SPAWN = new Location(Bukkit.getWorld("potionpanic"), 0.5, -44.5, -13.5, 0, 0);

    public static final Location[] TEAM_SPAWNS = new Location[] {
        new Location(Bukkit.getWorld("potionpanic"), -6.5, -48.5, 0.5, -90, 0),
        new Location(Bukkit.getWorld("potionpanic"), 7.5, -48.5, 0.5, 90, 0)
    };

    public static final Location SCHEMATIC_ORIGIN = new Location(Bukkit.getWorld("potionpanic"), 0, -50, 0);

    public static final BlockVector3 ARENA_MIN = BlockVector3.at(-14, -47, -7);
    public static final BlockVector3 ARENA_MAX = BlockVector3.at(14, -50, 7);
}
