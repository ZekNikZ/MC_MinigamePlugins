package io.zkz.mc.minigameplugins.minigamemanager.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class GameTask extends BukkitRunnable {
    private final boolean isRepeating;
    private final int delay;
    private final int period;
    private BukkitTask task = null;

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

    public synchronized void start(Plugin plugin) {
        if (this.isRepeating) {
            this.task = this.runTaskTimer(plugin, this.delay, this.period);
        } else {
            this.task =  this.runTaskLater(plugin, this.delay);
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        this.task = null;
    }

    public synchronized int getTaskId() {
        if (this.task == null) {
            throw new IllegalStateException("Task is not scheduled");
        }

        return this.task.getTaskId();
    }

    public synchronized boolean isScheduled() {
        return this.task != null;
    }
}
