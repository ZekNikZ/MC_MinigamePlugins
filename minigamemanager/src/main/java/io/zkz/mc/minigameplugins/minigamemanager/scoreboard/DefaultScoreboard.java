package io.zkz.mc.minigameplugins.minigamemanager.scoreboard;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ComponentEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.TimerEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class DefaultScoreboard {
    public static final TeamBasedMinigameScoreboard DEFAULT_SCOREBOARD = (team) -> {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(mm("<legacy_gold><bold>" + MinigameService.getInstance().getTournamentName()));
        scoreboard.addEntry("gameName", new ComponentEntry(mm("<legacy_aqua><bold>Game " + MinigameService.getInstance().getGameNumber() + "/" + MinigameService.getInstance().getMaxGameNumber() + ":</bold></legacy_aqua> " + MinigameConstantsService.getInstance().getMinigameName())));
        switch (currentState) {
            case SERVER_STARTING, LOADING -> {
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Server loading..."));
            }
            case SETUP -> {
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Setting up minigame..."));
            }
            case WAITING_FOR_PLAYERS -> {
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Waiting for players..."));
                scoreboard.addSpace();
                scoreboard.addEntry("playerCount", new ValueEntry<>("<legacy_green><bold>Players:</bold></legacy_green> <value>/" + MinigameService.getInstance().getMinigame().getParticipantsAndGameMasters().size(), 0));
            }
            case RULES -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Showing rules..."));
            }
            case WAITING_TO_BEGIN -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Waiting for ready..."));
                scoreboard.addSpace();
                scoreboard.addEntry("playerCount", new ValueEntry<>("<legacy_green><bold>Ready players:</bold></legacy_green> <value>/" + MinigameService.getInstance().getMinigame().getParticipants().size(), 0));
            }
            case PRE_ROUND, IN_GAME, IN_GAME_2, IN_GAME_3, POST_ROUND, POST_GAME, MID_GAME, MID_GAME_2 -> {
                addRoundInformation(scoreboard);
                addMinigameTimer(scoreboard);
                addTeamInformation(scoreboard, team);
            }
            case PAUSED -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry(mm("<legacy_red><bold>Game status:"));
                scoreboard.addEntry(mm("Paused"));
                addTeamInformation(scoreboard, team);
            }
        }

        MinigameService.getInstance().getMinigame().modifyScoreboard(currentState, scoreboard);
        MinigameService.getInstance().getScoreboardModifiers().get(currentState).forEach(consumer -> consumer.accept(currentState, scoreboard));

        if (team == null) {
            ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
        } else {
            ScoreboardService.getInstance().setTeamScoreboard(team.id(), scoreboard);
        }
    };

    public static void addRoundInformation(GameScoreboard scoreboard) {
        if (MinigameService.getInstance().getCurrentRound().getMapName() != null) {
            scoreboard.addEntry(mm("<legacy_aqua><bold>Map:</bold></legacy_aqua> " + MinigameService.getInstance().getCurrentRound().getMapName()));
        }
        if (MinigameService.getInstance().getCurrentRound().getMapBy() != null) {
            scoreboard.addEntry(mm("<legacy_aqua><bold>Map by:</bold></legacy_aqua> " + MinigameService.getInstance().getCurrentRound().getMapBy()));
        }
        if (MinigameService.getInstance().getRoundCount() > 1) {
            scoreboard.addEntry(mm("<legacy_green><bold>Round:</bold></legacy_green> " + (MinigameService.getInstance().getCurrentRoundIndex() + 1) + "/" + MinigameService.getInstance().getRoundCount()));
        }
    }

    public static void addMinigameTimer(GameScoreboard scoreboard) {
        if (MinigameService.getInstance().getTimer() != null) {
            scoreboard.addEntry(new TimerEntry("<legacy_red><bold><label></bold></legacy_red> <value>", MinigameService.getInstance().getTimerLabel(), MinigameService.getInstance().getTimer()));
        } else if (MinigameService.getInstance().getTimerLabel() != null) {
            scoreboard.addEntry(new ComponentEntry(mm("<legacy_red><bold><0></bold></legacy_red> waiting...", MinigameService.getInstance().getTimerLabel())));
        }
    }

    public static void addTeamInformation(GameScoreboard scoreboard, GameTeam team) {
        if (team == null) return;

        scoreboard.addSpace();
        scoreboard.addEntry("teamScores", new TeamScoresScoreboardEntry(team));
    }
}
