package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "GuessTheBuild", version = "1.0")
@Description("Guess the Build")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("MinigameManager")
@Dependency("SmartInvs")
public class GuessTheBuildPlugin extends GTPlugin<GuessTheBuildPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public GuessTheBuildPlugin() {
        // Services
        register(GuessTheBuildService.getInstance());
        register(new GuessTheBuildCommands());
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