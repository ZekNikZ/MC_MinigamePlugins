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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class TeamScoresScoreboardEntry extends ScoreboardEntry implements IObserver<ScoreService> {
    private final GameTeam team;

    public TeamScoresScoreboardEntry(GameTeam team) {
        this.team = team;
        ScoreService.getInstance().addListener(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, "" + ChatColor.AQUA + ChatColor.BOLD + "Game Points: " + ChatColor.RESET + "(" + ChatColor.YELLOW + MinigameService.getInstance().getPointMultiplier() + "x" + ChatColor.RESET + ")");
        List<Map.Entry<GameTeam, Double>> entries = ScoreService.getInstance().getGameTeamScoreSummary().entrySet().stream()
            .sorted(Comparator.comparing(e -> ((Map.Entry<GameTeam, Double>) e).getValue()).reversed())
            .toList();
        final int placement = IntStream.range(0, entries.size())
            .filter(i -> entries.get(i).getKey().equals(this.team))
            .findFirst().orElse(0);
        final int beforeNum;
        final int afterNum;
        final boolean displayTeam;
        if (placement == 0) { // team is in first place
            beforeNum = 0;
            afterNum = 3;
            displayTeam = false;
        } else if (placement == entries.size() - 1) { // team is in last place
            beforeNum = 2;
            afterNum = 0;
            displayTeam = true;
        } else { // team is in the middle
            beforeNum = 1;
            afterNum = 1;
            displayTeam = true;
        }

        // Display first place
        this.displayScore(pos + 1, 0, entries.get(0));

        // Display rest
        AtomicInteger i = new AtomicInteger(1);
        entries.stream()
            .skip(placement - beforeNum)
            .limit(beforeNum + afterNum + 1 + (displayTeam ? 0 : 1))
            .forEach(entry -> {
                if (!displayTeam && entry.getKey().equals(team)) {
                    return;
                }

                this.displayScore(pos + 1 + i.get(), i.get(), entry);

                i.incrementAndGet();
            });
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
