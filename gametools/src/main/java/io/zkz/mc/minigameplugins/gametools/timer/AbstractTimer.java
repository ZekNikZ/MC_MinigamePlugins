package io.zkz.mc.minigameplugins.gametools.timer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AbstractTimer {
    private final Map<Integer, Runnable> hooks = new ConcurrentHashMap<>();
    private final Map<Integer, Consumer<Runnable>> tempHooks = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;
    private final long refreshRate;
    private int taskId = -1;
    private boolean isStarted = false;
    private boolean isDone = false;
    private int nextHookId = 0;

    protected AbstractTimer(JavaPlugin plugin, long refreshRateTicks) {
        this.plugin = plugin;
        this.refreshRate = refreshRateTicks;
    }

    private void update() {
        this.onUpdate();
        this.hooks.values().forEach(Runnable::run);
        this.tempHooks.forEach((id, hook) -> hook.accept(() -> this.removeHook(id)));
    }

    protected abstract void onStart();

    protected abstract void onUpdate();

    protected void onPause() {
    }

    protected abstract void onStop();

    public AbstractTimer start() {
        if (this.taskId == -1) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this.plugin,
                this::update,
                this.refreshRate,
                this.refreshRate
            );
            this.isStarted = true;
            this.isDone = false;
            this.onStart();
        }

        return this;
    }

    public void pause() {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
            this.onPause();
        }
    }

    public void stop() {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
            this.taskId = -1;
            this.isStarted = false;
            this.isDone = true;
            this.onStop();
        }
    }

    public boolean isRunning() {
        return this.taskId != -1;
    }

    public boolean isPaused() {
        return this.isStarted() && !this.isRunning();
    }

    public boolean isStarted() {
        return this.isStarted;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public int addHook(Runnable hook) {
        this.hooks.put(++nextHookId, hook);
        return nextHookId;
    }

    public int addTempHook(Consumer<Runnable> hook) {
        this.tempHooks.put(++nextHookId, hook);
        return nextHookId;
    }

    public void removeHook(int hookId) {
        this.hooks.remove(hookId);
        this.tempHooks.remove(hookId);
    }

    protected abstract long getCurrentTimeMillis();

    public long getCurrentTime(TimeUnit unit) {
        return unit.convert(this.getCurrentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public long getModuloCurrentTime(TimeUnit unit) {
        return this.getCurrentTime(unit) % switch (unit) {
            case MINUTES, SECONDS -> 60;
            case MILLISECONDS -> 1000;
            default -> 1000000;
        };
    }

    /**
     * Format:
     * H, h     hour-of-day (0-23)          number            0
     * M, m     minute-of-hour              number            30
     * S, s     second-of-minute            number            55
     * L, l     millisecond-of-second       fraction          978
     *
     * @param format the format string
     * @return the formatted string
     */
    public String format(String format) {
        return format
            .replaceAll("%H", String.format("%02d", this.getModuloCurrentTime(TimeUnit.HOURS)))
            .replaceAll("%h", "" + this.getModuloCurrentTime(TimeUnit.HOURS))
            .replaceAll("%M", String.format("%02d", this.getModuloCurrentTime(TimeUnit.MINUTES)))
            .replaceAll("%m", "" + this.getModuloCurrentTime(TimeUnit.MINUTES))
            .replaceAll("%S", String.format("%02d", this.getModuloCurrentTime(TimeUnit.SECONDS)))
            .replaceAll("%s", "" + this.getModuloCurrentTime(TimeUnit.SECONDS))
            .replaceAll("%L", String.format("%03d", this.getModuloCurrentTime(TimeUnit.MILLISECONDS)))
            .replaceAll("%l", "" + this.getModuloCurrentTime(TimeUnit.MILLISECONDS));
    }

    public String toString() {
        if (this.getCurrentTime(TimeUnit.HOURS) > 0) {
            return this.format("%h:%M:%S");
        }

        return this.format("%m:%S");
    }
}