package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmResolve;

public class TimerEntry extends ValueEntry<AbstractTimer> {
    private final int hookId;
    private final Component label;

    public TimerEntry(String format, Component label, AbstractTimer value) {
        super(format, value);
        this.label = label;

        this.hookId = this.getValue().addHook(this::markDirty);
    }

    @Override
    public void setValue(AbstractTimer value) {
        throw new UnsupportedOperationException("Cannot set the value of a timer entry");
    }

    @Override
    public void cleanup() {
        this.getValue().removeHook(this.hookId);
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().setLine(pos, mmResolve(this.getFormat(),
            Placeholder.component("label", this.label),
            Placeholder.component("value", this.getValueComponent())
        ));
    }
}
