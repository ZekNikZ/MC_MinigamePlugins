package io.zkz.mc.minigameplugins.testplugin.service;

import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.CompositeScoreboardEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.TimerEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountupTimer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestService extends TestPluginService {
    private static final TestService INSTANCE = new TestService();

    public static TestService getInstance() {
        return INSTANCE;
    }

    private static final AtomicBoolean testBool = new AtomicBoolean();

    @Override
    public void onEnable() {
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
        GameCountupTimer timer1 = new GameCountupTimer(this.getPlugin(), 20) {
            @Override
            protected void onUpdate() {
                super.onUpdate();
                testBool.set(!testBool.get());
                valueEntry.setValue((testBool.get() ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(testBool.get()));
            }
        };
        timer1.start();
        GameCountdownTimer timer2 = new GameCountdownTimer(this.getPlugin(), 20, 10000, TimeUnit.SECONDS);
        timer2.start();
        globalScoreboard.addEntry(new TimerEntry(globalScoreboard, "", "Timer 1: ", "%s", timer1));
        globalScoreboard.addEntry(new TimerEntry(globalScoreboard, "", "Timer 2: ", "%s", timer2));

        ScoreboardService.getInstance().setGlobalScoreboard(globalScoreboard);

        // =====

        GameScoreboard teamScoreboard = ScoreboardService.getInstance().createNewScoreboard("Blue Team");

        for (int i = 0; i < 15; i++) {
            teamScoreboard.addSpace();
        }

        ScoreboardService.getInstance().setTeamScoreboard(DefaultTeams.BLUE.getId(), teamScoreboard);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new GameCountdownTimer(
            INSTANCE.getPlugin(),
            10,
            10,
            TimeUnit.SECONDS,
            () -> ReadyUpService.getInstance().waitForReady(
                List.of(player.getUniqueId()),
                () -> {
                    player.sendMessage("Cool!");
                    player.playSound(player.getLocation(), StandardSounds.GOAL_MET_MAJOR, 1, 1);
                }
            )
        ).start();
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        Location blockOn = event.getTo().clone().add(new Vector(0, -0.1, 0));
        if (!blockOn.isWorldLoaded()) {
            return;
        }
        Block block = blockOn.getBlock();
        Location blockLocation = block.getLocation();

        if (block.getType() == Material.AIR) {
            List<Location> otherOptions = List.of(
                blockLocation.add(1, 0, 0).getBlock().getLocation(),
                blockLocation.add(-1, 0, 0).getBlock().getLocation(),
                blockLocation.add(0, 0, -1).getBlock().getLocation(),
                blockLocation.add(0, 0, 1).getBlock().getLocation()
            );

            Location finalBlockOn = blockOn;
            blockOn = otherOptions.stream().map(b -> b.add(new Vector(0.5, 0.5, 0.5))).min(Comparator.comparing(b -> b.distance(finalBlockOn))).get();
        }

        if (blockOn.getBlock().getType() == Material.STONE) {
            blockOn.getWorld().setBlockData(blockOn, Material.ANDESITE.createBlockData());
            this.scheduleBlockForRemoval(blockOn);
        }
    }

    private void scheduleBlockForRemoval(Location location) {
        final double seconds = 0.5;
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            location.getWorld().setBlockData(location, Material.AIR.createBlockData());
        }, (long) (seconds * 20));
    }
}
