package io.zkz.mc.minigameplugins.dev;

import io.zkz.mc.minigameplugins.dev.survivalgames.SGCommands;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.dev.survivalgames.SGService;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "DevPlugin", version = "0.1")
@Description("A test plugin for the GameTools ecosystem")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
public class DevPlugin extends GTPlugin<DevPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public DevPlugin() {
        this.register(SGService.getInstance());
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