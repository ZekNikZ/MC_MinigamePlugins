package io.zkz.mc.minigameplugins.lobby;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.util.Pair;

import java.util.List;

public class Podiums {
    public static final List<Pair<BlockVector3, BlockVector3>> PODIUMS = List.of(
        new Pair<>(BlockVector3.at(-16, 115, -25), BlockVector3.at(-14, 110, -27)),
        new Pair<>(BlockVector3.at(-10, 114, -27), BlockVector3.at(-8, 110, -25)),
        new Pair<>(BlockVector3.at(-4, 113, -27), BlockVector3.at(-2, 110, -25)),
        new Pair<>(BlockVector3.at(2, 112, -25), BlockVector3.at(4, 110, -27)),
        new Pair<>(BlockVector3.at(8, 111, -27), BlockVector3.at(10, 110, -25)),
        new Pair<>(BlockVector3.at(14, 110, -27), BlockVector3.at(16, 110, -25))
    );
}
