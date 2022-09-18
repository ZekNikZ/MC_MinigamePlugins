package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.util.BlockUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class GameTeam implements ForwardingAudience {
    private String id;
    private Component name;
    private Component prefix;
    private String formatTag;
    private Color color;
    private NamedTextColor scoreboardColor;
    private boolean spectator;

    public GameTeam() {
        this.formatTag = "";
        this.color = Color.WHITE;
        this.spectator = false;
    }

    public GameTeam(String id, Component name, Component prefix) {
        this();
        this.prefix = prefix;
        this.setId(id)
            .setName(name);
    }

    public String getFormatTag() {
        return this.formatTag;
    }

    public GameTeam setFormatTag(String formatCode) {
        this.formatTag = formatCode;
        ScoreboardService.getInstance().setupGlobalTeams();
        return this;
    }

    public String getId() {
        return id;
    }

    private GameTeam setId(String id) {
        this.id = id;
        return this;
    }

    public Component getName() {
        return this.name;
    }

    public Component getDisplayName() {
        return mm(this.getFormatTag() + "<0> <1>", this.getPrefix(), this.getName());
    }

    public GameTeam setScoreboardColor(NamedTextColor color) {
        this.scoreboardColor = color;
        return this;
    }

    public NamedTextColor getScoreboardColor() {
        return this.scoreboardColor;
    }

    GameTeam setName(Component name) {
        this.name = name;
        ScoreboardService.getInstance().setupGlobalTeams();
        return this;
    }

    public Component getPrefix() {
        return this.prefix;
    }

    GameTeam setPrefix(Component prefix) {
        this.prefix = prefix;
        ScoreboardService.getInstance().setupGlobalTeams();
        return this;
    }

    public Color getColor() {
        return this.color;
    }

    GameTeam setColor(Color color) {
        this.color = color;
        return this;
    }

    public boolean isSpectator() {
        return this.spectator;
    }

    public GameTeam setSpectator(boolean spectator) {
        this.spectator = spectator;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameTeam gameTeam)) return false;
        return Objects.equals(getId(), gameTeam.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Material getWoolColor() {
        return BlockUtils.getWoolColor(this.scoreboardColor);
    }

    public Material getConcreteColor() {
        return BlockUtils.getConcreteColor(this.scoreboardColor);
    }

    public void removeAllMembers() {
        TeamService.getInstance().clearTeam(this.id);
    }

    public void addMember(Player player) {
        TeamService.getInstance().joinTeam(player, this);
    }

    public void addMember(UUID playerId) {
        TeamService.getInstance().joinTeam(playerId, this.id);
    }

    public boolean contains(Player player) {
        return this.equals(TeamService.getInstance().getTeamOfPlayer(player));
    }

    public boolean contains(UUID playerId) {
        return this.equals(TeamService.getInstance().getTeamOfPlayer(playerId));
    }

    public Collection<UUID> getAllMembers() {
        return TeamService.getInstance().getTeamMembers(this);
    }

    public Collection<Player> getAllOnlineMembers() {
        return TeamService.getInstance().getOnlineTeamMembers(this);
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return this.getAllOnlineMembers();
    }
}
