package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.logging.Level;

public class DefaultTeams {
    public static final GameTeam SPECTATOR;
    public static final GameTeam GAME_MASTER;
    public static final GameTeam CASTER;
    public static final GameTeam BLACK;
    public static final GameTeam NAVY;
    public static final GameTeam GREEN;
    public static final GameTeam CYAN;
    public static final GameTeam DARK_RED;
    public static final GameTeam PURPLE;
    public static final GameTeam GOLD;
    public static final GameTeam GRAY;
    public static final GameTeam DARK_GRAY;
    public static final GameTeam BLUE;
    public static final GameTeam LIME;
    public static final GameTeam AQUA;
    public static final GameTeam RED;
    public static final GameTeam MAGENTA;
    public static final GameTeam YELLOW;
    public static final GameTeam WHITE;


    static {
        SPECTATOR = new GameTeam("spectators", "Spectators", ChatColor.DARK_GRAY + "[SPEC]" + ChatColor.GRAY)
                .setScoreboardColor(org.bukkit.ChatColor.GRAY)
                .setFormatCode(ChatColor.GRAY)
                .setColor(Color.GRAY)
                .setSpectator(true);

        GAME_MASTER = new GameTeam("game_masters", "Game Masters", ChatColor.GOLD + "[GM]" + ChatColor.GRAY)
                .setScoreboardColor(org.bukkit.ChatColor.GRAY)
                .setFormatCode(ChatColor.GRAY)
                .setColor(Color.GRAY)
                .setSpectator(true);

        CASTER = new GameTeam("casters", "Casters", ChatColor.DARK_PURPLE + "[CASTER]" + ChatColor.GRAY)
                .setScoreboardColor(org.bukkit.ChatColor.GRAY)
                .setFormatCode(ChatColor.GRAY)
                .setColor(Color.GRAY)
                .setSpectator(true);

        BLACK = new GameTeam("black", "Black Team", "\u24b7")
                .setScoreboardColor(org.bukkit.ChatColor.BLACK)
                .setFormatCode(ChatColor.BLACK)
                .setColor(Color.BLACK);

        NAVY = new GameTeam("navy", "Navy Team", "\u24dd")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_BLUE)
                .setFormatCode(ChatColor.DARK_BLUE)
                .setColor(Color.BLUE.darker().darker());

        GREEN = new GameTeam("green", "Green Team", "\u24bc")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_GREEN)
                .setFormatCode(ChatColor.DARK_GREEN)
                .setColor(Color.GREEN.darker().darker());

        CYAN = new GameTeam("cyan", "Cyan Team", "\u24d2")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_AQUA)
                .setFormatCode(ChatColor.DARK_AQUA)
                .setColor(Color.CYAN.darker().darker());

        DARK_RED = new GameTeam("dark_red", "Crimson Team", "\u24d2")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_RED)
                .setFormatCode(ChatColor.DARK_RED)
                .setColor(Color.RED.darker().darker());

        PURPLE = new GameTeam("purple", "Purple Team", "\u24c5")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_PURPLE)
                .setFormatCode(ChatColor.DARK_PURPLE)
                .setColor(Color.MAGENTA.darker().darker());

        GOLD = new GameTeam("gold", "Gold Team", "\u24bc")
                .setScoreboardColor(org.bukkit.ChatColor.GOLD)
                .setFormatCode(ChatColor.GOLD)
                .setColor(Color.YELLOW.darker().darker());

        GRAY = new GameTeam("gray", "Gray Team", "\u24bc")
                .setScoreboardColor(org.bukkit.ChatColor.GRAY)
                .setFormatCode(ChatColor.GRAY)
                .setColor(Color.GRAY);

        DARK_GRAY = new GameTeam("dark_gray", "Ash Team", "\u24d0")
                .setScoreboardColor(org.bukkit.ChatColor.DARK_GRAY)
                .setFormatCode(ChatColor.DARK_GRAY)
                .setColor(Color.DARK_GRAY);

        BLUE = new GameTeam("blue", "Blue Team", "\u24b7")
                .setScoreboardColor(org.bukkit.ChatColor.BLUE)
                .setFormatCode(ChatColor.BLUE)
                .setColor(Color.BLUE);

        LIME = new GameTeam("lime", "Lime Team", "\u24db")
                .setScoreboardColor(org.bukkit.ChatColor.GREEN)
                .setFormatCode(ChatColor.GREEN)
                .setColor(Color.GREEN);

        AQUA = new GameTeam("aqua", "Aqua Team", "\u24d0")
                .setScoreboardColor(org.bukkit.ChatColor.AQUA)
                .setFormatCode(ChatColor.AQUA)
                .setColor(Color.CYAN);

        RED = new GameTeam("red", "Red Team", "\u24c7")
                .setScoreboardColor(org.bukkit.ChatColor.RED)
                .setFormatCode(ChatColor.RED)
                .setColor(Color.RED);

        MAGENTA = new GameTeam("magenta", "Magenta Team", "\u24dc")
                .setScoreboardColor(org.bukkit.ChatColor.LIGHT_PURPLE)
                .setFormatCode(ChatColor.LIGHT_PURPLE)
                .setColor(Color.MAGENTA);

        YELLOW = new GameTeam("yellow", "Yellow Team", "\u24e8")
                .setScoreboardColor(org.bukkit.ChatColor.YELLOW)
                .setFormatCode(ChatColor.YELLOW)
                .setColor(Color.YELLOW);

        WHITE = new GameTeam("white", "White Team", "\u24e6")
                .setScoreboardColor(org.bukkit.ChatColor.WHITE)
                .setFormatCode(ChatColor.WHITE)
                .setColor(Color.WHITE);
    }

    public static void addAll() {
        try {
            TeamService.getInstance().createTeam(GAME_MASTER, true);
            TeamService.getInstance().createTeam(CASTER, true);
            TeamService.getInstance().createTeam(SPECTATOR, true);
            TeamService.getInstance().createTeam(BLACK, true);
            TeamService.getInstance().createTeam(NAVY, true);
            TeamService.getInstance().createTeam(GREEN, true);
            TeamService.getInstance().createTeam(CYAN, true);
            TeamService.getInstance().createTeam(DARK_RED, true);
            TeamService.getInstance().createTeam(PURPLE, true);
            TeamService.getInstance().createTeam(GOLD, true);
            TeamService.getInstance().createTeam(GRAY, true);
            TeamService.getInstance().createTeam(DARK_GRAY, true);
            TeamService.getInstance().createTeam(BLUE, true);
            TeamService.getInstance().createTeam(LIME, true);
            TeamService.getInstance().createTeam(AQUA, true);
            TeamService.getInstance().createTeam(RED, true);
            TeamService.getInstance().createTeam(MAGENTA, true);
            TeamService.getInstance().createTeam(YELLOW, true);
            TeamService.getInstance().createTeam(WHITE, true);
        } catch (TeamService.TeamCreationException exception) {
            GameToolsPlugin.logger().log(Level.SEVERE, exception, () -> "Could not create default teams.");
        }
    }
}
