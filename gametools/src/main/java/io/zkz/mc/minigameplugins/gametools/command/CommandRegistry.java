package io.zkz.mc.minigameplugins.gametools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public final class CommandRegistry {
    private final CommandDispatcher<CommandSourceStack> dispatcher;
    private final GTPlugin<?> plugin;

    public CommandRegistry(GTPlugin<?> plugin) {
        this.plugin = plugin;
        this.dispatcher = MinecraftServer.getServer()
            .vanillaCommandDispatcher
            .getDispatcher();
    }

    public void register(LiteralArgumentBuilder<CommandSourceStack> command) {
        this.dispatcher.register(command);
        this.plugin.getLogger().info("Registered command /" + command.getLiteral());
    }
}
