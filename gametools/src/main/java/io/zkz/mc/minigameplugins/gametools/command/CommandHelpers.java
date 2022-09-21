package io.zkz.mc.minigameplugins.gametools.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CommandHelpers {
    public static SuggestionProvider<CommandSourceStack> suggestions(Supplier<Collection<String>> suggestions) {
        return (context, builder) -> {
            suggestions.get().forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    public static Predicate<CommandSourceStack> hasPermission(String permission) {
        return (source) -> source.getBukkitSender().hasPermission(permission);
    }

    public static Predicate<CommandSourceStack> hasPermission(Permission permission) {
        return (source) -> source.getBukkitSender().hasPermission(permission);
    }

    public static Predicate<CommandSourceStack> hasPermissionOrOp(String permission) {
        return (source) -> source.getBukkitSender().hasPermission(permission) || source.getBukkitSender().isOp();
    }

    public static Predicate<CommandSourceStack> hasPermissionOrOp(Permission permission) {
        return (source) -> source.getBukkitSender().hasPermission(permission) || source.getBukkitSender().isOp();
    }
}
