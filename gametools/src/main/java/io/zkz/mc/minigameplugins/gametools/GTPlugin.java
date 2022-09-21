package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.command.CommandGroup;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.reflection.ReflectionHelper;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
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
        this.getLogger().info("Registered command group " + commandGroup.getClass().getSimpleName());
    }

    protected void register(PluginService<T> service) {
        this.services.add(service);
        this.getLogger().info("Registered service " + service.getClass().getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        // Find annotated services
        this.services.addAll(ReflectionHelper.findAllServices(this.getClassLoader(), this));

        PluginManager pluginManager = this.getServer().getPluginManager();

        this.registerPluginDependents(pluginManager);

        // Init database
        try {
            this.getLogger().info("Initializing database...");
            this.initDB();
            this.getLogger().info("Database initialization complete");
        } catch (SQLException | IOException e) {
            this.getLogger().log(Level.SEVERE, "Error setting up databases", e);
        }

        // Register and enable services
        this.getLogger().info("Initializing services... ");
        services.forEach(service -> service.init((T) this, pluginManager));

        // Register command extras
        CommandRegistry commandRegistry = new CommandRegistry(this);
        this.addToCommandRegistry(commandRegistry);

        // Register commands
        this.getLogger().info("Initializing commands... ");
        commands.forEach(commandGroup -> commandGroup.registerCommands(this));
        ReflectionHelper.findAndRegisterCommands(this.getClassLoader(), this, commandRegistry);

        // Register permissions
        this.getLogger().info("Initializing permissions... ");
        List<Permission> permissions = ReflectionHelper.findPermissions(this.getClassLoader(), this);
        permissions.forEach(perm -> {
            pluginManager.addPermission(perm);
            this.getLogger().info("Registered permission node " + perm.getName());
        });

        this.getLogger().info("Enabled " + this.getName());
    }

    @Override
    public void onDisable() {
        // Disable services
        services.forEach(PluginService::cleanup);

        this.getLogger().info("Disabled " + this.getName());
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
        if (setup != null) {
            MySQLService.getInstance().addInitCommands(setup);
        }
    }

    public InputStream getResourceAsStream(String name) {
        return this.getClassLoader().getResourceAsStream(name);
    }

    protected void registerPluginDependents(PluginManager pluginManager) {

    }

    protected void addToCommandRegistry(CommandRegistry registry) {

    }
}
