package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;

import java.util.Objects;

public abstract class ScoreboardEntry {
    private static int nextId;
    private final int thisId = nextId++; // NOSONAR java:S1170

    private GameScoreboard scoreboard;

    public abstract void render(int pos);

    @SuppressWarnings("java:S3400")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreboardEntry that = (ScoreboardEntry) o;
        return this.thisId == that.thisId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.thisId);
    }
}
