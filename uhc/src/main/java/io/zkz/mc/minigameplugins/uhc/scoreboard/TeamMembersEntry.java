package io.zkz.mc.minigameplugins.uhc.scoreboard;

import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class TeamMembersEntry extends ScoreboardEntry {
    private final GameTeam team;

    public TeamMembersEntry(GameTeam team) {
        this.team = team;
    }

    @Override
    public void render(int pos) {
        AtomicInteger i = new AtomicInteger();
        this.team.getAllMembers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Player::getName))
            .forEach(player -> {
                if (MinigameService.getInstance().getCurrentRound().isAlive(player)) {
                    this.getScoreboard().setLine(pos + i.get(), mm("<0> - <legacy_red><1> \u2764</legacy_red>", player.displayName(), Component.text(Math.ceil(player.getHealth() + player.getAbsorptionAmount()) / 2.0)));
                } else {
                    this.getScoreboard().setLine(pos + i.get(), mm("<0> - <legacy_dark_red>\u2620</legacy_dark_red>", player.displayName()));
                }
                i.getAndIncrement();
            });
    }

    @Override
    public int getRowCount() {
        return team.getAllMembers().size();
    }
}
