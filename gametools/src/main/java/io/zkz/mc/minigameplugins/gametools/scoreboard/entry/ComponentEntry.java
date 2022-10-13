package io.zkz.mc.minigameplugins.gametools.scoreboard.entry;

import net.kyori.adventure.text.Component;

public class ComponentEntry extends ScoreboardEntry {
    private final Component component;

    public ComponentEntry(Component component) {
        this.component = component;
    }

    @Override
    public void render(int pos) {
        this.getScoreboard().setLine(pos, this.component);
    }
}
