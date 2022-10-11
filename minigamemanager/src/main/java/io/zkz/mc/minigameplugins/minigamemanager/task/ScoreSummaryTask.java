package io.zkz.mc.minigameplugins.minigamemanager.task;

import com.google.common.base.MoreObjects;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.ComponentUtils;
import io.zkz.mc.minigameplugins.gametools.util.StringUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;
import static net.kyori.adventure.audience.Audience.audience;

public class ScoreSummaryTask extends MinigameTask {
    public static final int NUM_SLIDES = 4;
    public static final int SECONDS_PER_SLIDE = 5;

    public int slideNum = 0;

    public ScoreSummaryTask() {
        super(SECONDS_PER_SLIDE * 20, SECONDS_PER_SLIDE * 20);
    }

    @Override
    public void run() {
        if (!MinigameService.getInstance().showScoreSummary()) {
            this.cancel();
            return;
        }

        Chat.sendMessage(mm(" "));
        switch (this.slideNum) {
            case 0 -> {
                Chat.sendMessage(mm("<bold>Top 5 players this game:"));
                this.displayScoreboard(
                    ScoreService.getInstance().getGamePlayerScoreSummary().entrySet().stream()
                        .map((playerId) -> {
                            Player player = Bukkit.getPlayer(playerId.getKey());
                            if (player == null) {
                                return null;
                            }
                            return new ScoreboardEntry(
                                player.displayName(),
                                playerId.getValue(),
                                MinigameService.getInstance().getPointMultiplier()
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList(),
                    5
                );
            }
            case 1 -> {
                Chat.sendMessage(mm("<bold>Best players on your team:"));
                MinigameService.getInstance().getGameTeams()
                    .forEach(team -> this.displayScoreboard(
                        TeamService.getInstance().getOnlineTeamMembers(team.id()),
                        ScoreService.getInstance().getGameTeamMemberScoreSummary(team).entrySet().stream()
                            .map((playerId) -> {
                                Player player = Bukkit.getPlayer(playerId.getKey());
                                if (player == null) {
                                    return null;
                                }
                                return new ScoreboardEntry(
                                    player.displayName(),
                                    playerId.getValue(),
                                    MinigameService.getInstance().getPointMultiplier()
                                );
                            })
                            .filter(Objects::nonNull)
                            .toList(),
                        10
                    ));
                TeamService.getInstance().getOnlineTeamMembers(DefaultTeams.SPECTATOR.id()).forEach(player -> Chat.sendMessage(player, mm("<gray><italic>You are a spectator.")));
                TeamService.getInstance().getOnlineTeamMembers(DefaultTeams.GAME_MASTER.id()).forEach(player -> Chat.sendMessage(player, mm("<gray><italic>You are a spectator.")));
                TeamService.getInstance().getOnlineTeamMembers(DefaultTeams.CASTER.id()).forEach(player -> Chat.sendMessage(player, mm("<gray><italic>You are a spectator.")));
            }
            case 2 -> {
                Chat.sendMessage(mm("<bold>The game results:"));
                Map<GameTeam, Double> teamScores = ScoreService.getInstance().getGameTeamScoreSummary();
                this.displayScoreboard(
                    MinigameService.getInstance().getGameTeams().stream()
                        .map(team -> new ScoreboardEntry(
                            team.getDisplayName(),
                            MoreObjects.firstNonNull(teamScores.get(team), 0.0),
                            MinigameService.getInstance().getPointMultiplier()
                        ))
                        .toList(),
                    16
                );
            }
            case 3 -> {
                Chat.sendMessage(mm("<bold>The current event standings:"));
                ScoreService.getInstance().loadAllData();
                Map<GameTeam, Double> teamScores = ScoreService.getInstance().getEventTeamScoreSummary();
                this.displayScoreboard(
                    MinigameService.getInstance().getGameTeams().stream()
                        .map(team -> new ScoreboardEntry(
                            team.getDisplayName(),
                            MoreObjects.firstNonNull(teamScores.get(team), 0.0),
                            1.0
                        ))
                        .toList(),
                    16
                );
            }
            default -> this.cancel();
        }

        ++slideNum;
    }

    private record ScoreboardEntry(Component name, double points, double multiplier) {
    }

    private void displayScoreboard(Collection<? extends Player> players, Collection<ScoreboardEntry> entries, int numToDisplay) {
        AtomicInteger placement = new AtomicInteger(1);
        entries.stream()
            .sorted(Comparator.comparing(ScoreboardEntry::points).reversed().thenComparing(ComponentUtils.comparing(ScoreboardEntry::name)))
            .limit(numToDisplay)
            .forEach(entry -> {
                Component placementStr = StringUtils.padOnLeftWithPixels(mm(placement.getAndIncrement() + ". "), 22);
                Component entryNameStr = StringUtils.padOnRightWithPixels(entry.name(), 160);
                Component pointsStr;
                if (entry.multiplier() == 1) {
                    pointsStr = StringUtils.padOnLeftWithPixels(mm(("%.1f" + ChatType.Constants.POINT_CHAR).formatted(entry.points())), 45);
                } else {
                    pointsStr = StringUtils.padOnLeftWithPixels(mm(("%.1f" + ChatType.Constants.POINT_CHAR).formatted(entry.points() * entry.multiplier())), 45)
                        .append(mm((" (%.1f" + ChatType.Constants.POINT_CHAR + " \u00d7 %.1f)").formatted(entry.points(), entry.multiplier())));
                }
                Chat.sendMessage(audience(players), placementStr.append(entryNameStr).append(pointsStr));
            });
        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
    }

    private void displayScoreboard(Collection<ScoreboardEntry> entries, int numToDisplay) {
        this.displayScoreboard(Bukkit.getOnlinePlayers(), entries, numToDisplay);
    }
}
