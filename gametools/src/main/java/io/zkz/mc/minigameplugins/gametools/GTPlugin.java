package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class GTPlugin<T extends GTPlugin<T>> extends JavaPlugin {
    private final List<CommandGroup> commands = new ArrayList<>();
    private final List<PluginService<T>> services = new ArrayList<>();

    protected void register(CommandGroup commandGroup) {
        this.commands.add(commandGroup);
    }

    protected void register(PluginService<T> service) {
        this.services.add(service);
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        // Register and enable services
        this.getLogger().info(ChatColor.YELLOW + "Registering services... ");
        services.forEach(service -> {
            service.setup((T) this);
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
