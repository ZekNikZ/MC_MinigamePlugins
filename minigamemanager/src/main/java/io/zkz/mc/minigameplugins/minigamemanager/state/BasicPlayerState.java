package io.zkz.mc.minigameplugins.minigamemanager.state;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public record BasicPlayerState(GameMode expectedGameMode, PotionEffect... potionEffects) implements IPlayerState {
    @Override
    public void apply(Player player) {
        player.setGameMode(this.expectedGameMode);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
    }
}
