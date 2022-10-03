package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import static net.kyori.adventure.audience.Audience.audience;

public class Chat {

    public static void sendMessage(Audience audience, double points, Component message) {
        audience.sendMessage(ChatType.NORMAL.format(message, points));
    }

    public static void sendMessage(Audience audience, Component message) {
        audience.sendMessage(ChatType.NORMAL.format(message));
    }

    public static void sendMessage(double points, Component message) {
        sendMessage(audience(Bukkit.getOnlinePlayers()), ChatType.NORMAL, points, message);
    }

    public static void sendMessage(Component message) {
        sendMessage(audience(Bukkit.getOnlinePlayers()), ChatType.NORMAL, message);
    }

    public static void sendMessage(Audience audience, ChatType type, double points, Component message) {
        audience.sendMessage(type.format(message, points));
    }

    public static void sendMessage(Audience audience, ChatType type, Throwable cause, Component message) {
        audience.sendMessage(type.format(message, cause));
    }

    public static void sendMessage(Audience audience, ChatType type, Component message) {
        audience.sendMessage(type.format(message));
    }

    public static void sendMessage(ChatType type, double points, Component message) {
        sendMessage(audience(Bukkit.getOnlinePlayers()), type, points, message);
    }

    public static void sendMessage(ChatType type, Component message) {
        sendMessage(audience(Bukkit.getOnlinePlayers()), type, message);
    }
}
