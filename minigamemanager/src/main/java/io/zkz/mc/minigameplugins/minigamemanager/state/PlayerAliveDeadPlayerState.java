package io.zkz.mc.minigameplugins.minigamemanager.state;

import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import org.bukkit.entity.Player;

public record PlayerAliveDeadPlayerState() implements IPlayerState {
    @Override
    public void apply(Player player) {
        MinigameService.getInstance().getCurrentRound().setupPlayer(player);
    }
}
