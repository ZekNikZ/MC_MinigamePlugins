package io.zkz.mc.minigameplugins.minigamemanager.task;

import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.Bukkit;

import java.util.List;

public class RulesTask extends GameTask {
    private static final int SECOND_DELAY = 5;
    private int currentIndex = 0;
    private final List<Character> slides;

    public RulesTask() {
        super(SECOND_DELAY, SECOND_DELAY);
        this.slides = MinigameService.getInstance().getRulesSlides();
    }

    @Override
    public void run() {
        if (this.currentIndex >= this.slides.size()) {
            MinigameService.getInstance().setState(MinigameState.WAITING_TO_BEGIN);
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> TitleUtils.sendActionBarMessage(player, "" + this.slides.get(this.currentIndex)));

        this.currentIndex++;
    }
}
