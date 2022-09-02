package io.zkz.mc.minigameplugins.gametools.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.SpaceEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Wrapper around a Bukkit scoreboard to allow additional functionality.
 */
public class GameScoreboard {
    private static final String[] INVISIBLE_STRINGS = new String[]{
        "\u00A70" + ChatColor.RESET,
        "\u00A71" + ChatColor.RESET,
        "\u00A72" + ChatColor.RESET,
        "\u00A73" + ChatColor.RESET,
        "\u00A74" + ChatColor.RESET,
        "\u00A75" + ChatColor.RESET,
        "\u00A76" + ChatColor.RESET,
        "\u00A77" + ChatColor.RESET,
        "\u00A78" + ChatColor.RESET,
        "\u00A79" + ChatColor.RESET,
        "\u00A7a" + ChatColor.RESET,
        "\u00A7b" + ChatColor.RESET,
        "\u00A7c" + ChatColor.RESET,
        "\u00A7d" + ChatColor.RESET,
        "\u00A7e" + ChatColor.RESET,
        "\u00A7f" + ChatColor.RESET,
    };

    private static int nextId = 0;
    private final int id;

    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<ScoreboardEntry> entries = new ArrayList<>();
    private final Map<String, ScoreboardEntry> mappedEntries = new HashMap<>();
    private final List<String> strings = new ArrayList<>(15);
    private String title;

    protected GameScoreboard(String title) {
        this.id = nextId++;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("display", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.setTitle(title);

        for (int i = 0; i < 15; i++) {
            strings.add(null);
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
        if (this.entries.size() >= 15) {
            throw new IndexOutOfBoundsException("A scoreboard can only have 15 entries.");
        }
        this.entries.add(entry);
        entry.setScoreboard(this);
        this.redraw();
        return entry;
    }

    public <T extends ScoreboardEntry> T addEntry(String id, T entry) {
        if (this.entries.size() >= 15) {
            throw new IndexOutOfBoundsException("A scoreboard can only have 15 entries.");
        }
        this.entries.add(entry);
        entry.setScoreboard(this);
        this.redraw();
        this.mappedEntries.put(id, entry);
        return entry;
    }

    public StringEntry addEntry(String entry) {
        return this.addEntry(new StringEntry(entry));
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
        this.addEntry(new SpaceEntry());
    }

    public void setString(int pos, String str) {
        if (pos < 0 || pos >= 15) {
            throw new IndexOutOfBoundsException("Scoreboard position must be between 0 and 15");
        }

        String existing = this.strings.get(pos);
        if (Objects.equals(existing, str)) {
            return;
        }

        if (str == null) {
            this.strings.set(pos, null);
            this.scoreboard.resetScores(INVISIBLE_STRINGS[pos]);
            return;
        }

        this.strings.set(pos, str);
        this.objective.getScore(INVISIBLE_STRINGS[pos]).setScore(15 - pos);

        Team team = this.scoreboard.getTeam("" + pos);
        if (team == null) {
            this.setupTeam(pos);
            team = this.scoreboard.getTeam("" + pos);
            team.addEntry(INVISIBLE_STRINGS[pos]);
        }

        team.setSuffix(str);
    }

    public @Nullable ScoreboardEntry getEntry(String id) {
        return this.mappedEntries.get(id);
    }

    private void setupTeam(int pos) {
        Team existingTeam = this.scoreboard.getTeam("" + pos);
        if (existingTeam != null) {
            existingTeam.unregister();
        }

        this.scoreboard.registerNewTeam("" + pos);
    }

    public void removeEntry(String id) {
        ScoreboardEntry scoreboardEntry = this.mappedEntries.get(id);
        if (scoreboardEntry == null) {
            return;
        }

        scoreboardEntry.cleanup();
        this.entries.remove(scoreboardEntry);
        this.mappedEntries.remove(id);
        this.clear();
        this.redraw();
    }

    private void clear() {
        for (int i = 0; i < 15; i++) {
            this.setString(i, null);
        }
    }

    public ScoreboardEntry swapEntry(String id, ScoreboardEntry newEntry) {
        ScoreboardEntry oldEntry = this.mappedEntries.get(id);
        if (oldEntry == null) {
            throw new NullPointerException("No entry with id " + id);
        }

        oldEntry.cleanup();
        int i = this.entries.indexOf(oldEntry);
        this.entries.remove(i);
        this.mappedEntries.remove(id);
        this.entries.add(i, newEntry);
        this.mappedEntries.put(id, newEntry);
        return oldEntry;
    }

    public int getId() {
        return this.id;
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
