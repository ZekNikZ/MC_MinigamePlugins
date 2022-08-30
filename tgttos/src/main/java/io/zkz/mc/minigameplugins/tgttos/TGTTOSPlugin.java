package io.zkz.mc.minigameplugins.tgttos;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "TGTTOS", version = "1.0")
@Description("To Get to the Other Side")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("MinigameManager")
public class TGTTOSPlugin extends GTPlugin<TGTTOSPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public TGTTOSPlugin() {
        // Services
        register(TGTTOSService.getInstance());
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