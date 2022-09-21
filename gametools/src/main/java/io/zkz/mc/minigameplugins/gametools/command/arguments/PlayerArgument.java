package io.zkz.mc.minigameplugins.gametools.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of("Player", "Notch");

    public static final SimpleCommandExceptionType PLAYER_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.player"));

    final boolean single;

    protected PlayerArgument(boolean singleTarget) {
        this.single = singleTarget;
    }

    public static PlayerArgument player() {
        return new PlayerArgument(true);
    }

    private static boolean checkPlayerName(String username) {
        return Bukkit.getOfflinePlayerIfCached(username) != null;
    }

    public static OfflinePlayer getPlayer(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        String playerName = StringArgumentType.getString(context, name);
        return Bukkit.getOfflinePlayerIfCached(playerName);
    }

    public static PlayerArgument players() {
        return new PlayerArgument(false);
    }

    public static Collection<OfflinePlayer> getPlayers(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        String args = StringArgumentType.getString(context, name);
        String[] playerNames = args.split(" ");
        return Arrays.stream(playerNames).map(Bukkit::getOfflinePlayerIfCached).toList();
    }

    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        int index = stringReader.getCursor();

        String[] playerNames;
        if (this.single) {
            playerNames = new String[]{stringReader.getString()};
        } else {
            playerNames = stringReader.getRemaining().split(" ");
        }

        for (int i = 0; i < playerNames.length; i++) {
            String playerName = playerNames[i];
            if (!checkPlayerName(playerName)) {
                stringReader.setCursor(index);
                throw PLAYER_NOT_FOUND.createWithContext(stringReader);
            }
            index += playerName.length() + 1;
        }

        return String.join(" ", playerNames);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            builder.suggest(offlinePlayer.getName());
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
