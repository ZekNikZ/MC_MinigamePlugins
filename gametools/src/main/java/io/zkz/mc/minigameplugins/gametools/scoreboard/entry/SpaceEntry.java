package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

public class SpaceEntry extends ScoreboardEntry {
    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, "");
    }
}
