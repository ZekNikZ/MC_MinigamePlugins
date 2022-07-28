package io.zkz.mc.minigameplugins.minigamemanager.state;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public record BasicPlayerState(GameMode expectedGameMode) implements IPlayerState {
    @Override
    public void apply(Player player) {
        player.setGameMode(this.expectedGameMode);
    }
}
