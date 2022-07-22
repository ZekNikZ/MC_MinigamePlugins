package io.zkz.mc.minigameplugins.testplugin.service;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.CompositeScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestService extends TestPluginService {
    private static final TestService INSTANCE = new TestService();

    public static TestService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void setup() {

    }

    private static final AtomicBoolean testBool = new AtomicBoolean();

    @Override
    public void onEnable() {
        TeamService.getInstance().setupDefaultTeams();

        GameScoreboard globalScoreboard = ScoreboardService.getInstance().createNewScoreboard("Testing");

        globalScoreboard.addEntry("Test 1");
        globalScoreboard.addEntry("Test 2");
        globalScoreboard.addSpace();

        CompositeScoreboardEntry compositeScoreboardEntry = globalScoreboard.addEntry(new CompositeScoreboardEntry(globalScoreboard));
        compositeScoreboardEntry.addChild(new StringEntry(globalScoreboard, "Test 3"));
        compositeScoreboardEntry.addChild(new StringEntry(globalScoreboard, "Test 4"));
        globalScoreboard.addSpace();

        ValueEntry<String> valueEntry = globalScoreboard.addEntry(new ValueEntry<>(globalScoreboard, "Boop: ", "true"));
        Bukkit.getScheduler().runTaskTimer(this.getPlugin(), () -> {
            testBool.set(!testBool.get());
            valueEntry.setValue(String.valueOf(testBool.get()));
        }, 0, 20);

        ScoreboardService.getInstance().setGlobalScoreboard(globalScoreboard);

        // =====

        GameScoreboard teamScoreboard = ScoreboardService.getInstance().createNewScoreboard("Blue Team");

        teamScoreboard.addEntry("Foo");
        teamScoreboard.addSpace();
        teamScoreboard.addEntry("Bar");

        ScoreboardService.getInstance().setTeamScoreboard(DefaultTeams.BLUE.getId(), teamScoreboard);
    }

    @Override
    public void onDisable() {

    }
}
