package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.commands.MiscCommands;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.event.CustomEventService;
import io.zkz.mc.minigameplugins.gametools.http.HTTPService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.readyup.command.ReadyUpCommands;
import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourcePackService;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.command.TeamCommands;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;
import io.zkz.mc.minigameplugins.gametools.util.VanishingService;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "GameTools", version = "4.0")
@Description("A library for making minigame plugins")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
//@Dependency("ProtocolLib")
@SoftDependency("WorldEdit")
@SoftDependency("WorldGuard")
public class GameToolsPlugin extends GTPlugin<GameToolsPlugin> {
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

//    private ProtocolManager protocolManager;

    public GameToolsPlugin() {
        // Services
        this.register(MySQLService.getInstance());
        this.register(MinigameConstantsService.getInstance());
        this.register(TeamService.getInstance());
        this.register(ScoreboardService.getInstance());
        this.register(ReadyUpService.getInstance());
        this.register(CustomEventService.getInstance());
        this.register(HTTPService.getInstance());
        this.register(ResourcePackService.getInstance());
        this.register(VanishingService.getInstance());
        this.register(ScoreService.getInstance());

        // Command Groups
        this.register(new TeamCommands());
        this.register(new ReadyUpCommands());
        this.register(new MiscCommands());

        // Misc
        StringUtils.init(this);

//        test();
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
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();
        super.onEnable();
//        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

//    public ProtocolManager getProtocolManager() {
//        return this.protocolManager;
//    }

    public void test() {
        MinecraftServer.getServer()
            .vanillaCommandDispatcher
            .getDispatcher()
            .register(new TestCommand(
                () -> this.getServer().getOnlinePlayers().stream().map(Player::getName).toList(),
                (String st) -> this.getServer().getPlayer(st),
                CommandSourceStack::getBukkitSender
            ).builder("testcommand"));
    }
}