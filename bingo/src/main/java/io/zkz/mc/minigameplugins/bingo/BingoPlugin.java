package io.zkz.mc.minigameplugins.bingo;

import io.zkz.mc.minigameplugins.bingo.command.BingoCommands;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "Bingo", version = "2.0")
@Description("Bingo")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("MinigameManager")
public class BingoPlugin extends GTPlugin<BingoPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public BingoPlugin() {
        // Services
        this.register(new BingoService());

        // Commands
        this.register(new BingoCommands());
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