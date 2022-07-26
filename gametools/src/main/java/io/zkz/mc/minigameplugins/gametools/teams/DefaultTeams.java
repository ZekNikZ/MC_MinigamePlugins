package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import org.bukkit.ChatColor;

import java.awt.*;
import java.util.logging.Level;

public class DefaultTeams {
    public static final GameTeam SPECTATOR;
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
        SPECTATOR = new GameTeam("spectators", "Spectators", "SPEC");
        
        BLACK = new GameTeam("black", "Black", "BLACK");
        BLACK.setFormatCode(ChatColor.BLACK);
        BLACK.setColor(Color.BLACK);
        
        NAVY = new GameTeam("navy", "Navy", "NAVY");
        NAVY.setFormatCode(ChatColor.DARK_BLUE);
        NAVY.setColor(Color.BLUE.darker().darker());

        GREEN = new GameTeam("green", "Green", "GREEN");
        GREEN.setFormatCode(ChatColor.DARK_GREEN);
        GREEN.setColor(Color.GREEN.darker().darker());

        CYAN = new GameTeam("cyan", "Cyan", "CYAN");
        CYAN.setFormatCode(ChatColor.DARK_AQUA);
        CYAN.setColor(Color.CYAN.darker().darker());

        DARK_RED = new GameTeam("dark_red", "Dark Red", "DARK_RED");
        DARK_RED.setFormatCode(ChatColor.DARK_RED);
        DARK_RED.setColor(Color.RED.darker().darker());

        PURPLE = new GameTeam("purple", "Purple", "PURPLE");
        PURPLE.setFormatCode(ChatColor.DARK_PURPLE);
        PURPLE.setColor(Color.MAGENTA.darker().darker());

        GOLD = new GameTeam("gold", "Gold", "GOLD");
        GOLD.setFormatCode(ChatColor.GOLD);
        GOLD.setColor(Color.YELLOW.darker().darker());

        GRAY = new GameTeam("gray", "Gray", "GRAY");
        GRAY.setFormatCode(ChatColor.GRAY);
        GRAY.setColor(Color.GRAY);

        DARK_GRAY = new GameTeam("dark_gray", "Dark Gray", "DARK_GRAY");
        DARK_GRAY.setFormatCode(ChatColor.DARK_GRAY);
        DARK_GRAY.setColor(Color.DARK_GRAY);

        BLUE = new GameTeam("blue", "Blue", "BLUE");
        BLUE.setFormatCode(ChatColor.BLUE);
        BLUE.setColor(Color.BLUE);

        LIME = new GameTeam("lime", "Lime", "LIME");
        LIME.setFormatCode(ChatColor.GREEN);
        LIME.setColor(Color.GREEN);

        AQUA = new GameTeam("aqua", "Aqua", "AQUA");
        AQUA.setFormatCode(ChatColor.AQUA);
        AQUA.setColor(Color.CYAN);

        RED = new GameTeam("red", "Red", "RED");
        RED.setFormatCode(ChatColor.RED);
        RED.setColor(Color.RED);

        MAGENTA = new GameTeam("magenta", "Magenta", "MAGENTA");
        MAGENTA.setFormatCode(ChatColor.LIGHT_PURPLE);
        MAGENTA.setColor(Color.MAGENTA);

        YELLOW = new GameTeam("yellow", "Yellow", "YELLOW");
        YELLOW.setFormatCode(ChatColor.YELLOW);
        YELLOW.setColor(Color.YELLOW);

        WHITE = new GameTeam("white", "White", "WHITE");
        WHITE.setFormatCode(ChatColor.WHITE);
        WHITE.setColor(Color.WHITE);
    }

    public static void addAll() {
        try {
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
