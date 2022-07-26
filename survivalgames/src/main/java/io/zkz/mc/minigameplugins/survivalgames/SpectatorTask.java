package io.zkz.mc.minigameplugins.survivalgames;

import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SpectatorTask extends MinigameTask {
    public SpectatorTask() {
        super(0, 60);
    }

    @Override
    public void run() {
        if (MinigameService.getInstance().getCurrentState() != MinigameState.IN_GAME) {
            this.cancel();
            return;
        }

        BukkitUtils.forEachPlayer(player -> {
            // Spectate messages
            if (player.getWorld().getName().equals("sg_lobby")) {
                TitleUtils.sendActionBarMessage(player, ChatColor.GOLD + "Use " + ChatColor.AQUA + "/spec" + ChatColor.GOLD + " to spectate the match.");
            }

            // Put into spectator mode
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
