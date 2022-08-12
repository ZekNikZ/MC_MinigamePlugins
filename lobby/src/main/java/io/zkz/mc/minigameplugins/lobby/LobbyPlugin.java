package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.lobby.command.LobbyCommands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "Lobby", version = "1.0")
@Description("Lobby")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@Dependency("WorldEdit")
@Dependency("WorldGuard")
@Dependency("SubServers-Client-Bukkit")
public class LobbyPlugin extends GTPlugin<LobbyPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public LobbyPlugin() {
        // Services
        this.register(LobbyService.getInstance());
        this.register(TournamentManager.getInstance());

        // Commands
        this.register(new LobbyCommands());
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