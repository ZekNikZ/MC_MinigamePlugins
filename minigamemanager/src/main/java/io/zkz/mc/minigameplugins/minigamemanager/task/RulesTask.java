package io.zkz.mc.minigameplugins.minigamemanager.task;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.Bukkit;

import java.util.List;

public class RulesTask extends GameTask {
    private static final int TICK_DELAY = 100;
    private int currentIndex = 0;
    private final List<Character> slides;

    public RulesTask() {
        super(TICK_DELAY, TICK_DELAY);
        this.slides = MinigameService.getInstance().getRulesSlides();
    }

    @Override
    public void run() {
        if (this.currentIndex >= this.slides.size()) {
            MinigameService.getInstance().setState(MinigameState.WAITING_TO_BEGIN);
            return;
        }

        SoundUtils.broadcastSound(StandardSounds.ALERT_INFO, 1, 1);
        Bukkit.getOnlinePlayers().forEach(player -> TitleUtils.sendActionBarMessage(player, "" + this.slides.get(this.currentIndex), 20, MinigameService.getInstance().getPlugin()));

        this.currentIndex++;
    }
}
