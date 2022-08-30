package io.zkz.mc.minigameplugins.minigamemanager.state;

import io.zkz.mc.minigameplugins.minigamemanager.round.PlayerAliveDeadRound;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.entity.Player;

public record PlayerAliveDeadPlayerState() implements IPlayerState {
    @Override
    public void apply(Player player) {
        ((PlayerAliveDeadRound) MinigameService.getInstance().getCurrentRound()).setupPlayer(player);
    }
}
