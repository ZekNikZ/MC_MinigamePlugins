package io.zkz.mc.minigameplugins.gametools.command.arguments;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.minecraft.extras.TextColorArgument;
import io.leangen.geantyref.TypeToken;
import io.zkz.mc.minigameplugins.gametools.util.GTColor;
import io.zkz.mc.minigameplugins.gametools.util.GTColors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serial;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public final class GTColorArgument<C> extends CommandArgument<C, GTColor> {
    private static final Pattern HEX_PREDICATE = Pattern.compile(
        "#?([a-fA-F0-9]{1,6})"
    );

    private GTColorArgument(
        final boolean required,
        final @NonNull String name,
        final @NonNull String defaultValue,
        final @Nullable BiFunction<@NonNull CommandContext<C>,
            @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
        final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new GTColorParser<>(), defaultValue, TypeToken.get(GTColor.class), suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, GTColor> of(final @NonNull String name) {
        return GTColorArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, GTColor> optional(final @NonNull String name) {
        return GTColorArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name        Component name
     * @param defaultUUID Default uuid
     * @param <C>         Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, GTColor> optional(
        final @NonNull String name,
        final @NonNull UUID defaultUUID
    ) {
        return GTColorArgument.<C>newBuilder(name).asOptionalWithDefault(defaultUUID.toString()).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, GTColor> {

        private Builder(final @NonNull String name) {
            super(GTColor.class, name);
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull GTColorArgument<C> build() {
            return new GTColorArgument<>(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription()
            );
        }
    }

    public static final class GTColorParser<C> implements ArgumentParser<C, GTColor> {
        @Override
        public @NonNull ArgumentParseResult<GTColor> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                    TextColorArgument.TextColorParser.class,
                    commandContext
                ));
            }
            for (final Map.Entry<String, GTColor> pair : GTColors.allColors().entrySet()) {
                if (pair.getKey().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ArgumentParseResult.success(
                        pair.getValue()
                    );
                }
            }
            if (HEX_PREDICATE.matcher(input).matches()) {
                inputQueue.remove();
                return ArgumentParseResult.success(
                    new GTColor(Integer.parseInt(input.startsWith("#") ? input.substring(1) : input, 16))
                );
            }
            return ArgumentParseResult.failure(
                new TextColorParseException(
                    commandContext,
                    input
                )
            );
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            final List<String> suggestions = new LinkedList<>();
            if (input.isEmpty() || input.equals("#") || (HEX_PREDICATE.matcher(input).matches()
                && input.length() < (input.startsWith("#") ? 7 : 6))) {
                for (char c = 'a'; c <= 'f'; c++) {
                    suggestions.add(String.format("%s%c", input, c));
                    suggestions.add(String.format("&%c", c));
                }
                for (char c = '0'; c <= '9'; c++) {
                    suggestions.add(String.format("%s%c", input, c));
                    suggestions.add(String.format("&%c", c));
                }
            }
            suggestions.addAll(GTColors.allColors().keySet());
            return suggestions;
        }
    }

    private static final class TextColorParseException extends ParserException {
        @Serial
        private static final long serialVersionUID = -6236625328843879518L;

        private TextColorParseException(
            final @NonNull CommandContext<?> commandContext,
            final @NonNull String input
        ) {
            super(
                TextColorArgument.TextColorParser.class,
                commandContext,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                CaptionVariable.of("input", input)
            );
        }
    }
}
