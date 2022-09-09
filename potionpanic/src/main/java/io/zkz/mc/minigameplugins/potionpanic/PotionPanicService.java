package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.IObservable;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PotionPanicService extends PluginService<PotionPanicPlugin> implements IObservable {
    private static final PotionPanicService INSTANCE = new PotionPanicService();

    public static PotionPanicService getInstance() {
        return INSTANCE;
    }

    private GameTeam team1;
    private GameTeam team2;
    private final Map<GameTeam, Integer> wins = new HashMap<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();

        MinigameConstantsService.getInstance().setMinigameID("potionpanic");
        MinigameConstantsService.getInstance().setMinigameName("Potion Panic");

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(300);
        minigame.setPostRoundDelay(200);
        minigame.setPostGameDelay(1200);
        minigame.setShowScoreSummary(false);

        // Player states
        minigame.registerPlayerState(new BasicPlayerState(GameMode.ADVENTURE),
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.PAUSED,
            MinigameState.POST_ROUND,
            MinigameState.IN_GAME,
            MinigameState.POST_GAME
        );

        // State change titles
        minigame.addSetupHandler(MinigameState.POST_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round over!", this.getCurrentRound().getWinner().getDisplayName() + " won!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_GAME, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Game over!", this.getCurrentRound().getWinner().getDisplayName() + " won!", 10, 70, 20);
        });

        // In game scoreboard
        BiConsumer<MinigameState, GameScoreboard> scoreboardModifier = (state, scoreboard) -> {
            scoreboard.removeEntry("gameName");
            scoreboard.removeEntry("teamScores");
            scoreboard.addEntry(new PotionPanicScoreboardEntry());
        };
        minigame.registerScoreboard(MinigameState.PRE_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.IN_GAME, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.POST_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.POST_GAME, scoreboardModifier);
    }

    @Override
    protected void onEnable() {
        MinigameService minigame = MinigameService.getInstance();
        minigame.registerRounds(
            Collections.nCopies(5, (Supplier<PotionPanicRound>) PotionPanicRound::new).stream()
                .map(Supplier::get)
                .toArray(PotionPanicRound[]::new)
        );
    }

    public PotionPanicRound getCurrentRound() {
        return (PotionPanicRound) MinigameService.getInstance().getCurrentRound();
    }

    public GameTeam getTeam1() {
        return this.team1;
    }

    public void setTeam1(GameTeam team1) {
        this.team1 = team1;
        this.wins.put(team1, 0);
    }

    public GameTeam getTeam2() {
        return this.team2;
    }

    public void setTeam2(GameTeam team2) {
        this.team2 = team2;
        this.wins.put(team2, 0);
    }

    public Map<GameTeam, Integer> getWins() {
        return this.wins;
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!this.getCurrentRound().isAlive(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.getCurrentRound().setupPlayer(event.getPlayer());
    }

    @EventHandler
    private void onItemPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.getCurrentRound().handlePotionPickUp(player);
        }
    }

    @EventHandler
    private void onPotionSplash(PotionSplashEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (this.getCurrentRound().isAlive(player)) {
                this.getCurrentRound().handlePlayerThrow(player);
            }
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (this.getCurrentRound().isAlive(event.getEntity())) {
            this.getCurrentRound().handlePlayerDeath(event.getEntity());
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        BukkitUtils.runNextTick(() -> this.getCurrentRound().setupPlayer(event.getPlayer()));
    }

    @EventHandler
    private void onHungerChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @SuppressWarnings("rawtypes")
    private final List<IObserver> listeners = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    @Override
    public void addListener(IObserver observer) {
        this.listeners.add(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void removeListener(IObserver observer) {
        this.listeners.remove(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<IObserver> getListeners() {
        return this.listeners;
    }

}
