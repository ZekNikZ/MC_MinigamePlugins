package io.zkz.mc.minigameplugins.gametools.timer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

record ScheduledEvent(long delay, Consumer<Long> hook) {
}

record ScheduledRepeatingEvent(long delay, long period, BiConsumer<Long, Runnable> hook) {
}

public abstract class AbstractTimer {
    private final Map<Integer, Runnable> hooks = new ConcurrentHashMap<>();
    private final Map<Integer, Consumer<Runnable>> tempHooks = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;
    private final long refreshRate;
    private int taskId = -1;
    private boolean isStarted = false;
    private boolean isDone = false;
    private int nextHookId = 0;
    private final List<ScheduledEvent> events = new ArrayList<>();
    private final List<Boolean> eventsCompleted = new ArrayList<>();
    private final List<ScheduledRepeatingEvent> repeatingEvents = new ArrayList<>();
    private final List<Long> repeatingEventsLastRunTimes = new ArrayList<>();
    private final List<Boolean> repeatingEventsCancelled = new ArrayList<>();

    protected AbstractTimer(JavaPlugin plugin, long refreshRateTicks) {
        this.plugin = plugin;
        this.refreshRate = refreshRateTicks;
    }

    private void update() {
        this.onUpdate();
        this.hooks.values().forEach(Runnable::run);
        this.tempHooks.forEach((id, hook) -> hook.accept(() -> this.removeHook(id)));

        // Run events
        var currentTime = this.getCurrentTimeMillis();
        for (int i = 0; i < this.events.size(); i++) {
            ScheduledEvent event = this.events.get(i);
            boolean completed = this.eventsCompleted.get(i);
            if (completed) {
                continue;
            }
            if (this.isReadyToRun(event, currentTime)) {
                event.hook().accept(currentTime);
                this.eventsCompleted.set(i, true);
            }
        }

        // Run repeating events
        for (int i = 0; i < this.repeatingEvents.size(); i++) {
            ScheduledRepeatingEvent event = this.repeatingEvents.get(i);
            long lastRun = this.repeatingEventsLastRunTimes.get(i);
            boolean cancelled = this.repeatingEventsCancelled.get(i);
            if (cancelled) {
                continue;
            }
            if (this.isReadyToRun(event, lastRun, currentTime)) {
                final int j = i;
                event.hook().accept(currentTime, () -> this.repeatingEventsCancelled.set(j, true));
                this.repeatingEventsLastRunTimes.set(i, currentTime);
            }
        }
    }

    protected abstract void onStart();

    protected abstract void onUpdate();

    protected abstract void onPause();

    protected abstract void onUnpause();

    protected abstract void onStop();

    protected abstract boolean isReadyToRun(ScheduledEvent event, long currentTimeMillis);

    protected abstract boolean isReadyToRun(ScheduledRepeatingEvent event, long lastRun, long currentTimeMillis);

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

    public void unpause() {
        if (this.taskId == -1) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    this.plugin,
                    this::update,
                    this.refreshRate,
                    this.refreshRate
            );
            this.onUnpause();
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

    public void scheduleEvent(long delay, Runnable hook) {
        this.events.add(new ScheduledEvent(delay, currentTime -> hook.run()));
        this.eventsCompleted.add(false);
    }

    public void scheduleEvent(long delay, Consumer<Long> hook) {
        this.events.add(new ScheduledEvent(delay, hook));
        this.eventsCompleted.add(false);
    }

    public void scheduleRepeatingEvent(long delay, long period, Runnable hook) {
        this.repeatingEvents.add(new ScheduledRepeatingEvent(delay, period, (currentTime, cancel) -> hook.run()));
        this.repeatingEventsLastRunTimes.add(-1L);
        this.repeatingEventsCancelled.add(false);
    }

    public void scheduleRepeatingEvent(long delay, long period, BiConsumer<Long, Runnable> hook) {
        this.repeatingEvents.add(new ScheduledRepeatingEvent(delay, period, hook));
        this.repeatingEventsLastRunTimes.add(-1L);
        this.repeatingEventsCancelled.add(false);
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
                .replace("%H", String.format("%02d", this.getModuloCurrentTime(TimeUnit.HOURS)))
                .replace("%h", "" + this.getModuloCurrentTime(TimeUnit.HOURS))
                .replace("%M", String.format("%02d", this.getModuloCurrentTime(TimeUnit.MINUTES)))
                .replace("%m", "" + this.getModuloCurrentTime(TimeUnit.MINUTES))
                .replace("%S", String.format("%02d", this.getModuloCurrentTime(TimeUnit.SECONDS)))
                .replace("%s", "" + this.getModuloCurrentTime(TimeUnit.SECONDS))
                .replace("%L", String.format("%03d", this.getModuloCurrentTime(TimeUnit.MILLISECONDS)))
                .replace("%l", "" + this.getModuloCurrentTime(TimeUnit.MILLISECONDS));
    }

    public String toString() {
        if (this.getCurrentTime(TimeUnit.HOURS) > 0) {
            return this.format("%h:%M:%S");
        }

        return this.format("%m:%S");
    }
}