package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import org.apache.commons.lang.StringUtils;

public class SpaceEntry extends ScoreboardEntry {
    public SpaceEntry(GameScoreboard scoreboard) {
        super(scoreboard);
    }

    @Override
    public void render(int pos) {
        this.scoreboard.setString(pos, StringUtils.repeat(" ", pos));
    }
}
