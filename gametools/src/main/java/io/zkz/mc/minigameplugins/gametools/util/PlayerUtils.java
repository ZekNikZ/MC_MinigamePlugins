package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlayerUtils {
    public static void hidePlayer(Plugin plugin, Player player) {
        BukkitUtils.forEachPlayer(otherPlayer -> {
            if (otherPlayer.equals(player)) {
                return;
            }

            otherPlayer.hidePlayer(plugin, player);
        });
    }

    public static void showPlayer(Plugin plugin, Player player) {
        BukkitUtils.forEachPlayer(otherPlayer -> {
            if (otherPlayer.equals(player)) {
                return;
            }

            otherPlayer.showPlayer(plugin, player);
        });
    }

    public static void showAllPlayers(Plugin plugin) {
        BukkitUtils.forEachPlayer(player -> {
            BukkitUtils.forEachPlayer(otherPlayer -> {
                if (otherPlayer.equals(player)) {
                    return;
                }

                otherPlayer.showPlayer(plugin, player);
            });
        });
    }
}
