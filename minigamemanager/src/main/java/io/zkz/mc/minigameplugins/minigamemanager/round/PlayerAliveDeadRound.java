package io.zkz.mc.minigameplugins.minigamemanager.round;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;

public class PlayerAliveDeadRound extends Round {
    public enum PlayerState {
        DEAD,
        ALIVE,
        SPEC
    }

    private final Set<UUID> alivePlayers = new HashSet<>();

    protected PlayerAliveDeadRound() {
        super();
    }

    protected PlayerAliveDeadRound(String mapName) {
        super(mapName);
    }

    protected PlayerAliveDeadRound(String mapName, String mapBy) {
        super(mapName, mapBy);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onEnterPreRound() {
        this.alivePlayers.addAll(this.getInitialAlivePlayers());
    }

    protected Collection<UUID> getInitialAlivePlayers() {
        return MinigameService.getInstance().getPlayers();
    }

    protected void onPlayerDeath(UUID playerId) {
    }

    protected void onPlayerRespawn(UUID playerId) {
    }

    protected void onPlayerSetup(Player player, PlayerState playerState) {
    }

    public void setDead(Player player) {
        this.setDead(player.getUniqueId());
    }

    public void setDead(UUID playerId) {
        this.alivePlayers.remove(playerId);
        this.onPlayerDeath(playerId);
    }

    public void setAlive(Player player) {
        this.setAlive(player);
    }

    public void setAlive(UUID playerId) {
        this.alivePlayers.add(playerId);
        this.onPlayerRespawn(playerId);
    }

    public Collection<UUID> getAlivePlayers() {
        return this.alivePlayers;
    }

    public Collection<? extends Player> getOnlineAlivePlayers() {
        return this.alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    /**
     * Sets up player location and gamemode.
     *
     * @param player the player to set up
     */
    public final void setupPlayer(Player player) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (team != null && team.spectator()) {
            this.onPlayerSetup(player, PlayerState.SPEC);
        } else {
            this.onPlayerSetup(player, this.isAlive(player) ? PlayerState.ALIVE : PlayerState.DEAD);
        }
    }

    public boolean isAlive(Player player) {
        return this.isAlive(player.getUniqueId());
    }

    public boolean isAlive(UUID playerId) {
        return this.getAlivePlayers().contains(playerId);
    }

    public boolean isTeamAlive(GameTeam team) {
        return this.alivePlayers.stream().anyMatch(playerId -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(playerId), team));
    }

    public boolean isTeamAlive(Player player) {
        return this.isTeamAlive(TeamService.getInstance().getTeamOfPlayer(player));
    }
}
