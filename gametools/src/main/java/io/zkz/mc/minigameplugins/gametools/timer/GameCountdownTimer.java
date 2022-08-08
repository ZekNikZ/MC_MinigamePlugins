package io.zkz.mc.minigameplugins.gametools.timer;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

// TODO: update timer value upon pause
public class GameCountdownTimer extends AbstractTimer {
    private final long timerValueMillis;
    private long startTime;
    private final Runnable onDone;

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

    }

    @Override
    protected long getCurrentTimeMillis() {
        return this.timerValueMillis - (System.currentTimeMillis() - this.startTime);
    }
}
