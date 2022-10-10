package io.zkz.mc.uhc.commands.impl;

import dev.mattrm.mc.uhcplugin.game.GameManager;
import dev.mattrm.mc.uhcplugin.lobby.SchematicLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = "test",
    desc = "Test",
    usage = "/test"
))
public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            SchematicLoader.clearSuddenDeath();
        } else {
            sender.sendMessage(SchematicLoader.loadSuddenDeath() ? "success" : "failed");
        }
        return true;
    }
}
