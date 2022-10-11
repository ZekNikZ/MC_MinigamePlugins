package io.zkz.mc.minigameplugins.minigamemanager.task;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.ActionBarService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.Bukkit;

import java.util.List;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class RulesTask extends MinigameTask {
    private static final int TICK_DELAY = 160;
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

        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
        // TODO: send rules
        Bukkit.getServer().sendMessage(mm("" + this.slides.get(this.currentIndex)));

        this.currentIndex++;
    }
}
