package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventTeamScoresScoreboardEntry extends ScoreboardEntry implements IObserver<ScoreService> {
    public EventTeamScoresScoreboardEntry() {
        ScoreService.getInstance().addListener(this);
    }

    @Override
    public void render(int pos) {
        // Header
        this.getScoreboard().setString(pos, "" + ChatColor.AQUA + ChatColor.BOLD + "Event Points: " + ChatColor.RESET + "(" + ChatColor.YELLOW + ScoreService.getInstance().getMultiplier() + "x" + ChatColor.RESET + ")");

        // Get team placements
        Map<GameTeam, Double> scores = this.getTeams().stream().collect(Collectors.toMap(team -> team, team -> 0.0));
        ScoreService.getInstance().getEventTeamScoreSummary().forEach((team, score) -> {
            if (scores.containsKey(team)) {
                scores.put(team, score);
            }
        });
        List<Map.Entry<GameTeam, Double>> entries = scores.entrySet().stream()
            .sorted(Comparator.comparing((Function<Map.Entry<GameTeam, Double>, Double>) Map.Entry::getValue).reversed().thenComparing(entry -> entry.getKey().getDisplayName()))
            .toList();
        int placement = 0;
        for (var entry : entries) {
            this.displayScore(pos + 1 + placement, placement, entry);
            ++placement;
        }
    }

    private Collection<GameTeam> getTeams() {
        return TeamService.getInstance().getAllNonSpectatorTeams();
    }

    private void displayScore(int scoreboardPos, int placement, Map.Entry<GameTeam, Double> entry) {
        String placementStr = StringUtils.padOnLeftWithPixels("" + (placement + 1) + ". ", 20);
        String nameStr = StringUtils.padOnRightWithPixels(entry.getKey().getDisplayName(), 100);
        String pointsStr = StringUtils.padOnLeftWithPixels("" + (int) (double) entry.getValue() + ChatType.Constants.POINT_CHAR, 45);
        this.getScoreboard().setString(scoreboardPos, placementStr + nameStr + pointsStr);
    }

    @Override
    public int getRowCount() {
        return 1 + this.getTeams().size();
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
