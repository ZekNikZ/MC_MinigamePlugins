package io.zkz.mc.minigameplugins.minigamemanager.minigame;

import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.state.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Round {
    private String mapName;
    private String mapBy;
    private final Set<UUID> alivePlayers = new HashSet<>();

    protected Round() {
        this.mapName = null;
        this.mapBy = null;
    }

    protected Round(String mapName) {
        this.mapName = mapName;
        this.mapBy = null;
    }

    protected Round(String mapName, String mapBy) {
        this.mapName = mapName;
        this.mapBy = mapBy;
    }

    /**
     * Run when round is selected. Designed for setting up spawnpoints, etc.
     */
    public void onSetup() {

    }

    /**
     * Run when round is deselected. Designed for saving scores, cleaning up the arena, etc.
     */
    public void onCleanup() {

    }

    /**
     * Run when round has begun (i.e., when players can start moving).
     */
    public void onRoundStart() {

    }

    public void onPhase1End() {

    }

    public void onPhase2Start() {

    }

    public void onPhase2End() {

    }

    public void onPhase3Start() {

    }

    /**
     * Run when game enters pre-round phase.
     */
    protected void onEnterPreRound() {

    }

    public final void enterPreRound() {
        this.alivePlayers.addAll(this.getInitialAlivePlayers());
        this.onEnterPreRound();
    }

    /**
     * Run when game enters post-round phase.
     */
    public void onEnterPostRound() {

    }

    /**
     * Run when game is paused.
     */
    public void onPause() {

    }

    /**
     * Run when game is unpaused.
     */
    public void onUnpause() {

    }

    /**
     * Run when timer ticks
     *
     * @param currentTimeMillis value of the timer at this point in time
     */
    public void onPreRoundTimerTick(long currentTimeMillis) {

    }

    protected Collection<UUID> getInitialAlivePlayers() {
        return MinigameService.getInstance().getMinigame().getParticipants();
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

    public Map<GameTeam, Long> getAliveTeams() {
        return this.getAlivePlayers().stream()
            .collect(Collectors.groupingBy(playerId -> TeamService.getInstance().getTeamOfPlayer(playerId), Collectors.counting()));
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

    /**
     * Sets up player location and gamemode.
     *
     * @param player the player to set up
     */
    public final void setupPlayer(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            this.setupPlayer(player);
        }
    }

    /**
     * Convenience method to end a round.
     */
    public void triggerRoundEnd() {
        MinigameService.getInstance().endRound();
    }

    /**
     * Convenience method to end the first phase.
     */
    public void triggerPhase1End() {
        MinigameService.getInstance().endPhase1();
    }

    /**
     * Convenience method to start the second phase.
     */
    public void triggerPhase2Start() {
        MinigameService.getInstance().startPhase2();
    }

    /**
     * Convenience method to end the second phase.
     */
    public void triggerPhase2End() {
        MinigameService.getInstance().endPhase2();
    }

    /**
     * Convenience method to start the third phase.
     */
    public void triggerPhase3Start() {
        MinigameService.getInstance().startPhase3();
    }

    public @Nullable String getMapName() {
        return this.mapName;
    }

    public @Nullable String getMapBy() {
        return this.mapBy;
    }

    public void setMapName(@Nullable String mapName) {
        this.mapName = mapName;
    }

    protected void setMapBy(String author) {
        this.mapBy = author;
    }
}
