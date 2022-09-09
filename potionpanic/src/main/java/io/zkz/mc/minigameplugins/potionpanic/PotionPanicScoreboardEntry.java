package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;

import java.util.Map;

public class PotionPanicScoreboardEntry extends ScoreboardEntry implements IObserver<PotionPanicService> {
    private static final char NO_POINT_CHAR = '\u25A1';
    private static final char POINT_CHAR = '\u25A0';

    @Override
    public void render(int pos) {
        Map<GameTeam, Integer> wins = PotionPanicService.getInstance().getWins();
        GameTeam team1 = PotionPanicService.getInstance().getTeam1();
        GameTeam team2 = PotionPanicService.getInstance().getTeam2();

        this.displayScore(pos, team1, wins.get(team1));
        this.displayScore(pos + 1, team2, wins.get(team2));
    }

    private void displayScore(int scoreboardPos, GameTeam team, int wins) {
        String nameStr = StringUtils.padOnRightWithPixels(team.getDisplayName(), 90);

        String s = org.apache.commons.lang.StringUtils.repeat(" " + POINT_CHAR, wins) + org.apache.commons.lang.StringUtils.repeat(" " + NO_POINT_CHAR, 3 - wins);
        String pointsStr = StringUtils.padOnRightWithPixels(s, 45);

        this.getScoreboard().setString(scoreboardPos, nameStr + pointsStr);
    }

    @Override
    public int getRowCount() {
        return 2;
    }

    @Override
    public void handleChanged(PotionPanicService observable) {
        this.markDirty();
    }

    @Override
    public void cleanup() {
        ScoreService.getInstance().removeListener(this);
    }
}
