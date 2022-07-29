package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class GTPlugin<T extends GTPlugin<T>> extends JavaPlugin {
    private final List<CommandGroup> commands = new ArrayList<>();
    protected final List<PluginService<T>> services = new ArrayList<>();

    protected void register(CommandGroup commandGroup) {
        this.commands.add(commandGroup);
    }

    protected void register(PluginService<T> service) {
        this.services.add(service);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        this.registerPluginDependents(pluginManager);

        // Init database
        try {
            this.initDB();
        } catch (SQLException | IOException e) {
            this.getLogger().log(Level.SEVERE, ChatColor.RED + "Error setting up databases", e);
        }

        // Register and enable services
        this.getLogger().info(ChatColor.YELLOW + "Registering services... ");
        services.forEach(service -> service.init((T) this, pluginManager));

        // Register commands
        this.getLogger().info(ChatColor.YELLOW + "Registering commands... ");
        commands.forEach(commandGroup -> commandGroup.registerCommands(this));

        this.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
    }

    @Override
    public void onDisable() {
        // Disable services
        services.forEach(PluginService::cleanup);

        this.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }

    private void initDB() throws SQLException, IOException {
        // First lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = this.getResourceAsStream("dbsetup.sql")) {
            if (in == null) {
                return;
            }
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        MySQLService.getInstance().addInitCommands(setup);
    }

    public InputStream getResourceAsStream(String name) {
        return this.getClassLoader().getResourceAsStream(name);
    }

    protected void registerPluginDependents(PluginManager pluginManager) {

    }
}
