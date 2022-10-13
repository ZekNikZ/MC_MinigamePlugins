package io.zkz.mc.uhc.commands;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import org.bukkit.permissions.Permission;

@RegisterPermissions
public class UHCCommands {
    private static final Permission PERM_SETUP = new Permission("Setup UHC", "Run the UHC setup command");

    private static final Permission PERM_START = new Permission("Start UHC", "Run the UHC start command");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
//        var builder = registry.newBaseCommand("uhc");
    }
}
