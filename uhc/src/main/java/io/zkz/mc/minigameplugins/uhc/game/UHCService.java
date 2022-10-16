package io.zkz.mc.minigameplugins.uhc.game;

import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.util.ActionBarService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.uhc.UHCPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@Service
public class UHCService extends PluginService<UHCPlugin> {
    private static final UHCService INSTANCE = new UHCService();

    public static UHCService getInstance() {
        return INSTANCE;
    }

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

    @EventHandler
    private void onStateChange(StateChangeEvent.Pre event) {
        BukkitUtils.forEachPlayer(player -> {
            ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning1");
            ActionBarService.getInstance().removeMessage(player.getUniqueId(), "wbWarning2");
        });
    }

    @EventHandler
    private void onTeamChange(TeamChangeEvent event) {
        ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
    }
}
