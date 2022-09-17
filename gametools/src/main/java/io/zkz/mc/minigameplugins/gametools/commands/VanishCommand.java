package io.zkz.mc.minigameplugins.gametools.commands;

import io.zkz.mc.minigameplugins.gametools.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.util.VanishingService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = VanishCommand.COMMAND_NAME,
    desc = "Vanish/unvanish yourself or others",
    usage = "/" + VanishCommand.COMMAND_NAME + " [player] [otherPlayer]",
    permission = Permissions.Vanish.COMMAND
))
@org.bukkit.plugin.java.annotation.permission.Permissions({
    @Permission(
        name = Permissions.Vanish.COMMAND,
        desc = "Use the /vanish command"
    ),
    @Permission(
        name = Permissions.Vanish.SELF,
        desc = "Vanish yourself"
    ),
    @Permission(
        name = Permissions.Vanish.OTHERS,
        desc = "Vanish other players"
    )
})
public class VanishCommand extends AbstractCommandExecutor implements TabCompleter {
    static final String COMMAND_NAME = "vanish";

    protected VanishCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
            } else {
                VanishingService.getInstance().togglePlayer(player);
                sender.sendMessage(ChatColor.GRAY + "Toggled your vanish status");
            }
            return true;
        } else if (args.length == 1) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + args[0] + " is not online");
            } else {
                VanishingService.getInstance().togglePlayer(player);
                sender.sendMessage(ChatColor.GRAY + "Toggled the vanish status of " + args[0]);
            }
            return true;
        } else if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            Player otherPlayer = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + args[0] + " is not online");
            } else if (otherPlayer == null) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not online");
            } else {
                VanishingService.getInstance().togglePlayer(player, otherPlayer);
                sender.sendMessage(ChatColor.GRAY + "Toggled the vanish status of " + args[0] + " for " + args[1]);
            }
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], playerNames, completions);
        } else if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], playerNames, completions);
        }

        Collections.sort(completions);

        return completions;
    }
}
