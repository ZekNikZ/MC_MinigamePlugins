package io.zkz.mc.minigameplugins.uhc.task;

import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;

public class WorldBorderStateCheckerTask extends MinigameTask {
    private int lastWb = -1;

    public WorldBorderStateCheckerTask() {
        super(1, 1);
    }

    @Override
    public void run() {
        var currentState = MinigameService.getInstance().getCurrentState();
        if (currentState != MinigameState.IN_GAME && currentState != MinigameState.IN_GAME_2) {
            this.cancel();
            return;
        }

        int currentWb = (int) WorldSyncUtils.getWorldBorderSize();

        if (currentWb != lastWb) {
            if (currentWb == SettingsManager.SETTING_WORLD_BORDER_DISTANCE_2.value()) {
                this.cancel();
                MinigameService.getInstance().getCurrentRound().triggerPhase1End();
            } else if (currentWb == SettingsManager.SETTING_WORLD_BORDER_DISTANCE_3.value()) {
                this.cancel();
                MinigameService.getInstance().getCurrentRound().triggerPhase2End();
            }

            lastWb = currentWb;
        }
    }
}
