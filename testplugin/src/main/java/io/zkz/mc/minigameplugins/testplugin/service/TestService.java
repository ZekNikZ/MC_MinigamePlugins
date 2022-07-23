package io.zkz.mc.minigameplugins.testplugin.service;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.CompositeScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.TimerEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountupTimer;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;
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
    private GameCountupTimer timer1;
    private GameCountdownTimer timer2;

    @Override
    public void onEnable() {
        TeamService.getInstance().setupDefaultTeams();

        GameScoreboard globalScoreboard = ScoreboardService.getInstance().createNewScoreboard("Testing");

        globalScoreboard.addEntry("Test 1");
        globalScoreboard.addEntry("Test 2");
        globalScoreboard.addEntry("\u200b");
        globalScoreboard.addSpace();

        CompositeScoreboardEntry compositeScoreboardEntry = globalScoreboard.addEntry(new CompositeScoreboardEntry(globalScoreboard));
        compositeScoreboardEntry.addChild(new StringEntry(globalScoreboard, "Test 3"));
        compositeScoreboardEntry.addChild(new StringEntry(globalScoreboard, "Test 4"));
        globalScoreboard.addSpace();

        ValueEntry<String> valueEntry = globalScoreboard.addEntry(new ValueEntry<>(globalScoreboard, "", "Boop: ", "%s", ""));
        this.timer1 = new GameCountupTimer(this.getPlugin(), 20) {
            @Override
            protected void onUpdate() {
                super.onUpdate();
                testBool.set(!testBool.get());
                valueEntry.setValue((testBool.get() ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(testBool.get()));
            }
        };
        this.timer1.start();
        this.timer2 = new GameCountdownTimer(this.getPlugin(), 20, 10000, TimeUnit.SECONDS);
        this.timer2.start();
        globalScoreboard.addEntry(new TimerEntry(globalScoreboard, "", "Timer 1: ", "%s", this.timer1));
        globalScoreboard.addEntry(new TimerEntry(globalScoreboard, "", "Timer 2: ", "%s", this.timer2));

        ScoreboardService.getInstance().setGlobalScoreboard(globalScoreboard);

        // =====

        GameScoreboard teamScoreboard = ScoreboardService.getInstance().createNewScoreboard("Blue Team");

        for (int i = 0; i < 15; i++) {
            teamScoreboard.addSpace();
        }

        ScoreboardService.getInstance().setTeamScoreboard(DefaultTeams.BLUE.getId(), teamScoreboard);
    }

    @Override
    public void onDisable() {

    }
}
