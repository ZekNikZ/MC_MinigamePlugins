package io.zkz.mc.minigameplugins.tntrun;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.util.NMSUtils;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

public class FloorRemovalTask extends MinigameTask {
    private final Set<BlockVector3> trackedBlocks = new HashSet<>();

    public FloorRemovalTask() {
        super(20, 3);
    }

    @Override
    public void run() {
        TNTRunService.getInstance().getAlivePlayers().forEach(player -> {
            // First, check if the player is just simply on a block
            Location blockOn = player.getLocation().add(new Vector(0, -0.1, 0));
            if (!blockOn.isWorldLoaded()) {
                return;
            }
            if (blockOn.getBlock().getType() == Material.SAND || blockOn.getBlock().getType() == Material.GRAVEL) {
                this.scheduleBlockForRemoval(blockOn);
                return;
            }

            // Otherwise, find the block the player is crouching on
            final AxisAlignedBB playerBB = NMSUtils.getEntityBoundingBox(player).d(0, -0.1, 0);
            Map<Block, AxisAlignedBB> blockBoxes = new HashMap<>();
            ArrayList<Block> supportingBlocks = new ArrayList<>();
            final Location cornerLoc = player.getLocation().clone().add(-1, -1, -1);
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    Block block = cornerLoc.clone().add(x, 0, z).getBlock();
                    if (block.getType() != Material.AIR) {
                        AxisAlignedBB boundingBox = NMSUtils.getBlockBoundingBox(block);
                        blockBoxes.put(block, boundingBox);
                    }
                }
            }
            blockBoxes.forEach((block, blockBB) -> {
                if (playerBB.c(blockBB)) {
                    supportingBlocks.add(block);
                }
            });

            // Schedule blocks for removal
            for (Block block : supportingBlocks) {
                if (block.getType() == Material.SAND || block.getType() == Material.GRAVEL) {
                    this.scheduleBlockForRemoval(block.getLocation());
                    break; // ensure only one block gets removed
                }
            }
        });
    }

    private void scheduleBlockForRemoval(Location location) {
        BlockVector3 block = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (!this.trackedBlocks.contains(block)) {
            this.trackedBlocks.add(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            final double seconds = 0.45;
            Bukkit.getScheduler().scheduleSyncDelayedTask(TNTRunService.getInstance().getPlugin(), () -> {
                location.getWorld().setBlockData(location, Material.AIR.createBlockData());
                location.getWorld().setBlockData(location.clone().add(0, -1, 0), Material.AIR.createBlockData());
                this.trackedBlocks.remove(block);
            }, (long) (seconds * 20));
        }
    }
}
