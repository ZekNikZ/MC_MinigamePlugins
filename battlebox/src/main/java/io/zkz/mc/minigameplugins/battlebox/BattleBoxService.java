package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.JustGamemodePlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BattleBoxService extends PluginService<BattleBoxPlugin> {
    private static final BattleBoxService INSTANCE = new BattleBoxService();

    public static BattleBoxService getInstance() {
        return INSTANCE;
    }

    private GameConfig config;
    private final List<BattleBoxRound> rounds = new ArrayList<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();

        MinigameConstantsService.getInstance().setMinigameID("battlebox");
        MinigameConstantsService.getInstance().setMinigameName("Battle Box");
        TeamService.getInstance().setFriendlyFire(true);

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(760);
        minigame.setPostRoundDelay(200);
        minigame.setPostGameDelay(600);

        // Player states
        minigame.registerPlayerState(new JustGamemodePlayerState(GameMode.ADVENTURE),
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.PAUSED,
            MinigameState.IN_GAME,
            MinigameState.POST_GAME,
            MinigameState.POST_ROUND
        );

        // State change titles
        minigame.addSetupHandler(MinigameState.POST_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round over!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_GAME, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Game over!", ChatColor.GOLD + "Check the chat for score information.", 10, 70, 20);
        });
    }

    @Override
    protected void onEnable() {
        MinigameService minigame = MinigameService.getInstance();

        List<GameTeam> teams = minigame.getGameTeams().stream().sorted(Comparator.comparing(GameTeam::getId)).collect(Collectors.toCollection(ArrayList::new));
        // note: assumes even number of teams
        int numMatches = teams.size() - 1;
        int halfSize = teams.size() / 2;

        for (int i = 0; i < numMatches; i++) {
            List<Pair<GameTeam, GameTeam>> match = new ArrayList<>();
            for (int j = 0; j < halfSize; j++) {
                match.add(new Pair<>(teams.get(j), teams.get(teams.size() - j - 1)));
                System.out.println(match.get(j).first().getId() + " vs. " + match.get(j).second().getId());
            }

            System.out.println("-----");
            this.rounds.add(new BattleBoxRound(match));

            GameTeam team = teams.remove(0);
            Collections.rotate(teams, 1);
            teams.add(0, team);
        }

        minigame.registerRounds(this.rounds.toArray(BattleBoxRound[]::new));
        minigame.randomizeRoundOrder();
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("battlebox.json"), null, this::loadData)
        );
    }

    private void loadData(TypedJSONObject<Object> json) {
        this.config = new GameConfig(json);
    }

    public GameConfig getConfig() {
        return this.config;
    }

    public BattleBoxRound getCurrentRound() {
        return (BattleBoxRound) MinigameService.getInstance().getCurrentRound();
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!this.getCurrentRound().isAlive(player)) {
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.getCurrentRound().setupPlayerLocation(event.getPlayer());
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        if (BlockUtils.isWool(event.getPlayer().getInventory().getItemInOffHand().getType())) {
            event.getPlayer().getInventory().getItemInOffHand().setAmount(64);
        } else if (BlockUtils.isWool(event.getPlayer().getInventory().getItemInMainHand().getType())) {
            event.getPlayer().getInventory().getItemInMainHand().setAmount(64);
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!this.getCurrentRound().isAlive(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerHungerChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        // Record the kill
        this.getCurrentRound().recordKill(event.getEntity(), killer);

        // Only show death message to those in the arena
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);
        this.getCurrentRound().getAllPlayersInArena(event.getEntity()).forEach(p -> {
            if (p.equals(killer)) {
                p.sendMessage(Chat.Constants.POINT_PREFIX.replace("%points%", String.valueOf(Points.KILL)) + deathMessage);
            } else {
                p.sendMessage(deathMessage);
            }
        });
    }

    @EventHandler
    private void onPlaceBlock(BlockPlaceEvent event) {
        this.getCurrentRound().checkIfMatchIsOver(this.getCurrentRound().arenaIndexOf(event.getPlayer()));
        this.getCurrentRound().checkIfAllMatchesAreOver();
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        BukkitUtils.runNextTick(() -> this.getCurrentRound().setupPlayerLocation(event.getPlayer()));
    }
}
