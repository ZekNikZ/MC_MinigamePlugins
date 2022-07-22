package io.zkz.mc.minigameplugins.gametools.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.SpaceEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper around a Bukkit scoreboard to allow additional functionality.
 */
public class GameScoreboard {
    private static int nextId = 0;
    private final int id;

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<ScoreboardEntry> entries = new ArrayList<>();
    private final List<String> strings = new ArrayList<>(15);
    private final List<Team> teams = new ArrayList<>(15);
    private int teamCounter = 0;
    private String title;

    protected GameScoreboard(String title) {
        this.id = nextId++;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("display", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.setTitle(title);

        for (int i = 0; i < 15; i++) {
            strings.add(null);
            teams.add(null);
        }

        this.redraw();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.objective.setDisplayName(title);
    }

    public <T extends ScoreboardEntry> T addEntry(T entry) {
        this.entries.add(entry);
        this.redraw();
        return entry;
    }

    public StringEntry addEntry(String entry) {
        StringEntry se = new StringEntry(this, entry);
        this.entries.add(se);
        this.redraw();
        return se;
    }

    public void redraw() {
        int pos = 0;
        for (ScoreboardEntry entry : this.entries) {
            entry.render(pos);
            pos += entry.getRowCount();
        }
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void addSpace() {
        this.addEntry(new SpaceEntry(this));
    }

    public void setString(int pos, String str) {
        String existing = this.strings.get(pos);
        if (pos >= 15 || Objects.equals(existing, str)) {
            return;
        }

        if (existing != null) {
            this.scoreboard.resetScores(existing);
        }

        this.strings.set(pos, str);
        this.objective.getScore(str).setScore(15 - pos);

        if (this.teams.get(pos) != null) {
            this.teams.get(pos).addEntry(str);
        }
    }

    public Team setTeam(int pos, Team team) {
        if (pos >= 15) {
            return null;
        }

        this.teams.set(pos, team);

        if (this.strings.get(pos) != null) {
            if (this.scoreboard.getTeam(team.getName()) == null) {
                Team newTeam = this.scoreboard.registerNewTeam(team.getName());
                newTeam.setSuffix(team.getSuffix());
                newTeam.setPrefix(team.getPrefix());
                newTeam.setDisplayName(team.getDisplayName());
                return newTeam;
            } else {
                team.addEntry(this.strings.get(pos));
                return null;
            }
        }

        return null;
    }

    public int getId() {
        return this.id;
    }

    public Team registerNewTeam() {
        return this.scoreboard.registerNewTeam("" + (teamCounter++));
    }

    public void unregisterTeam(Team team) {
        team.unregister();
    }

    public void cleanup() {
        this.scoreboard.getTeams().forEach(this::unregisterTeam);
        this.entries.forEach(ScoreboardEntry::cleanup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameScoreboard that = (GameScoreboard) o;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return this.getId();
    }
}
