package io.zkz.mc.uhc.commands;

import dev.mattrm.mc.gametools.CommandGroup;
import dev.mattrm.mc.uhcplugin.commands.impl.SetupCommand;
import dev.mattrm.mc.uhcplugin.commands.impl.StartGameCommand;
import dev.mattrm.mc.uhcplugin.commands.impl.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCCommands implements CommandGroup {
    @Override
    public void registerCommands(JavaPlugin plugin) {
        plugin.getCommand("uhcsetup").setExecutor(new SetupCommand());
        plugin.getCommand("uhcstartgame").setExecutor(new StartGameCommand());
        plugin.getCommand("test").setExecutor(new TestCommand());
    }
}
