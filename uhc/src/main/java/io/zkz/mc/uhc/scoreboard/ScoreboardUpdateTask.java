package io.zkz.mc.uhc.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.uhc.game.UHCRound;

public class ScoreboardUpdateTask extends MinigameTask {
    private int lastWorldborder = -1;

    public ScoreboardUpdateTask() {
        super(1, 1);
    }

    @Override
    public void run() {
        UHCRound currentRound = MinigameService.getInstance().getCurrentRound();
        if (this.lastWorldborder != currentRound.getCurrentWorldborderSize()) {
            this.lastWorldborder = currentRound.getCurrentWorldborderSize();
            ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
        }
    }
}
