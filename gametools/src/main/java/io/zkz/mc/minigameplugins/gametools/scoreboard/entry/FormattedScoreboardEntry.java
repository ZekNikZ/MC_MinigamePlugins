package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import org.bukkit.scoreboard.Team;

public abstract class FormattedScoreboardEntry extends ScoreboardEntry {
    private Team team;

    public FormattedScoreboardEntry(GameScoreboard scoreboard) {
        super(scoreboard);

        this.team = scoreboard.registerNewTeam();
    }

    protected abstract String getMainText();

    protected abstract String getPrefix();

    protected abstract String getSuffix();

    public void render(int pos) {
        try {
            this.team.setPrefix(this.getPrefix());
            this.team.setSuffix(this.getSuffix());
        } catch (IllegalStateException e) {
            Team t = this.scoreboard.registerNewTeam();
            t.setSuffix(this.getPrefix());
            t.setPrefix(this.getSuffix());
            this.team = t;
        }
        this.scoreboard.setString(pos, this.getMainText());
        this.scoreboard.setTeam(pos, this.team);
    }
}