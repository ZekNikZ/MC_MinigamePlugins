package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.logging.Level;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class DefaultTeams {
    public static final GameTeam SPECTATOR;
    public static final GameTeam GAME_MASTER;
    public static final GameTeam CASTER;
    public static final GameTeam BLUE;
    public static final GameTeam RED;

    static {
        SPECTATOR = new GameTeam("spectators", mm("Spectators"), mm("<dark_gray>[SPEC]"))
            .setScoreboardColor(NamedTextColor.GRAY)
            .setFormatTag("<gray>")
            .setColor(Color.GRAY)
            .setSpectator(true);

        GAME_MASTER = new GameTeam("game_masters", mm("Game Masters"), mm("<gold>[GM]"))
            .setScoreboardColor(NamedTextColor.GRAY)
            .setFormatTag("<gray>")
            .setColor(Color.GRAY)
            .setSpectator(true);

        CASTER = new GameTeam("casters", mm("Casters"), mm("<dark_purple>[CASTER]"))
            .setScoreboardColor(NamedTextColor.GRAY)
            .setFormatTag("<gray>")
            .setColor(Color.GRAY)
            .setSpectator(true);

        BLUE = new GameTeam("blue", mm("Blue Team"), mm("<blue><bold>B"))
            .setScoreboardColor(NamedTextColor.BLUE)
            .setFormatTag("<blue>")
            .setColor(Color.BLUE);

        RED = new GameTeam("red", mm("Red Team"), mm("<red><bold>R"))
            .setScoreboardColor(NamedTextColor.RED)
            .setFormatTag("<red>")
            .setColor(Color.RED);
    }

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
