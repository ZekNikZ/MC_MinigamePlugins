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
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;

public final class TeamArgument<C> extends CommandArgument<C, GameTeam> {
    private TeamArgument(
        final boolean required,
        final @NonNull String name,
        final @NonNull String defaultValue,
        final @Nullable BiFunction<@NonNull CommandContext<C>,
            @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider,
        final @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new TeamParser<>(), defaultValue, GameTeam.class, suggestionsProvider, defaultDescription);
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
    public static <C> @NonNull CommandArgument<C, GameTeam> of(final @NonNull String name) {
        return TeamArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, GameTeam> optional(final @NonNull String name) {
        return TeamArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name        Component name
     * @param defaultUUID Default uuid
     * @param <C>         Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, GameTeam> optional(
        final @NonNull String name,
        final @NonNull UUID defaultUUID
    ) {
        return TeamArgument.<C>newBuilder(name).asOptionalWithDefault(defaultUUID.toString()).build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, GameTeam> {

        private Builder(final @NonNull String name) {
            super(GameTeam.class, name);
        }

        /**
         * Builder a new example component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull TeamArgument<C> build() {
            return new TeamArgument<>(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription()
            );
        }
    }

    public static final class TeamParser<C> implements ArgumentParser<C, GameTeam> {
        @SuppressWarnings("java:S2583")
        @Override
        public @NonNull ArgumentParseResult<GameTeam> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                    TeamParser.class,
                    commandContext
                ));
            }

            GameTeam team = TeamService.getInstance().getTeam(input);
            if (team == null) {
                return ArgumentParseResult.failure(new TeamParseException(input, commandContext));

            }

            inputQueue.remove();
            return ArgumentParseResult.success(team);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return TeamService.getInstance().getAllTeams().stream().map(GameTeam::id).toList();
        }

        @Override
        public boolean isContextFree() {
            return true;
        }
    }

    public static final class TeamParseException extends ParserException {
        @Serial
        private static final long serialVersionUID = 6399602590976540023L;
        private final String input;

        /**
         * Construct a new UUID parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public TeamParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                cloud.commandframework.arguments.standard.UUIDArgument.UUIDParser.class,
                context,
                StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
                CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public String getInput() {
            return this.input;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final TeamParseException that = (TeamParseException) o;
            return this.input.equals(that.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.input);
        }
    }
}
