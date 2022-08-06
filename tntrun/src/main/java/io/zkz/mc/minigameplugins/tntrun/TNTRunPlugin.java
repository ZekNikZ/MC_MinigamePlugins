package io.zkz.mc.minigameplugins.tntrun;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "TNTRun", version = "0.1")
@Description("TNT Run / Spleef")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("MinigameManager")
public class TNTRunPlugin extends GTPlugin<TNTRunPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public TNTRunPlugin() {
        // Services
        register(TNTRunService.getInstance());
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