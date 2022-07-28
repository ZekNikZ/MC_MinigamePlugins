package io.zkz.mc.minigameplugins.minigamemanager.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameTask extends BukkitRunnable {
    private final boolean isRepeating;
    private final int delay;
    private final int period;

    public GameTask(int delay, int period) {
        this.isRepeating = true;
        this.delay = delay;
        this.period = period;
    }

    public GameTask(int delay) {
        this.isRepeating = false;
        this.delay = delay;
        this.period = -1;
    }

    public void start(Plugin plugin) {
        if (this.isRepeating) {
            this.runTaskTimer(plugin, this.delay, this.period);
        } else {
            this.runTaskLater(plugin, this.delay);
        }
    }
}
