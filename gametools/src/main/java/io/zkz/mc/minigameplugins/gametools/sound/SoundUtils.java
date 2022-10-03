package io.zkz.mc.minigameplugins.gametools.sound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SoundUtils {
    private SoundUtils() {}

    public static void playSound(Sound sound, float volume, float pitch) {
        playSound(Bukkit.getOnlinePlayers(), sound, volume, pitch);
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void playSound(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
        players.forEach(player -> playSound(player, sound, volume, pitch));
    }
}
