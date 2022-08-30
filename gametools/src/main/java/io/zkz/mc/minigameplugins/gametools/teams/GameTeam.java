package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.util.BlockUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scoreboard.Team;

import java.awt.*;
import java.util.Objects;

public class GameTeam {
    private String id;
    private String name;
    private String prefix;
    private String formatCode;
    private Color color;
    private org.bukkit.ChatColor scoreboardColor;
    private boolean spectator;

    public GameTeam() {
        this.formatCode = "" + ChatColor.RESET;
        this.color = Color.WHITE;
        this.spectator = false;
    }

    public GameTeam(String id, String name, String prefix) {
        this();
        this.prefix = prefix;
        this.setId(id)
            .setName(name);
    }

    public String getFormatCode() {
        return this.formatCode;
    }

    public GameTeam setFormatCode(ChatColor formatCode) {
        return this.setFormatCode(formatCode.toString());
    }

    public GameTeam setFormatCode(String formatCode) {
        this.formatCode = formatCode;
        this.updateMinecraftTeam();
        return this;
    }

    public String getId() {
        return id;
    }

    private GameTeam setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.formatCode + this.prefix + " " + this.name + ChatColor.RESET;
    }

    public GameTeam setScoreboardColor(org.bukkit.ChatColor color) {
        this.scoreboardColor = color;
        return this;
    }

    public org.bukkit.ChatColor getScoreboardColor() {
        return this.scoreboardColor;
    }

    GameTeam setName(String name) {
        this.name = name;
        this.updateMinecraftTeam();
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    GameTeam setPrefix(String prefix) {
        this.prefix = prefix;
        this.updateMinecraftTeam();
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

    private void updateMinecraftTeam() {
        Team scoreboardTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(this.getId());
        if (scoreboardTeam == null) {
            return;
        }
        scoreboardTeam.setPrefix("" + this.getFormatCode() + this.getPrefix() + ChatColor.RESET + this.getScoreboardColor() + " ");
        scoreboardTeam.setColor(this.getScoreboardColor() != null ? this.getScoreboardColor() : org.bukkit.ChatColor.WHITE);
        scoreboardTeam.setSuffix("" + ChatColor.RESET);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setAllowFriendlyFire(TeamService.getInstance().getFriendlyFire());
        scoreboardTeam.setOption(Team.Option.COLLISION_RULE, TeamService.getInstance().getCollisionRule());
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
}
