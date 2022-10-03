package io.zkz.mc.minigameplugins.gametools.worldedit;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;

import java.util.Map;

public class RegionService extends PluginService<GameToolsPlugin> {
    private static final RegionService INSTANCE = new RegionService();
    private static boolean loaded = false;

    public static RegionService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }

    public RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
    }

    public ProtectedRegion createGlobalRegion(World world, String regionId) {
        RegionManager manager = this.getRegionManager(world);
        ProtectedRegion protectedRegion = new GlobalProtectedRegion(regionId);
        manager.addRegion(protectedRegion);
        return protectedRegion;
    }

    public ProtectedRegion createProtectedRegion(World world, String regionId, BlockVector3 a, BlockVector3 b) {
        RegionManager manager = this.getRegionManager(world);
        ProtectedRegion protectedRegion = new ProtectedCuboidRegion(regionId, a, b);
        manager.addRegion(protectedRegion);
        return protectedRegion;
    }

    public ProtectedRegion createProtectedRegion(World world, String regionId, Region region) {
        RegionManager manager = this.getRegionManager(world);
        ProtectedRegion protectedRegion = new ProtectedCuboidRegion(regionId, region.getMinimumPoint(), region.getMaximumPoint());
        manager.addRegion(protectedRegion);
        return protectedRegion;
    }

    public ProtectedRegion getRegion(World world, String regionId) {
        return this.getRegionManager(world).getRegion(regionId);
    }

    public Map<String, ProtectedRegion> getRegions(World world) {
        return this.getRegionManager(world).getRegions();
    }

    public void removeProtectedRegion(World world, String regionId) {
        RegionManager manager = this.getRegionManager(world);
        manager.removeRegion(regionId);
    }

    public void removeProtectedRegion(World world, String regionId, RemovalStrategy strategy) {
        RegionManager manager = this.getRegionManager(world);
        manager.removeRegion(regionId, strategy);
    }
}
