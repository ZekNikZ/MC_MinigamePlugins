package io.zkz.mc.minigameplugins.gametools.sound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class SoundUtils {
    public static void broadcastSound(Sound sound, float volume, float pitch) {
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }
}
