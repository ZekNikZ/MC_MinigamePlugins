package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.minigamemanager.score.ScoreEntry;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class ScoreService extends MinigameManagerService {
    private static final ScoreService INSTANCE = new ScoreService();

    public static ScoreService getInstance() {
        return INSTANCE;
    }

    public void earnPoints(UUID playerId, String reason, float points) {
        // TODO: implement
        ScoreEntry entry = new ScoreEntry(playerId, ChatConstantsService.getInstance().getMinigameName(), MinigameService.getInstance().getCurrentRoundIndex(), reason, points);
    }

    public void earnPoints(Player player, String reason, float points) {
        this.earnPoints(player.getUniqueId(), reason, points);
    }

    public void earnPointsUUID(Collection<UUID> playerIds, String reason, float points) {
        playerIds.forEach(playerId -> this.earnPoints(playerId, reason, points));
    }

    public void earnPoints(Collection<? extends Player> players, String reason, float points) {
        players.forEach(player -> this.earnPoints(player, reason, points));
    }
}
