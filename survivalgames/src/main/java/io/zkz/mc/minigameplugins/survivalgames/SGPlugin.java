package io.zkz.mc.minigameplugins.survivalgames;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.survivalgames.command.SGCommands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "SurvivalGames", version = "2.0")
@Description("Survival Games")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("MinigameManager")
@Dependency("Multiverse-Core")
@Dependency("WorldGuard")
public class SGPlugin extends GTPlugin<SGPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public SGPlugin() {
        // Services
        this.register(SGService.getInstance());
        this.register(new ParkourService());

        // Commands
        this.register(new SGCommands());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        logger = this.getLogger();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}