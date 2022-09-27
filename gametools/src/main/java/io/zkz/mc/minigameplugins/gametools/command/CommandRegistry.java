package io.zkz.mc.minigameplugins.gametools.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.meta.CommandMeta;
import io.leangen.geantyref.TypeToken;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class CommandRegistry {
    private final GTPlugin<?> plugin;

    public CommandRegistry(GTPlugin<?> plugin) {
        this.plugin = plugin;
    }

    public Command.Builder<CommandSender> newBaseCommand(String command) {
        this.plugin.getLogger().info("Registered command /" + command);
        return this.plugin.getCommandManager().commandBuilder(command);
    }

    public Command.Builder<CommandSender> newConfirmableCommand(String command) {
        this.plugin.getLogger().info("Registered confirmable command /" + command);
        Command.Builder<CommandSender> builder = this.plugin.getCommandManager().commandBuilder(command);

        this.plugin.getCommandManager().command(builder.literal("confirm")
            .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
            .handler(this.plugin.getCommandConfirmationManager().createConfirmationExecutionHandler()));

        return builder;
    }

    public void registerCommand(Command.Builder<CommandSender> commandBuilder) {
        this.plugin.getCommandManager().command(commandBuilder);
    }

    public <T> void registerArgument(@NotNull Class<T> clazz, Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier) {
        this.plugin.getCommandManager().parserRegistry().registerParserSupplier(
            TypeToken.get(clazz),
            supplier
        );
    }
}
