package io.zkz.mc.minigameplugins.gametools.sound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SoundUtils {
    public static void broadcastSound(Sound sound, float volume, float pitch) {
        playSound(Bukkit.getOnlinePlayers(), sound, volume, pitch);
    }

    public static void playSound(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
        players.forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }
}
