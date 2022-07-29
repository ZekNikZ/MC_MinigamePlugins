package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ValueEntry<T> extends FormattedScoreboardEntry {
    private final String prefix, suffix, mainText;
    private T value;
    private String format = null;

    public ValueEntry(GameScoreboard scoreboard, T value) {
        this(scoreboard, "", "%s", "", value);
    }

    public ValueEntry(GameScoreboard scoreboard, String mainText, T value) {
        this(scoreboard, "", mainText, "%s", value);
    }

    public ValueEntry(GameScoreboard scoreboard, String prefix, String mainText, String suffix, T value) {
        super(scoreboard);
        this.prefix = prefix;
        this.mainText = mainText;
        this.suffix = suffix;
        this.setValue(value);
    }

    public T getValue() {
        return this.value;
    }

    protected String getValueString() {
        return this.getValue().toString();
    }

    protected final String getColoredValueString() {
        if (this.format != null) {
            return this.format + this.getValueString() + ChatColor.RESET;
        } else {
            return this.getValueString();
        }
    }

    public void setValue(T value) {
        this.value = value;
        this.markDirty();
    }

    @Override
    protected String getMainText() {
        return this.mainText.formatted(this.getValueString());
    }

    @Override
    protected String getPrefix() {
        return this.prefix.formatted(this.getColoredValueString());
    }

    @Override
    protected String getSuffix() {
        return this.suffix.formatted(this.getColoredValueString());
    }

    public ValueEntry<T> setValueColor(ChatColor... color) {
        this.format = Arrays.stream(color).map(ChatColor::toString).collect(Collectors.joining(""));
        return this;
    }
}
