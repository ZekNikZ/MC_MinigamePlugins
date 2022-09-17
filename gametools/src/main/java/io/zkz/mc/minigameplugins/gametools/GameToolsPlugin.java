package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.commands.MiscCommands;
import io.zkz.mc.minigameplugins.gametools.event.CustomEventService;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.teams.command.TeamCommands;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
@SoftDependency("WorldEdit")
@SoftDependency("WorldGuard")
public class GameToolsPlugin extends GTPlugin<GameToolsPlugin> {
    public static final String PLUGIN_NAME = "GameTools";

    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

    public GameToolsPlugin() {
        // Command Groups
        this.register(new TeamCommands());
        this.register(new MiscCommands());

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
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
