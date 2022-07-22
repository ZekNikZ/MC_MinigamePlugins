package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;

public class StringEntry extends ScoreboardEntry {
    private final String str;

    public StringEntry(GameScoreboard scoreboard, String str) {
        super(scoreboard);
        this.str = str;
    }

    @Override
    public void render(int pos) {
        this.scoreboard.setString(pos, this.str);
    }
}
