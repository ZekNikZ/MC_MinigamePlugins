package io.zkz.mc.minigameplugins.gametools.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import com.sk89q.worldedit.world.World;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.Material;

import java.util.Map;

public class WorldEditService extends GameToolsService {
    private static final WorldEditService INSTANCE = new WorldEditService();
    private static boolean loaded = false;

    public static WorldEditService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }

    public Region createSphericalRegion(BlockVector3 center, double radius) {
        return new SphereRegionFactory().createCenteredAt(center, radius);
    }

    public CuboidRegion createCuboidRegion(BlockVector3 a, BlockVector3 b) {
        return new CuboidRegion(a, b);
    }

    public Region createCuboidRegion(BlockVector3 center, double radius) {
        return new CuboidRegionFactory().createCenteredAt(center, radius);
    }

    public Pattern createPattern(Material mat) {
        return BukkitAdapter.adapt(mat.createBlockData());
    }

    public Pattern createRandomPattern(Map<Material, Double> materials) {
        RandomPattern pattern = new RandomPattern();
        materials.forEach((mat, weight) -> pattern.add(BukkitAdapter.adapt(mat.createBlockData()), weight));
        return pattern;
    }

    public EditSession createEditSession(World world) {
        return WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
    }

    public EditSession createEditSession(World world, int maxBlocks) {
        return WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(maxBlocks).build();
    }

    public void fillRegion(Region region, Pattern pattern) {
        this.fillRegion(region.getWorld(), region, pattern);
    }

    public void fillRegion(World world, Region region, Pattern pattern) {
        try (EditSession session = this.createEditSession(world)) {
            session.setBlocks(region, pattern);
        } catch (MaxChangedBlocksException e) {
            this.getLogger().warning("Max blocks changed in fill attempt");
        }
    }

    public World wrapWorld(org.bukkit.World world) {
        return new BukkitWorld(world);
    }
}
