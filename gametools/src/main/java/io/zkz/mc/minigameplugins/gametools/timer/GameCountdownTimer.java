package io.zkz.mc.minigameplugins.gametools.timer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class GameCountdownTimer extends AbstractTimer {
    private final long timerValueMillis;
    private long startTime;
    private final Runnable onDone;

    private long pausedTimeRemaining = -1;

    public GameCountdownTimer(JavaPlugin plugin, long refreshRateTicks, long timerValue, TimeUnit timerValueUnits, Runnable onDone) {
        super(plugin, refreshRateTicks);

        this.timerValueMillis = TimeUnit.MILLISECONDS.convert(timerValue, timerValueUnits);
        this.onDone = onDone;
    }

    public GameCountdownTimer(JavaPlugin plugin, long refreshRateTicks, long timerValue, TimeUnit timerValueUnits) {
        this(plugin, refreshRateTicks, timerValue, timerValueUnits, null);
    }

    @Override
    protected void onStart() {
        this.startTime = System.currentTimeMillis();
        this.onUpdate();
    }

    @Override
    protected void onUpdate() {
        if (this.isDone()) {
            return;
        }

        if (this.getCurrentTimeMillis() <= 0) {
            if (this.onDone != null) {
                this.onDone.run();
            }
            this.stop();
        }
    }

    @Override
    protected void onStop() {
        // not needed
    }

    @Override
    protected long getCurrentTimeMillis() {
        // total time - time elapsed
        return this.timerValueMillis - (System.currentTimeMillis() - this.startTime);
    }

    @Override
    protected void onPause() {
        this.pausedTimeRemaining = this.getCurrentTimeMillis();
    }

    @Override
    protected void onUnpause() {
        this.startTime = this.pausedTimeRemaining - this.timerValueMillis + System.currentTimeMillis();
        this.pausedTimeRemaining = -1;
    }

    @Override
    protected boolean isReadyToRun(ScheduledEvent event, long currentTimeMillis) {
        return currentTimeMillis <= event.delay();
    }

    @Override
    protected boolean isReadyToRun(ScheduledRepeatingEvent event, long lastRun, long currentTimeMillis) {
        // Simple way to avoid adding another condition below
        if (lastRun == -1) {
            lastRun = Long.MAX_VALUE;
        }

        return (this.timerValueMillis - currentTimeMillis) >= event.delay() && (lastRun - currentTimeMillis) >= event.period();
    }
}
