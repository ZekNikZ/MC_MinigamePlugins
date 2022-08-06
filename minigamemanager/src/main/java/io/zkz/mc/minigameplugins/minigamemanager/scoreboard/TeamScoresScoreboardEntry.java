package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.service.ScoreService;
import net.md_5.bungee.api.ChatColor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TeamScoresScoreboardEntry extends ScoreboardEntry implements IObserver<ScoreService> {
    private final GameTeam team;

    public TeamScoresScoreboardEntry(GameTeam team) {
        this.team = team;
        ScoreService.getInstance().addListener(this);
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Game Points: " + ChatColor.RESET + "(" + ChatColor.YELLOW + MinigameService.getInstance().getPointMultiplier() + "x" + ChatColor.RESET + ")");
    }

    @Override
    public int getRowCount() {
        return 5;
    }

    @Override
    public void cleanup() {
        ScoreService.getInstance().removeListener(this);
    }

    @Override
    public void handleChanged(ScoreService observable) {
        this.markDirty();
    }
}
