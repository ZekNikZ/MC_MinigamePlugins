package io.zkz.mc.minigameplugins.minigamemanager.task;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.List;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class RulesTask extends MinigameTask {
    private static final int TICK_DELAY = 160;
    private int currentIndex = 0;
    private final List<List<Component>> slides;

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
        var lines = this.slides.get(this.currentIndex);
        lines.forEach(Bukkit.getServer()::sendMessage);

        this.currentIndex++;
    }
}
