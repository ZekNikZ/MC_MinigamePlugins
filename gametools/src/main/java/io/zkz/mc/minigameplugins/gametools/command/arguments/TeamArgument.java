package io.zkz.mc.minigameplugins.gametools.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TeamArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of("blue", "red", "game_masters");

    private static final DynamicCommandExceptionType ERROR_TEAM_NOT_FOUND = new DynamicCommandExceptionType((name) -> Component.translatable("team.notFound", name));

    public static TeamArgument team() {
        return new TeamArgument();
    }

    public static GameTeam getTeam(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        String teamId = context.getArgument(name, String.class);
        GameTeam team = TeamService.getInstance().getTeam(teamId);
        if (team == null) {
            throw ERROR_TEAM_NOT_FOUND.create(teamId);
        } else {
            return team;
        }
    }

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        TeamService.getInstance().getAllTeams().stream().map(GameTeam::id).forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
