package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.IObservable;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.minigamemanager.MinigameManagerPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.score.ScoreEntry;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreService extends PluginService<MinigameManagerPlugin> implements IObservable {
    private static final ScoreService INSTANCE = new ScoreService();

    public static ScoreService getInstance() {
        return INSTANCE;
    }

    private final List<ScoreEntry> entries = new ArrayList<>();

    private final Map<UUID, Double> roundPlayerScores = new HashMap<>();
    private final Map<GameTeam, Double> roundTeamScores = new HashMap<>();

    private final Map<UUID, Double> gamePlayerScores = new HashMap<>();
    private final Map<GameTeam, Double> gameTeamScores = new HashMap<>();

    public void earnPoints(UUID playerId, String reason, double points) {
        ScoreEntry entry = new ScoreEntry(playerId, ChatConstantsService.getInstance().getMinigameName(), MinigameService.getInstance().getCurrentRoundIndex(), reason, points, MinigameService.getInstance().getPointMultiplier());
        this.entries.add(entry);

        // Player score
        this.roundPlayerScores.putIfAbsent(playerId, 0.0);
        this.gamePlayerScores.putIfAbsent(playerId, 0.0);
        this.roundPlayerScores.compute(playerId, (p, s) -> s + points);
        this.gamePlayerScores.compute(playerId, (p, s) -> s + points);

        // Team score
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(playerId);
        this.roundTeamScores.compute(team, (t, s) -> s + points);
        this.gameTeamScores.compute(team, (t, s) -> s + points);

        this.notifyObservers();
    }

    public void earnPoints(GameTeam team, @Nullable String reason, double points) {
        ScoreEntry entry = new ScoreEntry(new UUID(0,0), ChatConstantsService.getInstance().getMinigameName(), MinigameService.getInstance().getCurrentRoundIndex(), reason != null ? reason : "", points, MinigameService.getInstance().getPointMultiplier());
        this.entries.add(entry);

        // Team score
        this.roundTeamScores.compute(team, (t, s) -> s + points);
        this.gameTeamScores.compute(team, (t, s) -> s + points);

        this.notifyObservers();
    }

    public void earnPoints(Player player, String reason, double points) {
        this.earnPoints(player.getUniqueId(), reason, points);
    }

    public void earnPointsUUID(Collection<UUID> playerIds, String reason, double points) {
        playerIds.forEach(playerId -> this.earnPoints(playerId, reason, points));
    }

    public void earnPoints(Collection<? extends Player> players, String reason, double points) {
        players.forEach(player -> this.earnPoints(player, reason, points));
    }

    public Map<UUID, Double> getRoundPlayerScoreSummary() {
        return this.roundPlayerScores;
    }

    public List<ScoreEntry> getRoundEntries(Player player) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .filter(entry -> Objects.equals(entry.playerId(), player.getUniqueId()))
            .toList();
    }

    public Map<UUID, Double> getRoundTeamMemberScoreSummary(GameTeam team) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .filter(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()) == team)
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<GameTeam, Double> getRoundTeamScoreSummary() {
        return this.roundTeamScores;
    }

    public Map<UUID, Double> getGamePlayerScoreSummary() {
        return this.gamePlayerScores;
    }

    public List<ScoreEntry> getGameEntries(Player player) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .filter(entry -> Objects.equals(entry.playerId(), player.getUniqueId()))
            .toList();
    }

    public Map<UUID, Double> getGameTeamMemberScoreSummary(GameTeam team) {
        // TODO: probably not very efficient to be computing this so often, cache it
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()) == team)
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<GameTeam, Double> getGameTeamScoreSummary() {
        return this.gameTeamScores;
    }

    public Map<GameTeam, Double> getEventTeamScoreSummary() {
        // TODO: implement properly
        return this.getGameTeamScoreSummary();
    }

    @SuppressWarnings("rawtypes")
    private final List<IObserver> listeners = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    @Override
    public void addListener(IObserver observer) {
        this.listeners.add(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void removeListener(IObserver observer) {
        this.listeners.remove(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<IObserver> getListeners() {
        return this.listeners;
    }

    @EventHandler
    private void onStateChange(StateChangeEvent event) {
        if (event.getNewState() == MinigameState.PRE_ROUND) {
            this.resetRoundScores();
        } else if (event.getNewState() == MinigameState.SETUP) {
            this.resetGameScores();
        }
    }

    private void resetRoundScores() {
        this.roundPlayerScores.clear();

        this.roundTeamScores.clear();
        MinigameService.getInstance().getGameTeams().forEach(team -> this.roundTeamScores.put(team, 0.0));

    }

    private void resetGameScores() {
        this.gamePlayerScores.clear();

        this.gameTeamScores.clear();
        MinigameService.getInstance().getGameTeams().forEach(team -> this.gameTeamScores.put(team, 0.0));
    }
}
