package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

public class StringEntry extends ScoreboardEntry {
    private final String str;

    public StringEntry(String str) {
        this.str = str;
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, this.str);
    }
}
