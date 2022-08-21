package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGFinalArena;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.List;
import java.util.stream.Collectors;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = ListFinalArenasCommand.COMMAND_NAME,
    desc = "List final arenas",
    usage = "/" + ListFinalArenasCommand.COMMAND_NAME,
    permission = Permissions.FinalArena.LIST
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.FinalArena.LIST,
    desc = "List final arenas"
))
public class ListFinalArenasCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "listfinalarenas";

    protected ListFinalArenasCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        List<SGFinalArena> finalArenas = SGService.getInstance().getFinalArenaLists();

        Chat.sendMessage(sender, "Registered final arenas: " + finalArenas.stream().map(SGFinalArena::name).collect(Collectors.joining(", ")));

        return true;
    }
}
