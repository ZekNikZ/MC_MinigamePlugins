package io.zkz.mc.minigameplugins.gametools.timer;

import org.bukkit.plugin.java.JavaPlugin;

public class GameCountupTimer extends AbstractTimer {
    private long startTime;
    private long pausedCurrentTime = -1;

    public GameCountupTimer(JavaPlugin plugin, long refreshRateTicks) {
        super(plugin, refreshRateTicks);
    }

    @Override
    protected void onStart() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void onUpdate() {
        // not needed
    }

    @Override
    protected void onStop() {
        // not needed
    }

    @Override
    protected void onPause() {
        this.pausedCurrentTime = this.getCurrentTimeMillis();
    }

    @Override
    protected void onUnpause() {
        this.startTime = System.currentTimeMillis() - this.pausedCurrentTime;
        this.pausedCurrentTime = -1;
    }

    @Override
    protected long getCurrentTimeMillis() {
        return System.currentTimeMillis() - this.startTime;
    }

    @Override
    protected boolean isReadyToRun(ScheduledEvent event, long currentTimeMillis) {
        return currentTimeMillis >= event.delay();
    }

    @Override
    protected boolean isReadyToRun(ScheduledRepeatingEvent event, long lastRun, long currentTimeMillis) {
        return currentTimeMillis >= event.delay() && (currentTimeMillis - lastRun) >= event.period();
    }
}
