package io.zkz.mc.minigameplugins.gametools;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.permission.PermissionChecker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.MM;

public class TestCommand {
    private final @NotNull Supplier<@NotNull Collection<@NotNull String>> playersSuggestions;
    private final @NotNull Function<@NotNull String, @Nullable Audience> toAudience;
    private Function<CommandSourceStack, Audience> fromAToAudience = a -> Audience.empty();

    public TestCommand(@NotNull Supplier<Collection<String>> playersSuggestions, @NotNull Function<@NotNull String, @Nullable Audience> toAudience) {
        this.playersSuggestions = playersSuggestions;
        this.toAudience = toAudience;
    }

    public TestCommand(Supplier<Collection<String>> playersSuggestions, Function<@NotNull String, @Nullable Audience> toAudience, Function<CommandSourceStack, Audience> fromAToAudience) {
        this(playersSuggestions, toAudience);
        this.fromAToAudience = fromAToAudience;
    }

    public LiteralArgumentBuilder<CommandSourceStack> builder(String commandName) {
        return Commands.literal(commandName)
            .requires(a -> getAudience(a).pointers().supports(PermissionChecker.POINTER))
            .executes(cmd -> {
                Audience source = getAudience(cmd.getSource());
                source.sendMessage(MM.deserialize("<red>Test string!"));
                return 1;
            })
            .then(Commands.literal("help")
                .executes(cmd -> {
                    Audience source = getAudience(cmd.getSource());
                    source.sendMessage(MM.deserialize("<red>Help string!"));
                    return 1;
                })
            )
            .then(Commands.argument("blockPos", BlockPosArgument.blockPos())
                .then(Commands.argument("player", StringArgumentType.word())
                    .suggests((argument, builder) -> {
                        playersSuggestions.get().forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(cmd -> {
                        Audience source = getAudience(cmd.getSource());
                        BlockPos blockPos = BlockPosArgument.getSpawnablePos(cmd, "blockPos");
                        String playerName = cmd.getArgument("player", String.class);
                        source.sendMessage(MM.deserialize("<blue>Coordinates! " + blockPos));
                        source.sendMessage(MM.deserialize("<green>Player! " + playerName));
                        return 1;
                    })
                )
            );
    }

    private Audience getAudience(CommandSourceStack possibleAudience) {
        if (possibleAudience instanceof Audience audience) {
            return audience;
        }
        Audience audience = fromAToAudience.apply(possibleAudience);
        return audience != null ? audience : Audience.empty();
    }
}
