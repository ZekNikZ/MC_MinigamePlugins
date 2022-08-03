package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;
public class ValueEntry<T> extends ScoreboardEntry {
    private final String format;
    private T value;

    public ValueEntry(String format, T initialValue) {
        this.format = format;
        this.value = initialValue;
    }

    public void setValue(T value) {
        this.value = value;
        this.markDirty();
    }

    public final T getValue() {
        return this.value;
    }

    protected String getValueString() {
        return this.getValue().toString();
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, this.format.formatted(this.getValueString()));
    }
}
