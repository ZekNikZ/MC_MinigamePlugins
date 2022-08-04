package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

import static net.md_5.bungee.api.ChatColor.*;

public class Chat {
    public static class Constants {
        public static final String INFO_CHAR = YELLOW + "\u2666" + RESET;
        public static final String POINT_CHAR = YELLOW + "\u2605" + RESET;
        public static final String INFO_PREFIX = "[" + INFO_CHAR + "] ";
        public static final String POINT_PREFIX = "[+%points%" + POINT_CHAR + "] ";
        public static final String GAME_PREFIX = "[" + GOLD + BOLD + "%name%" + RESET + "] ";
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }

    public static void sendMessage(Collection<? extends Player> players, String message) {
        players.forEach(player -> sendMessage(player, message));
    }

    public static void sendMessage(String message) {
        sendMessage(Bukkit.getOnlinePlayers(), message);
    }

    public static void sendMessageFormatted(Player player, String message, Object... args) {
        player.sendMessage(message.formatted(args));
    }

    public static void sendMessageFormatted(Collection<? extends Player> players, String message, Object... args) {
        players.forEach(player -> sendMessage(player, message.formatted(args)));
    }

    public static void sendMessageFormatted(String message, Object... args) {
        sendMessage(Bukkit.getOnlinePlayers(), message.formatted(args));
    }

    public static void sendAlert(Player player, ChatType type, String message, float points) {
        sendMessage(player, type.format(message, points));
    }

    public static void sendAlert(Player player, ChatType type, String message) {
        sendMessage(player, type.format(message));
    }

    public static void sendAlert(Collection<? extends Player> players, ChatType type, String message, float points) {
        sendMessage(players, type.format(message, points));
    }

    public static void sendAlert(Collection<? extends Player> players, ChatType type, String message) {
        sendMessage(players, type.format(message));
    }

    public static void sendAlert(ChatType type, String message, float points) {
        sendMessage(type.format(message, points));
    }

    public static void sendAlert(ChatType type, String message) {
        sendMessage(type.format(message));
    }

    public static void sendAlertFormatted(Player player, ChatType type, String message, float points, Object... args) {
        sendMessage(player, type.format(message.formatted(args), points));
    }

    public static void sendAlertFormatted(Player player, ChatType type, String message, Object... args) {
        sendMessage(player, type.format(message.formatted(args)));
    }

    public static void sendAlertFormatted(Collection<? extends Player> players, ChatType type, String message, float points, Object... args) {
        sendMessage(players, type.format(message.formatted(args), points));
    }

    public static void sendAlertFormatted(Collection<? extends Player> players, ChatType type, String message, Object... args) {
        sendMessage(players, type.format(message.formatted(args)));
    }

    public static void sendAlertFormatted(ChatType type, String message, float points, Object... args) {
        sendMessage(type.format(message.formatted(args), points));
    }

    public static void sendAlertFormatted(ChatType type, String message, Object... args) {
        sendMessage(type.format(message.formatted(args)));
    }
}
