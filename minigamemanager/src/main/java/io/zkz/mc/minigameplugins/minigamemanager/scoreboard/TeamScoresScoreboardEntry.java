package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.service.ScoreService;
import net.md_5.bungee.api.ChatColor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TeamScoresScoreboardEntry extends ScoreboardEntry implements IObserver<ScoreService> {
    private final GameTeam team;

    public TeamScoresScoreboardEntry(GameTeam team) {
        this.team = team;
        ScoreService.getInstance().addListener(this);
    }

    @Override
    public void render(int pos) {
        // Header
        this.getScoreboard().setString(pos, "" + ChatColor.AQUA + ChatColor.BOLD + "Game Points: " + ChatColor.RESET + "(" + ChatColor.YELLOW + MinigameService.getInstance().getPointMultiplier() + "x" + ChatColor.RESET + ")");

        // Get team placements
        List<Map.Entry<GameTeam, Double>> entries = ScoreService.getInstance().getGameTeamScoreSummary().entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .toList();
        int placement = 0;
        final int totalNumTeams = entries.size();
        for (int i = 0; i < totalNumTeams; i++) {
            if (entries.get(i).getKey().equals(this.team)) {
                placement = i;
                break;
            }
        }

        // Determine which teams to display
        List<Integer> placements;
        if (placement == 0) { // team is in first place
            placements = List.of(0, 1, 2, 3);
        } else if (placement == totalNumTeams - 1) { // team is in last place
            placements = List.of(0, totalNumTeams - 3, totalNumTeams - 2,totalNumTeams - 1);
        } else { // team is in the middle
            placements = List.of(0, placement - 1, placement, placement + 1);
        }

        // Write to scoreboard
        AtomicInteger i = new AtomicInteger(1);
        placements.forEach(place -> this.displayScore(pos + i.getAndIncrement(), place, entries.get(place)));
    }

    private void displayScore(int scoreboardPos, int placement, Map.Entry<GameTeam, Double> entry) {
        this.getScoreboard().setString(scoreboardPos, " " + (placement + 1) + ". " + entry.getKey().getDisplayName() + " - " + entry.getValue());
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
