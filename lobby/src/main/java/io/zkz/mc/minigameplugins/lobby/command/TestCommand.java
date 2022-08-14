package io.zkz.mc.minigameplugins.lobby.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = "test",
    desc = "Test command",
    usage = "/test",
    permission = "lobby.test"
))
@Permissions(
    @Permission(name = "lobby.test", desc = "Test")
)
public class TestCommand extends ArgumentCommandExecutor {
    protected TestCommand() {
        super("test", 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
//        TournamentManager.getInstance().startMinigameServer("tntrun", () -> {
//            TournamentManager.getInstance().sendPlayersToServer("tntrun");
//        });
        Bukkit.getServer().reloadData();
        return true;
    }
}
