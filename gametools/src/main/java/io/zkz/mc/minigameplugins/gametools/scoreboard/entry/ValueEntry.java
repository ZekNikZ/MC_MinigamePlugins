package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import org.bukkit.scoreboard.Team;

public class ValueEntry<T> extends ScoreboardEntry {
    public enum ValuePos {
        AFTER_BASE,
        BEFORE_BASE
    }

    private String baseStr;
    private T value;
    private final ValuePos valuePos;
    private Team team;

    // TODO: I'm quite certain that this won't work as intended
    public ValueEntry(GameScoreboard scoreboard, T value) {
        this(scoreboard, "", ValuePos.AFTER_BASE, value);
    }

    public ValueEntry(GameScoreboard scoreboard, String baseStr, T value) {
        this(scoreboard, baseStr, ValuePos.AFTER_BASE, value);
    }

    public ValueEntry(GameScoreboard scoreboard, String baseStr, ValuePos valuePos, T value) {
        super(scoreboard);
        this.team = this.scoreboard.registerNewTeam();
        this.valuePos = valuePos;
        this.setBaseStr(baseStr);
        this.setValue(value);
    }

    protected String getBaseStr() {
        return baseStr;
    }

    public void setBaseStr(String baseStr) {
        this.baseStr = baseStr;
        this.markDirty();
    }

    public T getValue() {
        return value;
    }

    protected String getValueString() {
        return this.getValue().toString();
    }

    public void setValue(T value) {
        try {
            this.value = value;
            if (this.valuePos == ValuePos.BEFORE_BASE) {
                this.team.setPrefix(this.getValueString());
            } else {
                this.team.setSuffix(this.getValueString());
            }
            this.markDirty();
        } catch (IllegalStateException e) {
            Team t = this.scoreboard.registerNewTeam();
            t.setSuffix(this.team.getSuffix());
            t.setPrefix(this.team.getPrefix());
            this.team = t;
            this.setValue(value);
        }
    }

    @Override
    public void render(int pos) {
        this.scoreboard.setString(pos, this.baseStr);
        Team t = this.scoreboard.setTeam(pos, this.team);
        if (t != null) {
            this.team = t;
        }
    }
}
