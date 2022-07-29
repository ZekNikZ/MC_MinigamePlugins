package io.zkz.mc.minigameplugins.gametools;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.zkz.mc.minigameplugins.gametools.commands.MiscCommands;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.event.CustomEventService;
import io.zkz.mc.minigameplugins.gametools.http.HTTPService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.readyup.command.ReadyUpCommands;
import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourcePackService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.command.TeamCommands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "GameTools", version = "4.0")
@Description("A library for making minigame plugins")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("ProtocolLib")
public class GameToolsPlugin extends GTPlugin<GameToolsPlugin> {
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

    private ProtocolManager protocolManager;

    public GameToolsPlugin() {
        // Services
        this.register(MySQLService.getInstance());
        this.register(MinigameConstantsService.getInstance());
        this.register(TeamService.getInstance());
        this.register(ScoreboardService.getInstance());
        this.register(ReadyUpService.getInstance());
        this.register(CustomEventService.getInstance());
        this.register(HTTPService.getInstance());
        this.register(ResourcePackService.getInstance());

        // Command Groups
        this.register(new TeamCommands());
        this.register(new ReadyUpCommands());
        this.register(new MiscCommands());
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();
        super.onEnable();
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }
}