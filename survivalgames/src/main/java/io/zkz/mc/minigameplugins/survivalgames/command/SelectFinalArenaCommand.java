package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGFinalArena;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.List;
import java.util.stream.Collectors;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SelectFinalArenaCommand.COMMAND_NAME,
    desc = "Select final arenas",
    usage = "/" + SelectFinalArenaCommand.COMMAND_NAME,
    permission = Permissions.FinalArena.SELECT
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.FinalArena.SELECT,
    desc = "Select final arenas"
))
public class SelectFinalArenaCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "selectfinalarena";

    protected SelectFinalArenaCommand() {
        super(COMMAND_NAME, 1);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        List<SGFinalArena> finalArenas = SGService.getInstance().getFinalArenaLists();

        if (finalArenas.stream().noneMatch(arena -> arena.name().equalsIgnoreCase(args[0]))) {
            Chat.sendMessage(sender, ChatColor.RED + "That final arena doesn't exist");
            return true;
        }

        SGService.getInstance().selectFinalArena(args[0]);

        Chat.sendMessageFormatted(sender, "Setting up final arena '%s'. Teleporting in 10 seconds.", args[0]);

        return true;
    }
}
