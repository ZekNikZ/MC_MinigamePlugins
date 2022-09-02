package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.PlayerAliveDeadRound;
import io.zkz.mc.minigameplugins.minigamemanager.score.ScoreEntry;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.service.ScoreService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BattleBoxRound extends PlayerAliveDeadRound {
    private final List<Pair<GameTeam, GameTeam>> matches;
    private final GameConfig config;
    private final Map<UUID, String> kitSelections = new HashMap<>();
    private final Map<GameTeam, Set<String>> teamKitSelections = new HashMap<>();

    public BattleBoxRound(List<Pair<GameTeam, GameTeam>> matches) {
        this.matches = matches;
        this.config = BattleBoxService.getInstance().getConfig();
        MinigameService.getInstance().getGameTeams().forEach(team -> {
            this.teamKitSelections.put(team, new HashSet<>());
        });
    }

    @Override
    public void onSetup() {
        // TODO: create arenas (load schematic)

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
            player.getInventory().clear();
        });
    }

    @Override
    public void onPreRoundTimerTick(long currentTimeMillis) {
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
        // Assign kits to players who didn't pick
        BukkitUtils.forEachPlayer(player -> {
            if (this.kitSelections.get(player.getUniqueId()) == null) {
                for (String kit : this.config.map().kits().keySet()) {
                    if (this.assignKit(player, kit)) {
                        break;
                    }
                }
            }
        });

        // Setup players
        BukkitUtils.forEachPlayer(this::setupPlayer);

        // Spawn potions
        World world = this.config.world();
        this.config.potions().forEach(loc -> {
            world.dropItem(loc, ISB.material(Material.SPLASH_POTION).meta(itemMeta -> ((PotionMeta) itemMeta).setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))).build());
        });

        // Start timer
        MinigameService.getInstance().changeTimer(new GameCountdownTimer(BattleBoxService.getInstance().getPlugin(), 20, 120, TimeUnit.SECONDS, this::roundIsOver));
    }

    @Override
    public void onEnterPostRound() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
        BukkitUtils.forEachPlayer(player -> {
            double points = ScoreService.getInstance().getRoundEntries(player).stream().mapToDouble(ScoreEntry::points).sum();
            Chat.sendMessage(player, " ");
            Chat.sendAlertFormatted(player, ChatType.ACTIVE_INFO, "You earned " + net.md_5.bungee.api.ChatColor.GREEN + ChatColor.BOLD + "%.1f" + Chat.Constants.POINT_CHAR + " this round.", points);
        });
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
            if (currentState == MinigameState.PRE_ROUND) {
                player.teleport(this.config.computedTeamKitSpawn(arenaIndex, teamIndex));
            } else {
                player.teleport(this.config.computedTeamArenaSpawn(arenaIndex, teamIndex));
            }
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
        Kits.apply(this.kitSelections.get(player.getUniqueId()), player);
    }

    public boolean assignKit(Player player, String kit) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);

        // Check validity
        if (this.teamKitSelections.get(team).contains(kit) || !config.map().kits().containsKey(kit)) {
            return false;
        }

        // Assign kit
        this.kitSelections.put(player.getUniqueId(), kit);
        this.teamKitSelections.get(team).add(kit);

        // Mark as such
        int arenaIndex = this.arenaIndexOf(team);
        WorldEditService.getInstance().fillRegion(
            this.config.kitSelectionPodiumRegion(kit, arenaIndex, this.teamIndexOf(arenaIndex, team)),
            Material.RED_CONCRETE
        );

        return true;
    }

    private int arenaIndexOf(GameTeam team) {
        return IntStream.range(0, this.matches.size())
            .filter(i -> this.matches.get(i).eitherMatch(team))
            .findFirst().orElse(0);
    }

    private int teamIndexOf(int arenaIndex, GameTeam team) {
        return this.matches.get(arenaIndex).first().equals(team) ? 0 : 1;
    }

    private void roundIsOver() {
        this.triggerRoundEnd();
    }
}
