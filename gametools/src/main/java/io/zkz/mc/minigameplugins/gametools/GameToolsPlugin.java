package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.command.TeamCommands;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Plugin(name = "GameTools", version = "4.0")
@Description("A library for making minigame plugins")
@Author("ZekNikZ")
public class GameToolsPlugin extends JavaPlugin {
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

    private final List<CommandGroup> commands = new ArrayList<>();
    private final List<GameToolsService> services = new ArrayList<>();

    public void register(CommandGroup commandGroup) {
        this.commands.add(commandGroup);
    }

    public void register(GameToolsService service) {
        this.services.add(service);
    }

    public GameToolsPlugin() {
        // Services
        register(TeamService.getInstance());

        // Command Groups
        register(new TeamCommands());
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();

        PluginManager pluginManager = this.getServer().getPluginManager();

        // Register and enable services
        logger().info(ChatColor.YELLOW + "Registering services... ");
        services.forEach(service -> {
            service.setup(this);
            service.onEnable();
            pluginManager.registerEvents(service, this);
        });

        // Register commands
        logger().info(ChatColor.YELLOW + "Registering commands... ");
        commands.forEach(commandGroup -> commandGroup.registerCommands(this));

        logger().info(ChatColor.GREEN + "Enabled " + this.getName());
    }

    @Override
    public void onDisable() {
        // Disable services
        services.forEach(PluginService::onDisable);

        this.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }
}