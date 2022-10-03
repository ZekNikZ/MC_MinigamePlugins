package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.util.GTColors;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.logging.Level;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class DefaultTeams {
    private DefaultTeams() {}

    public static final GameTeam SPECTATOR = GameTeam.builder("spectators", mm("Specators"))
        .prefix(mm("<dark_gray>[SPEC]"))
        .formatTag("<light_gray>")
        .color(GTColors.LIGHT_GRAY)
        .scoreboardColor(NamedTextColor.GRAY)
        .spectator(true)
        .build();
    public static final GameTeam GAME_MASTER = GameTeam.builder("game_masters", mm("Game Masters"))
        .prefix(mm("<alert_accent>[GM]"))
        .formatTag("<light_gray>")
        .color(GTColors.LIGHT_GRAY)
        .scoreboardColor(NamedTextColor.GRAY)
        .spectator(true)
        .build();
    public static final GameTeam CASTER = GameTeam.builder("casters", mm("Casters"))
        .prefix(mm("<purple>[CASTER]"))
        .formatTag("<light_gray>")
        .color(GTColors.LIGHT_GRAY)
        .scoreboardColor(NamedTextColor.GRAY)
        .spectator(true)
        .build();
    public static final GameTeam BLUE = GameTeam.builder("blue", mm("Blue Team"))
        .prefix(mm("<blue><bold>B"))
        .formatTag("<blue>")
        .color(GTColors.BLUE)
        .scoreboardColor(NamedTextColor.BLUE)
        .build();
    public static final GameTeam RED = GameTeam.builder("red", mm("Red Team"))
        .prefix(mm("<red><bold>R"))
        .formatTag("<red>")
        .color(GTColors.RED)
        .scoreboardColor(NamedTextColor.RED)
        .build();

    public static void addAll() {
        try {
            TeamService.getInstance().createTeam(GAME_MASTER, true);
            TeamService.getInstance().createTeam(CASTER, true);
            TeamService.getInstance().createTeam(SPECTATOR, true);
            TeamService.getInstance().createTeam(BLUE, true);
            TeamService.getInstance().createTeam(RED, true);
        } catch (TeamService.TeamCreationException exception) {
            GameToolsPlugin.logger().log(Level.SEVERE, exception, () -> "Could not create default teams.");
        }
    }
}
