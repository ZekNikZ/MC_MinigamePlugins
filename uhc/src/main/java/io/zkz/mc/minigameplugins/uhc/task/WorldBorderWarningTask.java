package io.zkz.mc.minigameplugins.uhc.task;

import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.ActionBarService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.uhc.game.UHCService;
import io.zkz.mc.minigameplugins.uhc.game.UHCRound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class WorldBorderWarningTask extends MinigameTask {
    private int i = 0;

    public WorldBorderWarningTask() {
        super(20, 20);
    }

    @Override
    public void run() {
        ++i;

        MinigameState currentState = MinigameService.getInstance().getCurrentState();
        if (currentState != MinigameState.IN_GAME && currentState != MinigameState.IN_GAME_2 && currentState != MinigameState.IN_GAME_3) {
            this.cancel();
            return;
        }

        UHCRound round = UHCService.getMinigame().getCurrentRound();

        Bukkit.getOnlinePlayers().forEach(player -> {
            double worldBorderRadius = round.getCurrentWorldborderSize() / 2.0;
            double worldBorderSpeed = round.getCurrentWorldBorderSpeed();
            double x = player.getLocation().getX();
            double z = player.getLocation().getZ();
            double playerRadius = Math.max(Math.abs(x), Math.abs(z));

            if (playerRadius <= round.getCurrentWorldBorderTarget() / 2) {
                ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning1");
                ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning2");
                return;
            }

            final int leeway = 20;
            if (worldBorderRadius - playerRadius <= worldBorderSpeed * (60 + leeway)) {
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning1", mm("<legacy_red>The world border will pass you in less than 1 minute!"));
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning2", mm("<legacy_dark_red>The world border will pass you in less than 1 minute!"));
                if (i % 2 == 0) {
                    playWarningSoundToPlayer(player);
                }
            } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (180 + leeway)) {
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning1", mm("<legacy_red>The world border will pass you in less than 3 minutes!"));
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning2", mm("<legacy_dark_red>The world border will pass you in less than 3 minutes!"));
                if (i % 3 == 0) {
                    playWarningSoundToPlayer(player);
                }
            } else if (worldBorderRadius - playerRadius <= worldBorderSpeed * (300 + leeway)) {
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning1", mm("<legacy_red>The world border will pass you in less than 5 minutes!"));
                ActionBarService.getInstance().addMessage(player.getUniqueId(), "wbWarning2", mm("<legacy_dark_red>The world border will pass you in less than 5 minutes!"));
                if (i % 4 == 0) {
                    playWarningSoundToPlayer(player);
                }
            } else {
                ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning1");
                ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning2");
            }
        });
    }

    public void playWarningSoundToPlayer(Player player) {
        player.playSound(player.getLocation(), StandardSounds.ALERT_INFO, 0.5f, 0.5f);
    }
}
