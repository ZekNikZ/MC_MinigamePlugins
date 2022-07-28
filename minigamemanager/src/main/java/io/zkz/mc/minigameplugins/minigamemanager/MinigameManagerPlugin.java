package io.zkz.mc.minigameplugins.minigamemanager;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "MinigameManager", version = "0.1")
@Description("A plugin to manage minigame states")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
public class MinigameManagerPlugin extends GTPlugin<MinigameManagerPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public MinigameManagerPlugin() {
        // Services
        this.register(MinigameService.getInstance());
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