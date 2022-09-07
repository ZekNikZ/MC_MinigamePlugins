package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.GameTask;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;

import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class SpinnerTask extends GameTask {
    private static final int DELAY = 7;

    private int current;
    private final int max = 6;
    private final Set<Integer> skip;
    private final Consumer<Integer> onFinish;
    private int picked = -1;
    private int minTimes = -1;

    public SpinnerTask(Set<Integer> skip, Consumer<Integer> onFinish) {
        super(DELAY, DELAY);
        this.skip = skip;
        this.onFinish = onFinish;

        // Initial
        this.current = new Random().nextInt(this.max);
        while (this.skip.contains(this.current)) {
            this.current = (this.current + 1) % this.max;
        }
    }

    @Override
    public void run() {
        if (this.picked == this.current && this.minTimes == 0) {
            this.onFinish.accept(this.picked);
            this.cancel();
            return;
        }

        // Unset slice blocks
        SchematicService.getInstance().placeSchematic(this.current + "_black", SpinnerService.getInstance().spinnerLocation(), true);

        // Next slice
        this.current = (this.current + 1) % this.max;
        while (this.skip.contains(this.current)) {
            this.current = (this.current + 1) % this.max;
        }

        // Set slice blocks
        SchematicService.getInstance().placeSchematic(this.current + "_green", SpinnerService.getInstance().spinnerLocation(), true);

        // Tick sound
        SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);

        if (this.picked != -1 && this.minTimes > 0) {
            --this.minTimes;
        }

        SpinnerService.getInstance().getLogger().info(this.current + " " + this.max + " " + this.picked + " " + this.minTimes);
    }

    public void pickResult(int segment) {
        if (segment < 0 || segment >= this.max || this.skip.contains(segment)) {
            throw new IllegalArgumentException("Segment out of range");
        }

        this.picked = segment;
        this.minTimes = this.max * 4;
    }
}
