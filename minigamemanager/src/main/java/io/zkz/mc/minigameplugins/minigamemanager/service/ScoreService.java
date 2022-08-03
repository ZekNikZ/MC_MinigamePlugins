package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.MinigameManagerPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.score.ScoreEntry;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreService extends PluginService<MinigameManagerPlugin> {
    private static final ScoreService INSTANCE = new ScoreService();

    public static ScoreService getInstance() {
        return INSTANCE;
    }

    private final List<ScoreEntry> entries = new ArrayList<>();

    public void earnPoints(UUID playerId, String reason, double points) {
        ScoreEntry entry = new ScoreEntry(playerId, ChatConstantsService.getInstance().getMinigameName(), MinigameService.getInstance().getCurrentRoundIndex(), reason, points);
        this.entries.add(entry);
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
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
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
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .collect(Collectors.groupingBy(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()), Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<UUID, Double> getGamePlayerScoreSummary() {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public List<ScoreEntry> getGameEntries(Player player) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> entry.round() == MinigameService.getInstance().getCurrentRoundIndex())
            .filter(entry -> Objects.equals(entry.playerId(), player.getUniqueId()))
            .toList();
    }

    public Map<UUID, Double> getGameTeamMemberScoreSummary(GameTeam team) {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .filter(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()) == team)
            .collect(Collectors.groupingBy(ScoreEntry::playerId, Collectors.summingDouble(ScoreEntry::points)));
    }

    public Map<GameTeam, Double> getGameTeamScoreSummary() {
        return this.entries.stream()
            .filter(entry -> entry.minigame().equals(ChatConstantsService.getInstance().getMinigameName()))
            .collect(Collectors.groupingBy(entry -> TeamService.getInstance().getTeamOfPlayer(entry.playerId()), Collectors.summingDouble(ScoreEntry::points)));
    }
}
