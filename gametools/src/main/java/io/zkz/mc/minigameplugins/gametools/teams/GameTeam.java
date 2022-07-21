package io.zkz.mc.minigameplugins.gametools.teams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import java.awt.*;
import java.util.Objects;

public class GameTeam {
    private String id;
    private String name;

    private String prefix;
    private ChatColor formatCode;
    private Color color;

    public GameTeam() {
        this.formatCode = ChatColor.RESET;
    }

    public GameTeam(String id, String name, String prefix) {
        this();
        this.prefix = prefix;
        this.setId(id);
        this.setName(name);
    }

    public ChatColor getFormatCode() {
        return this.formatCode;
    }

    public void setFormatCode(ChatColor formatCode) {
        this.formatCode = formatCode;
        this.updateMinecraftTeam();
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.updateMinecraftTeam();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.updateMinecraftTeam();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    private void updateMinecraftTeam() {
        Team scoreboardTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(this.getId());
        if (scoreboardTeam == null) {
            return;
        }
        scoreboardTeam.setPrefix("" + this.getFormatCode() + ChatColor.BOLD + this.getPrefix() + ChatColor.RESET + this.getFormatCode() + " ");
        scoreboardTeam.setSuffix("" + ChatColor.RESET);
        scoreboardTeam.setDisplayName(this.getName());
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
}
