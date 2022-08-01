package io.zkz.mc.minigameplugins.gametools.util;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;

public class NMSUtils {
    public static Entity toNMS(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    public static WorldServer toNMS(org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static IBlockData toNMS(org.bukkit.block.Block block) {
        return ((CraftBlock) block).getNMS();
    }

    public static BlockPosition toNMSBlockPos(org.bukkit.Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static AxisAlignedBB getEntityBoundingBox(org.bukkit.entity.Entity entity) {
        return toNMS(entity).cz();
    }

    public static AxisAlignedBB getBlockBoundingBox(org.bukkit.block.Block block) {
        IBlockData nmsBlockData = toNMS(block);
        Block nmsBlock = nmsBlockData.b();
        WorldServer nmsWorld = toNMS(block.getWorld());
        BlockPosition nmsBlockPos = toNMSBlockPos(block.getLocation());
        VoxelShape nmsVoxel = nmsBlock.b(nmsBlockData, nmsWorld, nmsBlockPos, VoxelShapeCollision.a());
        return nmsVoxel.a().a(nmsBlockPos);
    }
}
