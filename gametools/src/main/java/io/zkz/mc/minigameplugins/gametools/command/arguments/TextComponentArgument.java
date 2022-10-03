package io.zkz.mc.minigameplugins.gametools.command.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.mojang.brigadier.StringReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.arguments.ComponentArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serial;
import java.util.*;
import java.util.function.BiFunction;

import static net.minecraft.commands.arguments.ComponentArgument.ERROR_INVALID_JSON;

public class TextComponentArgument<C> extends CommandArgument<C, Component> {
    protected TextComponentArgument(
        final boolean required,
        final @NonNull String name,
        final @NonNull String defaultValue,
        final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
        final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new TextComponentParser<>(), defaultValue, Component.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> CommandArgument.@NonNull Builder<C, Component> newBuilder(final @NonNull String name) {
        return new TextComponentArgument.Builder<>(name);
    }

    /**
     * Create a new required argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Component> of(final @NonNull String name) {
        return TextComponentArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Component> optional(final @NonNull String name) {
        return TextComponentArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new optional argument with a default value
     *
     * @param name         Argument name
     * @param defaultValue Default value
     * @param <C>          Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, Component> optional(
        final @NonNull String name,
        final @NonNull String defaultValue
    ) {
        return TextComponentArgument.<C>newBuilder(name).asOptionalWithDefault(defaultValue).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, Component> {

        private Builder(final @NonNull String name) {
            super(Component.class, name);
        }

        @Override
        public @NonNull CommandArgument<C, Component> build() {
            return new TextComponentArgument<>(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription()
            );
        }
    }

    public static final class TextComponentParser<C> implements ArgumentParser<C, Component> {
        @Override
        public @NonNull ArgumentParseResult<Component> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<String> inputQueue
        ) {
            String peek = inputQueue.peek();
            if (peek == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                    TextComponentParser.class,
                    commandContext
                ));
            }

            int i = 0;
            StringJoiner stringJoiner = new StringJoiner(" ");
            Throwable lastError = null;
            while (i < inputQueue.size()) {
                stringJoiner.add(((LinkedList<String>) inputQueue).get(i));
                ++i;
                StringReader stringReader = new StringReader(stringJoiner.toString());
                try {
                    net.minecraft.network.chat.Component component = net.minecraft.network.chat.Component.Serializer.fromJson(stringReader);
                    if (component == null) {
                        throw ERROR_INVALID_JSON.createWithContext(stringReader, "empty");
                    } else {
                        for (int j = 1; j < i; j++) {
                            inputQueue.remove();
                        }
                        return ArgumentParseResult.success(GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(component)));
                    }
                } catch (Exception var4) {
                    String string = var4.getCause() != null ? var4.getCause().getMessage() : var4.getMessage();
//                    throw ERROR_INVALID_JSON.createWithContext(stringReader, string);
                    Exception e = new JsonParseException(string, commandContext);
                    e.printStackTrace();
                    lastError = e;
                }
            }

            return ArgumentParseResult.failure(lastError);
        }

        @Override
        public @NonNull List<String> suggestions(final @NonNull CommandContext<C> commandContext, final @NonNull String input) {
            return Collections.emptyList();
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }

    public static final class JsonParseException extends ParserException {
        @Serial
        private static final long serialVersionUID = -8903115465005472945L;
        private final String input;

        /**
         * Construct a new string parse exception
         *
         * @param input   Input
         * @param context Command context
         */
        public JsonParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                StringArgument.StringParser.class,
                context,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                CaptionVariable.of("input", input)
            );
            this.input = input;
        }


        /**
         * Get the input provided by the sender
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }
    }
}
