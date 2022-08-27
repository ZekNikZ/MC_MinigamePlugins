package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.logging.Level;

public class DefaultTeams {
    public static final GameTeam SPECTATOR;
    public static final GameTeam GAME_MASTER;
    public static final GameTeam CASTER;
    public static final GameTeam NAVY;
    public static final GameTeam GREEN;
    public static final GameTeam CYAN;
    public static final GameTeam PURPLE;
    public static final GameTeam GOLD;
    public static final GameTeam BLUE;
    public static final GameTeam LIME;
    public static final GameTeam RED;
    public static final GameTeam MAGENTA;
    public static final GameTeam YELLOW;


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

        RED = new GameTeam("red", "Sexy TNTs", ChatColor.BOLD + "ST" + ChatColor.RED)
            .setScoreboardColor(org.bukkit.ChatColor.RED)
            .setFormatCode(ChatColor.RED)
            .setColor(Color.RED);

        MAGENTA = new GameTeam("magenta", "Thunder Buddies", ChatColor.BOLD + "TB" + ChatColor.LIGHT_PURPLE)
            .setScoreboardColor(org.bukkit.ChatColor.LIGHT_PURPLE)
            .setFormatCode(ChatColor.LIGHT_PURPLE)
            .setColor(Color.MAGENTA);

        YELLOW = new GameTeam("yellow", "Rage Queens", ChatColor.BOLD + "RQ" + ChatColor.YELLOW)
            .setScoreboardColor(org.bukkit.ChatColor.YELLOW)
            .setFormatCode(ChatColor.YELLOW)
            .setColor(Color.YELLOW);

        NAVY = new GameTeam("navy", "Team Crafted", ChatColor.BOLD + "TC" + ChatColor.DARK_BLUE)
            .setScoreboardColor(org.bukkit.ChatColor.DARK_BLUE)
            .setFormatCode(ChatColor.DARK_BLUE)
            .setColor(Color.BLUE.darker().darker());

        GREEN = new GameTeam("green", "Chubby Bunnies", ChatColor.BOLD + "CB" + ChatColor.DARK_GREEN)
            .setScoreboardColor(org.bukkit.ChatColor.DARK_GREEN)
            .setFormatCode(ChatColor.DARK_GREEN)
            .setColor(Color.GREEN.darker().darker());

        CYAN = new GameTeam("cyan", "Busty Bros", ChatColor.BOLD + "BB" + ChatColor.DARK_AQUA)
            .setScoreboardColor(org.bukkit.ChatColor.DARK_AQUA)
            .setFormatCode(ChatColor.DARK_AQUA)
            .setColor(Color.CYAN.darker().darker());

        PURPLE = new GameTeam("purple", "The Tatertots", ChatColor.BOLD + "TT" + ChatColor.DARK_PURPLE)
            .setScoreboardColor(org.bukkit.ChatColor.DARK_PURPLE)
            .setFormatCode(ChatColor.DARK_PURPLE)
            .setColor(Color.MAGENTA.darker().darker());

        GOLD = new GameTeam("gold", "Player 2", ChatColor.BOLD + "P2" + ChatColor.GOLD)
            .setScoreboardColor(org.bukkit.ChatColor.GOLD)
            .setFormatCode(ChatColor.GOLD)
            .setColor(Color.YELLOW.darker().darker());

        BLUE = new GameTeam("blue", "Iddy Biddies", ChatColor.BOLD + "IB" + ChatColor.BLUE)
            .setScoreboardColor(org.bukkit.ChatColor.BLUE)
            .setFormatCode(ChatColor.BLUE)
            .setColor(Color.BLUE);

        LIME = new GameTeam("lime", "Sloppy Slayers", ChatColor.BOLD + "SS" + ChatColor.GREEN)
            .setScoreboardColor(org.bukkit.ChatColor.GREEN)
            .setFormatCode(ChatColor.GREEN)
            .setColor(Color.GREEN);
    }

    public static void addAll() {
        try {
            TeamService.getInstance().createTeam(SPECTATOR, true);
            TeamService.getInstance().createTeam(GAME_MASTER, true);
            TeamService.getInstance().createTeam(CASTER, true);
            TeamService.getInstance().createTeam(NAVY, true);
            TeamService.getInstance().createTeam(GREEN, true);
            TeamService.getInstance().createTeam(CYAN, true);
            TeamService.getInstance().createTeam(PURPLE, true);
            TeamService.getInstance().createTeam(GOLD, true);
            TeamService.getInstance().createTeam(BLUE, true);
            TeamService.getInstance().createTeam(LIME, true);
            TeamService.getInstance().createTeam(RED, true);
            TeamService.getInstance().createTeam(MAGENTA, true);
            TeamService.getInstance().createTeam(YELLOW, true);
        } catch (TeamService.TeamCreationException exception) {
            GameToolsPlugin.logger().log(Level.SEVERE, exception, () -> "Could not create default teams.");
        }
    }
}
