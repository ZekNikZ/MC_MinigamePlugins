package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.util.BlockUtils;
import io.zkz.mc.minigameplugins.gametools.util.GTColor;
import io.zkz.mc.minigameplugins.gametools.util.GTColors;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public record GameTeam(String id, Component name, Component prefix, String formatTag, GTColor color,
                       NamedTextColor scoreboardColor, boolean spectator) implements ForwardingAudience {
    public static final class Builder {
        private final String id;
        private final Component name;
        private Component prefix = mm("");
        private String formatTag = "";
        private GTColor color = GTColors.WHITE;
        private NamedTextColor scoreboardColor = NamedTextColor.WHITE;
        private boolean spectator = false;

        private Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder prefix(Component prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder formatTag(String formatTag) {
            this.formatTag = formatTag;
            return this;
        }

        public Builder color(GTColor color) {
            this.color = color;
            return this;
        }

        public Builder scoreboardColor(NamedTextColor scoreboardColor) {
            this.scoreboardColor = scoreboardColor;
            return this;
        }

        public Builder spectator(boolean spectator) {
            this.spectator = spectator;
            return this;
        }

        public GameTeam build() {
            return new GameTeam(this.id, this.name, this.prefix, this.formatTag, this.color, this.scoreboardColor, this.spectator);
        }
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public Component getDisplayName() {
        return mm(this.formatTag() + "<0> <1>", this.prefix(), this.name());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTeam gameTeam = (GameTeam) o;
        return Objects.equals(id, gameTeam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
