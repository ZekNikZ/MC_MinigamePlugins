package io.zkz.mc.minigameplugins.gametools.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TitleUtils {
    private static final Map<Player, BukkitTask> PENDING_MESSAGES = new HashMap<>();

    /**
     * Sends a message to the player's action bar.
     * <p/>
     * The message will appear above the player's hot bar for 2 seconds and then fade away over 1 second.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param message      the message to send.
     */
    public static void sendActionBarMessage(@NotNull Player bukkitPlayer, @NotNull String message) {
        bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    /**
     * Sends a raw message (JSON format) to the player's action bar. Note: while the action bar accepts raw messages
     * it is currently only capable of displaying text.
     * <p/>
     * The message will appear above the player's hot bar for 2 seconds and then fade away over 1 second.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param rawMessage   the json format message to send.
     */
    public static void sendRawActionBarMessage(@NotNull Player bukkitPlayer, @NotNull String rawMessage) {
        bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(rawMessage));
    }

    /**
     * Sends a message to the player's action bar that lasts for an extended duration.
     * <p/>
     * The message will appear above the player's hot bar for the specified duration and fade away during the last
     * second of the duration.
     * <p/>
     * Only one long duration message can be sent at a time per player. If a new message is sent via this message
     * any previous messages still being displayed will be replaced.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param message      the message to send.
     * @param duration     the duration the message should be visible for in seconds.
     * @param plugin       the plugin sending the message.
     */
    public static void sendActionBarMessage(@NotNull final Player bukkitPlayer, @NotNull final String message,
                                     @NotNull final int duration, @NotNull Plugin plugin) {
        cancelPendingMessages(bukkitPlayer);
        final BukkitTask messageTask = new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count >= (duration - 3)) {
                    this.cancel();
                }
                sendActionBarMessage(bukkitPlayer, message);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        PENDING_MESSAGES.put(bukkitPlayer, messageTask);
    }

    public static void cancelPendingMessages(@NotNull Player bukkitPlayer) {
        if (PENDING_MESSAGES.containsKey(bukkitPlayer)) {
            PENDING_MESSAGES.get(bukkitPlayer).cancel();
        }
    }

    public static void sendTitle(Player player, String title) {
        sendTitle(player, title, "");
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 10, 10);
    }

    public static void sendTitle(Player player, String title, int fadeTime, int lingerTime) {
        sendTitle(player, title, "", fadeTime, lingerTime, fadeTime);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeTime, int lingerTime) {
        sendTitle(player, title, subtitle, fadeTime, lingerTime, fadeTime);
    }

    public static void sendTitle(Player player, String title, int fadeInTime, int lingerTime, int fadeOutTime) {
        sendTitle(player, title, "", fadeInTime, lingerTime, fadeOutTime);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeInTime, int lingerTime, int fadeOutTime) {
        player.sendTitle(title, subtitle, fadeInTime, lingerTime, fadeOutTime);
    }

    public static void broadcastTitle(String title) {
        broadcastTitle(title, "");
    }

    public static void broadcastTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, 10, 10, 10);
    }
    
    public static void broadcastTitle(String title, int fadeTime, int lingerTime) {
        broadcastTitle(title, "", fadeTime, lingerTime, fadeTime);
    }

    public static void broadcastTitle(String title, String subtitle, int fadeTime, int lingerTime) {
        broadcastTitle(title, subtitle, fadeTime, lingerTime, fadeTime);
    }

    public static void broadcastTitle(String title, int fadeInTime, int lingerTime, int fadeOutTime) {
        broadcastTitle(title, "", fadeInTime, lingerTime, fadeOutTime);
    }
    
    public static void broadcastTitle(String title, String subtitle, int fadeInTime, int lingerTime, int fadeOutTime) {
        BukkitUtils.forEachPlayer(player -> sendTitle(player, title, subtitle, fadeInTime, lingerTime, fadeOutTime));
    }
}
