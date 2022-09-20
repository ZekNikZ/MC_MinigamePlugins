package io.zkz.mc.minigameplugins.minigamemanager.state;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public record JustGamemodePlayerState(GameMode expectedGameMode) implements IPlayerState {
    @Override
    public void apply(Player player) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // Gamemode
        if (team != null && team.spectator()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(this.expectedGameMode);
        }
    }
}
