package io.zkz.mc.minigameplugins.minigamemanager.state;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public record BasicPlayerState(GameMode expectedGameMode, PotionEffect... potionEffects) implements IPlayerState {
    @Override
    public void apply(Player player) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // Gamemode
        if (team != null && team.isSpectator()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(this.expectedGameMode);
        }

        // Potion effects
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
    }
}
