package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class SpaceEntry extends ScoreboardEntry {
    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, mm(""));
    }
}
