package io.zkz.mc.minigameplugins.uhc.task;

import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class UpdateSpectatorInventoriesTask extends MinigameTask {
    public UpdateSpectatorInventoriesTask() {
        super(1, 1);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.getGameMode() == GameMode.SPECTATOR)
            .filter(player -> player.getSpectatorTarget() != null && player.getSpectatorTarget() instanceof Player)
            .forEach(player -> {
                player.getInventory().setArmorContents(((Player) player.getSpectatorTarget()).getInventory().getArmorContents());
                player.getInventory().setContents(((Player) player.getSpectatorTarget()).getInventory().getContents());
            });
    }
}
