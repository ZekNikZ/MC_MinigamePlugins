package io.zkz.mc.minigameplugins.survivalgames;

import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.task.GameTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SpectatorTask extends GameTask {
    public SpectatorTask() {
        super(0, 100);
    }

    @Override
    public void run() {
        BukkitUtils.forEachPlayer(player -> {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                return;
            }

            if (player.getSpectatorTarget() == null) {
                player.getInventory().clear();
            } else if (player.getSpectatorTarget() instanceof Player otherPlayer) {
                player.getInventory().setContents(otherPlayer.getInventory().getContents());
            }
        });
    }
}
