package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;

public abstract class ScoreboardEntry {
    private GameScoreboard scoreboard;

    public abstract void render(int pos);

    public int getRowCount() {
        return 1;
    }

    protected void markDirty() {
        this.scoreboard.redraw();
    }

    public void setScoreboard(GameScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    protected final GameScoreboard getScoreboard() {
        if (this.scoreboard == null) {
            throw new IllegalStateException("This entry is not part of a scoreboard");
        }

        return this.scoreboard;
    }

    public void cleanup() {}
}
