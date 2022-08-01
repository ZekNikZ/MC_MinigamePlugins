package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Chat {
    public static void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }

    public static void sendMessage(Collection<? extends Player> players, String message) {
        players.forEach(player -> sendMessage(player, message));
    }

    public static void sendMessage(String message) {
        sendMessage(Bukkit.getOnlinePlayers(), message);
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
}
