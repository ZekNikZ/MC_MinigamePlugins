package io.zkz.mc.minigameplugins.gametools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.gametools.command.arguments.TeamArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public <A extends ArgumentType<?>> void register(Class<? extends A> argumentType) {
        try {
            Method method = ArgumentTypeInfos.class.getDeclaredMethod("a", Registry.class, String.class, Class.class, ArgumentTypeInfo.class);
            method.setAccessible(true);

            method.invoke(null, Registry.COMMAND_ARGUMENT_TYPE, "gametools:team", argumentType, SingletonArgumentInfo.contextFree(TeamArgument::team));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
