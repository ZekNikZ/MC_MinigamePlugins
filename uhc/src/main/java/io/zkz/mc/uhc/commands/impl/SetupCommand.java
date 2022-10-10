package io.zkz.mc.uhc.commands.impl;

import dev.mattrm.mc.uhcplugin.lobby.SchematicLoader;
import dev.mattrm.mc.uhcplugin.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = "uhcsetup",
    aliases = {"setupuhc"},
    desc = "Setup UHC lobby",
    usage = "/uhcsetup"
))
public class SetupCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(SchematicLoader.loadLobby() ? "success" : "failed");
        GameManager.getInstance().enterSetupPhase();
        return true;
    }
}
