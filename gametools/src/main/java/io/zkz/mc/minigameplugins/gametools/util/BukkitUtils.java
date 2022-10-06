package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.event.CustomEventService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BukkitUtils {
    private BukkitUtils() {
    }

    public static void runNow(Runnable runnable) {
        Bukkit.getScheduler().runTask(CustomEventService.getInstance().getPlugin(), runnable);
    }

    public static void runNextTick(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEventService.getInstance().getPlugin(), runnable, 1);
    }

    public static void dispatchEvent(Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static void dispatchNextTick(Supplier<Event> event) {
        runNextTick(() -> dispatchEvent(event.get()));
    }

    public static void forEachPlayer(Consumer<Player> func) {
        Bukkit.getOnlinePlayers().forEach(func);
    }

    public static void runLater(Runnable runnable, int delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEventService.getInstance().getPlugin(), runnable, delay);
    }

    public static Collection<? extends Player> allPlayersExcept(Player... players) {
        return allPlayersExcept(Arrays.asList(players));
    }

    public static Collection<? extends Player> allPlayersExcept(Collection<? extends Player> players) {
        var res = new HashSet<>(Bukkit.getOnlinePlayers());
        players.forEach(res::remove);
        return res;
    }
}
