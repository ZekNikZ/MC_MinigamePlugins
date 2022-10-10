package io.zkz.mc.uhc.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;

public class UHCScoreboards {
    private static boolean scoreboardTitleColor = false;

    private static IntValueEntry numPlayers;
    private static IntValueEntry numTeams;

    private static final SharedReference<Integer> worldborderSize = new SharedReference<>(0);

    private static final SharedReference<Integer> alivePlayers = new SharedReference<>(0);
    private static final SharedReference<Integer> aliveTeams = new SharedReference<>(0);

    private static JavaPlugin plugin;

    public static void setup(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
    }

    public static void setupLobbyScoreboard() {
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.YELLOW + ChatColor.BOLD + "RFP UHC");
        scoreboard.addEntry(numPlayers = new IntValueEntry(scoreboard, "Players: ", ValueEntry.ValuePos.SUFFIX, 0));
        scoreboard.addEntry(numTeams = new IntValueEntry(scoreboard, "Teams: ", ValueEntry.ValuePos.SUFFIX, 0));
        updateCompetitors();
        ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
        ScoreboardService.getInstance().setupTeams();
    }

    public static StringValueEntry setupPregameScoreboard(SharedReference<Integer> worldborderSizeRaw) {
        worldborderSizeRaw.addListener(() -> {
            worldborderSize.setAndNotify(worldborderSizeRaw.get() / 2);
        });
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.YELLOW + ChatColor.BOLD + "RFP UHC");
        StringValueEntry readyPlayersEntry = new StringValueEntry(scoreboard, "Ready players: ", ValueEntry.ValuePos.SUFFIX, "0/" + GameManager.getInstance().getCompetitors().size());
        scoreboard.addEntry(readyPlayersEntry);
        scoreboard.addEntry(new TimerEntry(scoreboard, GameManager.getInstance().getTimer()));
        scoreboard.addEntry(new SharedReferenceEntry<>(scoreboard, "World border: \u00b1", ValueEntry.ValuePos.SUFFIX, worldborderSize));
        ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
        ScoreboardService.getInstance().setupTeams();
        return readyPlayersEntry;
    }

    public static void setupGameScoreboard() {
        // Default scoreboard
        GameScoreboard globalScoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.YELLOW + ChatColor.BOLD + "RFP UHC");
        globalScoreboard.addEntry(new TimerEntry(globalScoreboard, GameManager.getInstance().getTimer(), "Time: ", ValueEntry.ValuePos.SUFFIX, null));
        globalScoreboard.addEntry(new SharedReferenceEntry<>(globalScoreboard, "World border: \u00b1", ValueEntry.ValuePos.SUFFIX, worldborderSize));
        globalScoreboard.addSpace();
        globalScoreboard.addEntry(new SharedReferenceEntry<>(globalScoreboard, "Alive players: ", ValueEntry.ValuePos.SUFFIX, alivePlayers));
        if (SettingsManager.getInstance().teamGame().get() == TeamStatus.TEAM_GAME) {
            globalScoreboard.addEntry(new SharedReferenceEntry<>(globalScoreboard, "Alive teams: ", ValueEntry.ValuePos.SUFFIX, aliveTeams));
        }
        globalScoreboard.addSpace();
        globalScoreboard.addEntry("You are " + ChatColor.GRAY + ChatColor.BOLD + "SPECTATING");
        ScoreboardService.getInstance().setGlobalScoreboard(globalScoreboard);

        // Health display
        Objective globalObj = globalScoreboard.getScoreboard().registerNewObjective("hp", "health");
        globalObj.setDisplayName("HP");
        globalObj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        globalObj.setRenderType(RenderType.HEARTS);

        // Competitor scoreboards
        GameManager.getInstance().getCompetitors().forEach(uuid -> {
            GameScoreboard playerScoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.YELLOW + ChatColor.BOLD + "RFP UHC");
            playerScoreboard.addEntry(new TimerEntry(playerScoreboard, GameManager.getInstance().getTimer(), "Time: ", ValueEntry.ValuePos.SUFFIX, null));
            playerScoreboard.addEntry(new SharedReferenceEntry<>(playerScoreboard, "World border: \u00b1", ValueEntry.ValuePos.SUFFIX, worldborderSize));
            playerScoreboard.addSpace();
            playerScoreboard.addEntry(new SharedReferenceEntry<>(playerScoreboard, "Alive players: ", ValueEntry.ValuePos.SUFFIX, alivePlayers));
            if (SettingsManager.getInstance().teamGame().get() == TeamStatus.TEAM_GAME) {
                playerScoreboard.addEntry(new SharedReferenceEntry<>(playerScoreboard, "Alive teams: ", ValueEntry.ValuePos.SUFFIX, aliveTeams));
                playerScoreboard.addSpace();
                GameTeam team = TeamService.getInstance().getPlayerTeam(uuid);
                if (team != null) {
                    playerScoreboard.addEntry("Your team: " + team.getFormatCode() + ChatColor.BOLD + team.getPrefix());
                    playerScoreboard.addEntry("Teammates:");
                    TeamService.getInstance().getTeamMembers(team).forEach(otherUUID -> {
                        if (uuid.equals(otherUUID)) {
                            return;
                        }

                        playerScoreboard.addEntry(new PlayerEntry(playerScoreboard, " ", ValueEntry.ValuePos.SUFFIX, otherUUID, true));
                    });
                }
            }

            // Health display
            Objective playerObj = playerScoreboard.getScoreboard().registerNewObjective("hp", "health");
            playerObj.setDisplayName("HP");
            playerObj.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            ScoreboardService.getInstance().setPlayerScoreboard(uuid, playerScoreboard);
        });

        ScoreboardService.getInstance().setupTeams();
    }

    public static void setupPostgameScoreboard() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            GameStats.getInstance().startStatScoreboards(plugin);
        }, 20);
    }

    public static void updateCompetitors() {
        if (GameManager.getInstance().getState().isInGame()) {
            alivePlayers.setAndNotify(GameManager.getInstance().getAliveCompetitors().size());
            aliveTeams.setAndNotify(GameManager.getInstance().getAliveTeams().size());
        } else if (numPlayers != null) {
            numPlayers.setValue(GameManager.getInstance().getInitialCompetitors().size());
            numTeams.setValue(GameManager.getInstance().getInitialTeams().size());
        }
    }
}
