package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.proxy.ProtocolLibProxy;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = GameToolsPlugin.PLUGIN_NAME, version = "4.1")
@Description("A library for making minigame plugins")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@SoftDependency("ProtocolLib")
@SoftDependency("WorldEdit")
@SoftDependency("WorldGuard")
public class GameToolsPlugin extends GTPlugin<GameToolsPlugin> {
    public static final String PLUGIN_NAME = "GameTools";

    private static Logger logger;  // NOSONAR java:S2387

    public static Logger logger() {
        return logger;
    }

    public GameToolsPlugin() {
        // Misc
        StringUtils.init(this);
    }

    @Override
    protected void registerPluginDependents(PluginManager pluginManager) {
        if (pluginManager.getPlugin("WorldEdit") != null) {
            this.getLogger().info("WorldEdit found, registering dependent services...");

            WorldEditService.markAsLoaded();
            this.register(WorldEditService.getInstance());

            SchematicService.markAsLoaded();
            this.register(SchematicService.getInstance());
        }

        if (pluginManager.getPlugin("WorldGuard") != null) {
            this.getLogger().info("WorldGuard found, registering dependent services...");

            RegionService.markAsLoaded();
            this.register(RegionService.getInstance());
        }

        if (pluginManager.getPlugin("ProtocolLib") != null) {
            ProtocolLibProxy.setupGlowing(this);
        }
    }

    @Override
    public void onEnable() {
        logger = this.getLogger(); // NOSONAR java:S2696
        super.onEnable();
    }

    @SuppressWarnings("java:S125")
    @Override
    protected void registerCommandFrameworkExtras(CommandRegistry registry) {
//        registry.registerArgument(Component.class, options -> new TextComponentArgument.TextComponentParser<>());
//        registry.registerArgument(GameTeam.class, options -> new TeamArgument.TeamParser<>());
    }
}
