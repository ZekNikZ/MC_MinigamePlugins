package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class GuessTheBuildService extends PluginService<GuessTheBuildPlugin> {
    private static final GuessTheBuildService INSTANCE = new GuessTheBuildService();

    public static GuessTheBuildService getInstance() {
        return INSTANCE;
    }

    private final List<String> words = new ArrayList<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();

        MinigameConstantsService.getInstance().setMinigameID("guessthebuild");
        MinigameConstantsService.getInstance().setMinigameName("Guess the Build");

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(300);
        minigame.setPostRoundDelay(100);
        minigame.setPostGameDelay(600);

        // Player states
        minigame.registerPlayerState(new BasicPlayerState(GameMode.SPECTATOR),
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

        // Round setup handlers

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            // TODO: X is building
//            TitleUtils.broadcastTitle(ChatColor.RED + "Round starts in 20 seconds", ChatColor.GOLD + "Find a good starting position!", 10, 70, 20);
            Chat.sendAlert(ChatType.ALERT, this.getCurrentRound().getBuilder().getDisplayName() + ChatColor.AQUA + " is choosing a word!");
        });
        minigame.addSetupHandler(MinigameState.POST_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round over!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_GAME, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Game over!", ChatColor.GOLD + "Check the chat for score information.", 10, 70, 20);
        });

        // In game scoreboard
        BiConsumer<MinigameState, GameScoreboard> scoreboardModifier = (state, scoreboard) -> {
            scoreboard.addSpace();
            scoreboard.addEntry("" + ChatColor.GREEN + ChatColor.BOLD + "Builder: " + this.getCurrentRound().getBuilder().getDisplayName());
        };
        minigame.registerScoreboard(MinigameState.PRE_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.IN_GAME, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.PAUSED, scoreboardModifier);
    }

    @Override
    protected void onEnable() {
        MinigameService minigame = MinigameService.getInstance();
        minigame.registerRounds(minigame.getPlayers().stream().sorted(Comparator.comparing(p -> p)).map(GuessTheBuildRound::new).toArray(GuessTheBuildRound[]::new));
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("gtb.json"), null, this::loadData)
        );
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> object) {
        this.words.clear();
        this.words.addAll(object.getList("words", String.class));
        Collections.shuffle(this.words);
    }

    public GuessTheBuildRound getCurrentRound() {
        return (GuessTheBuildRound) MinigameService.getInstance().getCurrentRound();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.getCurrentRound().setupPlayerLocation(event.getPlayer());
    }

    @EventHandler
    private void onChatMessage(AsyncPlayerChatEvent event) {
        // TODO: find a way to deal with this and cancel the event
        if (MinigameService.getInstance().getCurrentState().isInGame()) {
            if (this.getCurrentRound().handlePlayerGuess(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
            }
        }
    }

    public List<String> getWords() {
        return this.words;
    }
}
