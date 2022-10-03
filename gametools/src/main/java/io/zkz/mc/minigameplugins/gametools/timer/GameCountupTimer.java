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

    }

    @Override
    protected void onStop() {

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
}
