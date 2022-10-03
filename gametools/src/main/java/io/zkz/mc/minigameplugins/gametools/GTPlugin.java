package io.zkz.mc.minigameplugins.gametools;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.data.MySQLService;
import io.zkz.mc.minigameplugins.gametools.reflection.ReflectionHelper;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public abstract class GTPlugin<T extends GTPlugin<T>> extends JavaPlugin {
    protected final List<PluginService<T>> services = new ArrayList<>();

    private BukkitCommandManager<CommandSender> manager;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    private MinecraftHelp<CommandSender> minecraftHelp;

    protected void register(PluginService<T> service) {
        this.services.add(service);
        this.getLogger().info("Registered service " + service.getClass().getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        //
        // This is a function that will provide a command execution coordinator that parses and executes commands
        // asynchronously
        //
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
            AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
        // This function maps the command sender type of our choice to the bukkit command sender.
        // However, in this example we use the Bukkit command sender, and so we just need to map it
        // to itself
        //
        final Function<CommandSender, CommandSender> mapperFunction = Function.identity(); // NOSONAR java:S4276
        try {
            this.manager = new PaperCommandManager<>(
                /* Owning plugin */ this,
                /* Coordinator function */ executionCoordinatorFunction,
                /* Command Sender -> C */ mapperFunction,
                /* C -> Command Sender */ mapperFunction
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //
        // Create the Minecraft help menu system
        //
        /* Help Prefix */
        /* Audience mapper */
        /* Manager */
        this.minecraftHelp = new MinecraftHelp<>(
            /* Help Prefix */ "/example help",
            /* Audience mapper */ s -> s,
            /* Manager */ this.manager
        );
        //
        // Register Brigadier mappings
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }
        //
        // Register asynchronous completions
        //
        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }
        //
        // Create the confirmation this.manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        //
        this.confirmationManager = new CommandConfirmationManager<>(
            /* Timeout */ 30L,
            /* Timeout unit */ TimeUnit.SECONDS,
            /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
            mm("<alert_warning>Confirmation required. Confirm using <alert_info>/" + context.getCommand().getComponents().get(0).getArgument().getName() + " confirm</alert_info>.")),
            /* Action when no confirmation is pending */ sender -> sender.sendMessage(
            mm("<alert_warning>You don't have any pending commands."))
        );
        //
        // Register the confirmation processor. This will enable confirmations for commands that require it
        //
        this.confirmationManager.registerConfirmationProcessor(this.manager);
        //
        // Override the default exception handlers
        //
        new MinecraftExceptionHandler<CommandSender>()
            .withInvalidSyntaxHandler()
            .withInvalidSenderHandler()
            .withNoPermissionHandler()
            .withArgumentParsingHandler()
            .withCommandExecutionHandler()
            .withDecorator(ChatType.COMMAND_ERROR::format)
            .apply(this.manager, s -> s);

        // Find annotated services
        this.services.addAll(ReflectionHelper.findAllServices(this.getClassLoader(), this));

        PluginManager pluginManager = this.getServer().getPluginManager();

        this.registerPluginDependents(pluginManager);

        // Init database
        try {
            this.getLogger().info("Initializing database...");
            this.initDB();
            this.getLogger().info("Database initialization complete");
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Error setting up databases", e);
        }

        // Register and enable services
        this.getLogger().info("Initializing services... ");
        services.forEach(service -> service.init((T) this, pluginManager));

        // Register command extras
        CommandRegistry commandRegistry = new CommandRegistry(this);
        this.registerCommandFrameworkExtras(commandRegistry);

        // Register commands
        this.getLogger().info("Initializing commands... ");
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

    private void initDB() throws IOException {
        // First lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = this.getResourceAsStream("dbsetup.sql")) {
            if (in == null) {
                return;
            }
            setup = new String(in.readAllBytes());
        } catch (IOException e) { // NOSONAR
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

    protected void registerCommandFrameworkExtras(CommandRegistry registry) {

    }

    public BukkitCommandManager<CommandSender> getCommandManager() {
        return this.manager;
    }

    public CommandConfirmationManager<CommandSender> getCommandConfirmationManager() {
        return this.confirmationManager;
    }
}
