package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.event.CustomEventService;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.function.Supplier;

public class BukkitUtils {
    public static void runNextTick(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEventService.getInstance().getPlugin(), runnable, 1);
    }

    public static void dispatchEvent(Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static void dispatchNextTick(Supplier<Event> event) {
        runNextTick(() -> dispatchEvent(event.get()));
    }
}
