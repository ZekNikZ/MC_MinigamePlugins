package io.zkz.mc.minigameplugins.gametools.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;

public class NMSUtils {
    public static Entity toNMS(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public static ServerLevel toNMS(org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static BlockState toNMS(org.bukkit.block.Block block) {
        return ((CraftBlock) block).getNMS();
    }

    public static BlockPos toNMSBlockPos(org.bukkit.Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static AABB getEntityBoundingBox(org.bukkit.entity.Entity entity) {
        return toNMS(entity).getBoundingBox();
    }

    public static AABB getBlockBoundingBox(org.bukkit.block.Block block) {
        BlockState nmsBlockData = toNMS(block);
        Block nmsBlock = nmsBlockData.getBlock();
        ServerLevel nmsWorld = toNMS(block.getWorld());
        BlockPos nmsBlockPos = toNMSBlockPos(block.getLocation());
        VoxelShape nmsVoxel = nmsBlock.getShape(nmsBlockData, nmsWorld, nmsBlockPos, CollisionContext.empty());
        return nmsVoxel.bounds().move(nmsBlockPos);
    }
}
