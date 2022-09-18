package io.zkz.mc.minigameplugins.battlebox;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.PlayerAliveDeadRound;
import io.zkz.mc.minigameplugins.gametools.score.ScoreEntry;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BattleBoxRound extends PlayerAliveDeadRound {
    private final List<Pair<GameTeam, GameTeam>> matches;
    private final List<Boolean> activeMatches;
    private final List<GameTeam> matchWinners;
    private final GameConfig config;
    private final Map<UUID, String> kitSelections = new HashMap<>();
    private final Map<GameTeam, Set<String>> teamKitSelections = new HashMap<>();

    public BattleBoxRound(List<Pair<GameTeam, GameTeam>> matches) {
        this.matches = matches;
        this.activeMatches = new ArrayList<>(Collections.nCopies(this.matches.size(), true));
        this.matchWinners = new ArrayList<>(Collections.nCopies(this.matches.size(), null));
        this.config = BattleBoxService.getInstance().getConfig();
        MinigameService.getInstance().getGameTeams().forEach(team -> {
            this.teamKitSelections.put(team, new HashSet<>());
        });
        this.setMapName(this.config.map().name());
        this.setMapBy(this.config.map().author());
    }

    @Override
    public void onSetup() {
        // Create arenas (load schematic)
        World world = this.config.world();
        this.config.arenas().stream().map(vec -> BukkitAdapter.adapt(world, vec)).forEach(location -> {
            SchematicService.getInstance().placeSchematic(
                BattleBoxRound.class.getResourceAsStream("/schematics/arenas/" + this.config.selectedMapName() + ".schem"),
                location
            );
        });
        world.getEntitiesByClass(Item.class).forEach(Entity::remove);

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.HARD);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        WorldSyncUtils.setGameRule(GameRule.FALL_DAMAGE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_TILE_DROPS, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_LOOT, false);
        WorldSyncUtils.setGameRule(GameRule.MOB_GRIEFING, false);
        WorldSyncUtils.setGameRule(GameRule.DO_FIRE_TICK, false);
        WorldSyncUtils.setGameRule(GameRule.KEEP_INVENTORY, true);
    }

    @Override
    public void onEnterPreRound() {
        super.onEnterPreRound();

        BukkitUtils.forEachPlayer(player -> {
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
            if (team == null || team.isSpectator()) {
                return;
            }

            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);

            this.setupPlayer(player);
            var match = this.matches.get(this.arenaIndexOf(player));
            Chat.sendAlertFormatted(player, ChatType.GAME_INFO, "Round %d: %s vs %s", (Object) (MinigameService.getInstance().getCurrentRoundIndex() + 1), match.first().getDisplayName() + ChatColor.RESET, match.second().getDisplayName());
        });
    }

    private boolean movedDown = false;

    @Override
    public void onPreRoundTimerTick(long currentTimeMillis) {
        if (currentTimeMillis <= 10000 && !movedDown) {
            BukkitUtils.forEachPlayer(player -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
                if (team == null || team.isSpectator()) {
                    return;
                }

                int arenaIndex = this.arenaIndexOf(player);
                int teamIndex = this.teamIndexOf(arenaIndex, TeamService.getInstance().getTeamOfPlayer(player));
                player.teleport(this.config.computedTeamArenaSpawn(arenaIndex, teamIndex));

                // Assign kits to players who didn't pick
                if (this.kitSelections.get(player.getUniqueId()) == null) {
                    for (String kit : this.config.map().kits().keySet()) {
                        if (this.assignKit(player, kit)) {
                            break;
                        }
                    }
                }
                setupPlayerInventory(player);
            });
            movedDown = true;
            return;
        }

        if (currentTimeMillis > 3000) {
            return;
        }

        Material mat;
        if (currentTimeMillis <= 1000) {
            mat = Material.GREEN_STAINED_GLASS;
        } else if (currentTimeMillis <= 2000) {
            mat = Material.YELLOW_STAINED_GLASS;
        } else {
            mat = Material.RED_STAINED_GLASS;
        }

        WorldEditService we = WorldEditService.getInstance();
        var world = we.wrapWorld(this.config.world());
        this.config.allWalls().forEach(region -> we.fillRegion(
            world,
            region,
            mat
        ));
    }

    @Override
    public void onRoundStart() {
        // Wall
        WorldEditService we = WorldEditService.getInstance();
        var weWorld = we.wrapWorld(this.config.world());
        this.config.allWalls().forEach(region -> we.fillRegion(
            weWorld,
            region,
            Material.AIR
        ));

        // Spawn potions
        World world = this.config.world();
        this.config.potions().forEach(loc -> {
            world.dropItem(loc, ISB.material(Material.SPLASH_POTION).meta(itemMeta -> ((PotionMeta) itemMeta).setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))).build()).setVelocity(new Vector(0, 0, 0));
        });

        // Start timer
        MinigameService.getInstance().changeTimer(new GameCountdownTimer(BattleBoxService.getInstance().getPlugin(), 20, 120, TimeUnit.SECONDS, this::roundIsOver));
        MinigameService.getInstance().getTimer().addHook(new Runnable() {
            boolean warning30 = false;
            boolean warning20 = false;
            boolean warning10 = false;

            @Override
            public void run() {
                if (MinigameService.getInstance().getTimer() == null) {
                    return;
                }

                long currentTime = MinigameService.getInstance().getTimer().getCurrentTime(TimeUnit.MILLISECONDS);

                if (currentTime < 30000 && !warning30) {
                    warning30 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "30 seconds remain.");
                } else if (currentTime < 20000 && !warning20) {
                    warning20 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "20 seconds remain.");
                } else if (currentTime < 10000 && !warning10) {
                    warning10 = true;
                    SoundUtils.playSound(StandardSounds.ALERT_WARNING, 1, 1);
                    Chat.sendAlert(ChatType.WARNING, "10 seconds remain.");
                } else if (currentTime < 10000) {
                    SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);
                }
            }
        });
    }

    @Override
    public void onEnterPostRound() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
        BukkitUtils.runLater(() -> {
            Chat.sendAlert(ChatType.ACTIVE_INFO, "Round winners:");
            for (int i = 0; i < this.matches.size(); i++) {
                Pair<GameTeam, GameTeam> match = this.matches.get(i);
                GameTeam winner = this.matchWinners.get(i);
                String team1 = match.first().getFormatTag() + match.first().getPrefix() + " " + (match.first().equals(winner) ? "" + ChatColor.BOLD + ChatColor.UNDERLINE : "") + match.first().getName() + ChatColor.RESET;
                String team2 = match.second().getFormatTag() + match.second().getPrefix() + " " + (match.second().equals(winner) ? "" + ChatColor.BOLD + ChatColor.UNDERLINE : "") + match.second().getName() + ChatColor.RESET;
                Chat.sendMessage(team1 + ChatColor.GRAY + " vs. " + team2);
            }
            BukkitUtils.forEachPlayer(player -> {
                double points = ScoreService.getInstance().getRoundEntries(player, MinigameService.getInstance().getCurrentRoundIndex()).stream().mapToDouble(ScoreEntry::points).sum();
                Chat.sendMessage(player, " ");
                Chat.sendAlertFormatted(player, ChatType.ACTIVE_INFO, "You earned " + net.md_5.bungee.api.ChatColor.GREEN + ChatColor.BOLD + "%.1f" + ChatType.Constants.POINT_CHAR + " this round.", points);
            });
        }, 100);
    }

    @Override
    protected void onPlayerSetup(Player player, PlayerState playerState) {
        this.setupPlayerLocation(player);
        this.setupPlayerInventory(player);
    }

    public void setupPlayerLocation(Player player) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        if (team == null || team.isSpectator() || List.of(MinigameState.WAITING_FOR_PLAYERS, MinigameState.RULES, MinigameState.WAITING_TO_BEGIN).contains(currentState)) {
            player.teleport(this.config.computedSpecSpawn());
            return;
        }

        int arenaIndex = this.arenaIndexOf(team);
        int teamIndex = this.teamIndexOf(arenaIndex, team);

        if (this.isAlive(player)) {
            player.teleport(this.config.computedTeamKitSpawn(arenaIndex, teamIndex));
        } else {
            player.teleport(this.config.computedSpecSpawn(arenaIndex));
        }
    }

    public void setupPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3, 10, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 3, 10, true));

        Kits.apply("base", player);
        if (this.kitSelections.get(player.getUniqueId()) != null) {
            Kits.apply(this.kitSelections.get(player.getUniqueId()), player);
        }
    }

    public boolean assignKit(Player player, String kit) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // Check validity
        if (this.kitSelections.get(player.getUniqueId()) != null || this.teamKitSelections.get(team).contains(kit) || !config.map().kits().containsKey(kit)) {
            return false;
        }

        // Assign kit
        this.kitSelections.put(player.getUniqueId(), kit);
        this.teamKitSelections.get(team).add(kit);

        // Mark as such
        int arenaIndex = this.arenaIndexOf(team);
        WorldEditService.getInstance().fillRegion(
            WorldEditService.getInstance().wrapWorld(this.config.world()),
            this.config.kitSelectionPodiumRegion(kit, arenaIndex, this.teamIndexOf(arenaIndex, team)),
            Material.RED_CONCRETE
        );

        return true;
    }

    private int arenaIndexOf(GameTeam team) {
        return IntStream.range(0, this.matches.size())
            .filter(i -> this.matches.get(i).eitherMatch(team))
            .findFirst().orElse(-1);
    }

    public int arenaIndexOf(Player player) {
        return this.arenaIndexOf(TeamService.getInstance().getTeamOfPlayer(player));
    }

    private int teamIndexOf(int arenaIndex, GameTeam team) {
        return this.matches.get(arenaIndex).first().equals(team) ? 0 : 1;
    }

    private void roundIsOver() {
        this.getOnlineAlivePlayers().forEach(this::setDead);

        // Compute and assign scores for still-active games
        for (int i = 0; i < this.matches.size(); i++) {
            this.checkIfMatchIsOver(i);
        }

        // Check if all matches are over
        this.triggerRoundEnd();
    }

    public void recordKill(Player player, @Nullable Player killer) {
        // Sound
        SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);

        // Tell system they are dead
        this.setDead(player);

        if (killer != null) {
            // Assign scores
            SoundUtils.playSound(killer, StandardSounds.GOAL_MET_MINOR, 1, 1);
            player.spawnParticle(Particle.TOTEM, killer.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);
            MinigameService.getInstance().earnPoints(killer, "kill", Points.KILL);
        }

        // Check if this match is over (if so, play a sound and title and assign winning scores)
        this.checkIfMatchIsOver(this.arenaIndexOf(player));

        // Check if all matches are over
        this.checkIfAllMatchesAreOver();
    }

    public Collection<Player> getAllPlayersInArena(Player player) {
        return this.getAllPlayersInArena(this.arenaIndexOf(player));
    }

    public Collection<Player> getAllPlayersInArena(int arenaIndex) {
        Pair<GameTeam, GameTeam> match = this.matches.get(arenaIndex);
        Set<Player> res = new HashSet<>();
        res.addAll(TeamService.getInstance().getOnlineTeamMembers(match.first()));
        res.addAll(TeamService.getInstance().getOnlineTeamMembers(match.second()));
        return res;
    }

    public void checkIfMatchIsOver(int arenaIndex) {
        if (!this.activeMatches.get(arenaIndex)) {
            return;
        }

        Pair<GameTeam, GameTeam> match = this.matches.get(arenaIndex);

        // Compute block counts
        WorldEditService we = WorldEditService.getInstance();
        int woolCount1 = we.regionStats(
            we.wrapWorld(this.config.world()),
            this.config.woolRegion(arenaIndex),
            match.first().getWoolColor()
        );
        int woolCount2 = we.regionStats(
            we.wrapWorld(this.config.world()),
            this.config.woolRegion(arenaIndex),
            match.second().getWoolColor()
        );

        if (this.getAllPlayersInArena(arenaIndex).stream().noneMatch(this::isAlive) || woolCount1 == 9 || woolCount2 == 9) {
            // Compute winner
            Collection<Player> teamMembers;
            Collection<Player> otherTeamMembers;
            int points, otherPoints;
            if (woolCount1 > woolCount2) {
                // Team 1 won
                this.matchWinners.set(arenaIndex, match.first());
                teamMembers = TeamService.getInstance().getOnlineTeamMembers(match.first());
                otherTeamMembers = TeamService.getInstance().getOnlineTeamMembers(match.second());
                points = teamMembers.size() > 0 ? Points.WINNING / teamMembers.size() : 0;
                otherPoints = otherTeamMembers.size() > 0 ? Points.LOSING / otherTeamMembers.size() : 0;
            } else if (woolCount2 > woolCount1) {
                // Team 2 won
                this.matchWinners.set(arenaIndex, match.second());
                teamMembers = TeamService.getInstance().getOnlineTeamMembers(match.second());
                otherTeamMembers = TeamService.getInstance().getOnlineTeamMembers(match.first());
                points = teamMembers.size() > 0 ? Points.WINNING / teamMembers.size() : 0;
                otherPoints = otherTeamMembers.size() > 0 ? Points.LOSING / otherTeamMembers.size() : 0;
            } else {
                // Tie
                teamMembers = List.of();
                otherTeamMembers = this.getAllPlayersInArena(arenaIndex);
                points = 0;
                otherPoints = 0;
            }

            teamMembers.forEach(p -> {
                Chat.sendAlert(p, ChatType.GAME_SUCCESS, "Your team won! Well played!", points);
                SoundUtils.playSound(p, StandardSounds.GOAL_MET_MAJOR, 1, 1);
                p.spawnParticle(Particle.TOTEM, p.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);
                MinigameService.getInstance().earnPoints(p, "winning", points);
                this.setDead(p);
            });
            otherTeamMembers.forEach(p -> {
                Chat.sendAlert(p, ChatType.GAME_INFO, "Your team lost! Better luck next time!", otherPoints);
                MinigameService.getInstance().earnPoints(p, "losing", otherPoints);
                this.setDead(p);
            });

            // Mark match as completed
            this.activeMatches.set(arenaIndex, false);
        }
    }

    public void checkIfAllMatchesAreOver() {
        if (this.activeMatches.stream().noneMatch(x -> x)) {
            this.triggerRoundEnd();
        }
    }

    public List<Pair<GameTeam, GameTeam>> getArenas() {
        return this.matches;
    }
}
