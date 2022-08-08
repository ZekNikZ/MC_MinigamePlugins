package io.zkz.mc.minigameplugins.minigamemanager;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.command.MinigameCommands;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.service.ScoreService;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "MinigameManager", version = "1.0")
@Description("A plugin to manage minigame states")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
public class MinigameManagerPlugin extends GTPlugin<MinigameManagerPlugin> {
    public MinigameManagerPlugin() {
        // Services
        this.register(MinigameService.getInstance());
        this.register(ScoreService.getInstance());

        // Commands
        this.register(new MinigameCommands());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}