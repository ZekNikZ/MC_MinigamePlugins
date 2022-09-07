package io.zkz.mc.minigameplugins.bingo.util;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.util.BlockUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TimberTask extends MinigameTask {
    private final Set<BlockVector3> seen = new HashSet<>();
    private final Queue<Location> toBreak = new LinkedList<>();
    private boolean first = true;

    public TimberTask(Location loc) {
        super(1, 1);
        this.toBreak.add(loc.clone());
    }

    @Override
    public void run() {
        if (this.toBreak.isEmpty()) {
            this.cancel();
            return;
        }

        Location nextBreak = this.toBreak.remove();
        this.seen.add(WorldEditService.getInstance().wrapLocation(nextBreak));

        if (BlockUtils.isLog(nextBreak.getBlock().getType()) || this.first) {
            this.first = false;
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -2; z <= 2; z++) {
                        Location loc = nextBreak.clone().add(x, y, z);
                        if (BlockUtils.isLog(loc.getBlock().getType()) || BlockUtils.isLeaves(loc.getBlock().getType())) {
                            tryAddToQueue(nextBreak.clone().add(x, y, z));
                        }
                    }
                }
            }
        }

        nextBreak.getBlock().breakNaturally();
    }

    private void tryAddToQueue(Location location) {
        if (this.seen.contains(WorldEditService.getInstance().wrapLocation(location))) {
            return;
        }

        this.toBreak.add(location);
    }
}
