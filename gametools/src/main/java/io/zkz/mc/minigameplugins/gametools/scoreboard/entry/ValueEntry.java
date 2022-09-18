package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmResolve;

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

    protected Component getValueComponent() {
        return Component.text(this.getValue().toString());
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().setString(pos, mmResolve(this.format, Placeholder.component("value", this.getValueComponent())));
    }
}
