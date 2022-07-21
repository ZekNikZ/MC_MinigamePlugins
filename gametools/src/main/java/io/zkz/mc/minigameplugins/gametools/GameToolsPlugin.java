package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.GameToolsCommandGroup;
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

@Plugin(name = "GameTools", version = "4.0")
@Description("A library for making minigame plugins")
@Author("ZekNikZ")
public class GameToolsPlugin extends JavaPlugin {
    private final List<GameToolsCommandGroup> commands = new ArrayList<>();
    private final List<GameToolsService> services = new ArrayList<>();

    public <T extends GameToolsCommandGroup> void register(T commandGroup) {
        this.commands.add(commandGroup);
    }

    public <T extends GameToolsService> void register(T service) {
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
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Register and enable services
        this.getLogger().info(ChatColor.YELLOW + "Registering services... ");
        services.forEach(service -> {
            service.setup(this);
            service.onEnable();
            pluginManager.registerEvents(service, this);
        });

        // Register commands
        this.getLogger().info(ChatColor.YELLOW + "Registering commands... ");
        commands.forEach(commandGroup -> commandGroup.registerCommands(this));

        this.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
    }

    @Override
    public void onDisable() {
        // Disable services
        services.forEach(PluginService::onDisable);

        this.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }
}