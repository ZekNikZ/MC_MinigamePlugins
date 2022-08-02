package io.zkz.mc.minigameplugins.gametools.util;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.List;

public class JSONUtils {
    public static List<Long> toJSON(BlockVector3 vec) {
        return List.of(
            (long) vec.getX(),
            (long) vec.getY(),
            (long) vec.getZ()
        );
    }

    public static BlockVector3 readBlockVector(List<Long> json) {
        return BlockVector3.at(json.get(0), json.get(1), json.get(2));
    }
}
