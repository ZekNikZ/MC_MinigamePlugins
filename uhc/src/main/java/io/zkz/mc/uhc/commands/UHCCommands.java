package io.zkz.mc.uhc.commands;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterPermissions;
import io.zkz.mc.uhc.game.GameManager;
import io.zkz.mc.uhc.lobby.SchematicLoader;
import io.zkz.mc.uhc.settings.SettingsManager;
import io.zkz.mc.uhc.settings.enums.TeamStatus;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;

@RegisterPermissions
public class UHCCommands {
    private static final Permission PERM_SETUP = new Permission("Setup UHC", "Run the UHC setup command");

    private static final Permission PERM_START = new Permission("Start UHC", "Run the UHC start command");

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        var builder = registry.newBaseCommand("uhc");

        registry.registerCommand(
            builder.literal("setup")
                .permission(PERM_SETUP.getName())
                .handler(cmd -> {
                    cmd.getSender().sendMessage(SchematicLoader.loadLobby() ? "success" : "failed");
                    GameManager.getInstance().enterSetupPhase();
                })
        );

        registry.registerCommand(
            builder.literal("start")
                .permission(PERM_START.getName())
                .handler(cmd -> {
                    if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME && GameManager.getInstance().getInitialTeams().size() < 2) {
                        cmd.getSender().sendMessage(ChatColor.RED + "Not enough teams.");
                    } else if (GameManager.getInstance().getInitialCompetitors().size() < 2) {
                        cmd.getSender().sendMessage(ChatColor.RED + "Not enough players.");
                    } else {
                        GameManager.getInstance().enterPregamePhase();
                    }
                })
        );
    }
}
