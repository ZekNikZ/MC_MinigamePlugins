package io.zkz.mc.uhc.game;

import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.uhc.UHCPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@Service
public class UHCService extends PluginService<UHCPlugin> {
    @Override
    protected void onEnable() {
        MinigameService.getInstance().setMinigame(new UHCMinigame());

        TeamService.getInstance().setupDefaultTeams();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        MinigameService.getInstance().getCurrentRound().setupPlayer(event.getPlayer());
    }

    public static UHCMinigame getMinigame() {
        return MinigameService.getInstance().getMinigame();
    }
}
