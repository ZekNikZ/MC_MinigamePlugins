package io.zkz.mc.minigameplugins.minigamemanager.minigame;

import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpSession;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.score.ScoreService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.MinigameManagerPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.event.RoundChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.RulesTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.ScoreSummaryTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service
public class MinigameService extends PluginService<MinigameManagerPlugin> {
    private static final MinigameService INSTANCE = new MinigameService();

    public static MinigameService getInstance() {
        return INSTANCE;
    }

    // region State Variables and Handlers
    private MinigameState state = MinigameState.SERVER_STARTING;
    private MinigameState unpausedState;
    private final Map<MinigameState, List<Runnable>> stateSetupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Runnable>> stateCleanupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Supplier<MinigameTask>>> stateTasks = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, IPlayerState> statePlayerStates = new HashMap<>();
    private final Map<MinigameState, MinigameScoreboard> scoreboards = new HashMap<>();
    private final Map<MinigameState, List<BiConsumer<MinigameState, GameScoreboard>>> scoreboardModifiers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Set<MinigameTask> runningTasks = new HashSet<>();
    private Component timerLabel;
    private AbstractTimer timer;
    // endregion

    // region Minigame Metadata Variables
    private String tournamentName = "MC Tournament";
    private int gameNumber = 0;
    private int maxGameNumber = 6;
    private Minigame<?> minigame = null;
    private final List<Round> rounds = new ArrayList<>();
    private final List<List<Component>> rulesSlides = new ArrayList<>();
    private int currentRound = -1, initialRound = 0;
    // endregion

    // region Minigame Settings Variables
    private boolean spectatorsCanOnlySeeAliveTeammates = false;
    // endregion

    // region Registering & Setup Functions
    @Override
    protected void onEnable() {
        // Schedule a transition to the setup phase
        BukkitUtils.runNextTick(() -> {
            // Load initial state
            // TODO: migrate to Tournament Connector
            // this.db.addAction(this::loadInitialState);

            this.transitionToSetup();
        });

        // Add default state handlers

        // WAITING_FOR_PLAYERS
        this.registerTask(MinigameState.WAITING_FOR_PLAYERS, () -> new MinigameTask(1, 20) {
            @Override
            public void run() {
                waitForPlayers();
            }
        });

        // RULES
        this.registerTask(MinigameState.RULES, RulesTask::new);

        // WAITING_TO_BEGIN
        this.registerSetupHandler(MinigameState.WAITING_TO_BEGIN, () -> {
            if (this.minigame.getReadyUpEachRound()) {
                this.setState(MinigameState.PRE_ROUND);
            } else {
                ReadyUpService.getInstance().waitForReady(this.minigame.getParticipantsAndGameMasters(), this::handlePlayersReady, this::handlePlayerReady);
            }
        });

        // PRE_ROUND
        this.registerSetupHandler(MinigameState.PRE_ROUND, () -> {
            this.getCurrentRound().enterPreRound();

            // Reset glowing
            BukkitUtils.forEachPlayer(player -> {
                BukkitUtils.forEachPlayer(otherPlayer -> {
                    if (player.equals(otherPlayer)) {
                        return;
                    }

                    player.hidePlayer(this.getPlugin(), otherPlayer);
                    player.showPlayer(this.getPlugin(), otherPlayer);
                });
            });

            if (this.minigame.getReadyUpEachRound()) {
                ReadyUpService.getInstance().waitForReady(this.minigame.getParticipantsAndGameMasters(), () -> {
                    Chat.sendMessage(ChatType.GAME_INFO, mm("All players are now ready. Round starting in " + this.minigame.getPreRoundDelay() / 20 + " seconds."));
                    this.startPreRoundTimer();
                });
            } else {
                this.startPreRoundTimer();
            }
        });
        this.registerCleanupHandler(MinigameState.PRE_ROUND, () -> {
            this.changeTimer(null, null);
        });

        // IN_GAME (note: these are registered for different states to prevent paused from triggering them)
        this.registerCleanupHandler(MinigameState.PRE_ROUND, () -> this.getCurrentRound().onRoundStart());

        // MID_GAME
        this.registerSetupHandler(MinigameState.MID_GAME, () -> this.getCurrentRound().onPhase1End());
        this.registerCleanupHandler(MinigameState.MID_GAME, () -> this.getCurrentRound().onPhase2Start());

        // MID_GAME_2
        this.registerSetupHandler(MinigameState.MID_GAME_2, () -> this.getCurrentRound().onPhase2End());
        this.registerCleanupHandler(MinigameState.MID_GAME_2, () -> this.getCurrentRound().onPhase3Start());

        // POST_ROUND
        this.registerSetupHandler(MinigameState.POST_ROUND, () -> {
            this.getCurrentRound().onEnterPostRound();
            this.changeTimer(null, null);
            if (this.minigame.getAutomaticNextRound()) {
                this.goToNextRound();
            }
        });

        // PAUSE
        this.registerSetupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onPause());
        this.registerCleanupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onUnpause());

        // POST_GAME
        this.registerSetupHandler(MinigameState.POST_GAME, () -> this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.minigame.getPostGameDelay() * 50L + ScoreSummaryTask.SECONDS_PER_SLIDE * ScoreSummaryTask.NUM_SLIDES * 20, TimeUnit.MILLISECONDS, this::endGame), mm("Back to hub in:")));
        this.registerTask(MinigameState.POST_GAME, ScoreSummaryTask::new);
    }

    // endregion

    // region Helper Functions
    private void drawScoreboard(MinigameState state) {
        MinigameScoreboard scoreboard = this.scoreboards.get(state);
        if (scoreboard == null) {
            scoreboard = this.minigame.buildScoreboard(state);
        }
        if (scoreboard == null) {
            return;
        }
        scoreboard.setup();
    }

    private void startPreRoundTimer() {
        this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.minigame.getPreRoundDelay() * 50L, TimeUnit.MILLISECONDS, this::transitionToInGame) {
            @Override
            protected void onUpdate() {
                if (getCurrentTimeMillis() <= 5000) {
                    SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);
                }

                getCurrentRound().onPreRoundTimerTick(getCurrentTimeMillis());

                super.onUpdate();
            }
        }, mm("Round starts in:"));
    }

    public void removeRunningTask(MinigameTask task) {
        this.runningTasks.remove(task);
    }

    public void changeTimer(@Nullable AbstractTimer timer, @Nullable Component timerLabel) {
        if (this.timer != null) {
            this.timer.stop();
        }

        this.timer = timer;
        this.timerLabel = timerLabel;
        if (this.timer != null) {
            this.timer.start();
        }
        this.drawScoreboard(this.state);
    }
    // endregion

    public void setState(MinigameState newState) {
        this.getLogger().info("State transition from " + this.state.name() + " to " + newState.name());
        StateChangeEvent.Pre preEvent = new StateChangeEvent.Pre(this.state, newState);
        BukkitUtils.dispatchEvent(preEvent);
        if (!preEvent.isCancelled()) {
            if (this.state != null) {
                this.runningTasks.forEach(task -> task.cancel(false));
                this.runningTasks.clear();
                this.minigame.onStateCleanup(this.state);
                this.stateCleanupHandlers.get(this.state).forEach(Runnable::run);
            }
            this.state = newState;
            this.minigame.onStateSetup(this.state);
            this.stateSetupHandlers.get(newState).forEach(Runnable::run);
            Stream.concat(
                this.minigame.buildTasks(this.state).stream(),
                this.stateTasks.get(this.state).stream()
            ).forEach(t -> {
                MinigameTask task = t.get();
                task.start(this.getPlugin());
                this.getLogger().info("Started task with ID " + task.getTaskId() + " of type " + t.getClass().getName());
                this.runningTasks.add(task);
            });
            IPlayerState playerState = this.minigame.buildPlayerState(this.state);
            if (playerState != null) {
                BukkitUtils.forEachPlayer(playerState::apply);
            }
            playerState = this.statePlayerStates.get(this.state);
            if (playerState != null) {
                BukkitUtils.forEachPlayer(playerState::apply);
            }
        }
        // TODO: migrate to Tournament Connector
        // this.storeStateInDatabase();
        BukkitUtils.dispatchEvent(new StateChangeEvent.Post(this.state, newState));
    }

    public MinigameState getCurrentState() {
        return this.state;
    }

    public void registerSetupHandler(MinigameState state, Runnable handler) {
        this.stateSetupHandlers.get(state).add(handler);
    }

    public void registerCleanupHandler(MinigameState state, Runnable handler) {
        this.stateCleanupHandlers.get(state).add(handler);
    }

    public void registerTask(MinigameState state, Supplier<MinigameTask> taskSupplier) {
        this.stateTasks.get(state).add(taskSupplier);
    }

    public List<List<Component>> getRulesSlides() {
        return this.rulesSlides;
    }

    public void registerScoreboard(MinigameState state, MinigameScoreboard scoreboard) {
        this.scoreboards.put(state, scoreboard);
    }

    public void registerScoreboard(MinigameState state, BiConsumer<MinigameState, GameScoreboard> scoreboardModifier) {
        this.scoreboardModifiers.get(state).add(scoreboardModifier);
    }

    public void registerGlobalScoreboard(MinigameScoreboard scoreboard) {
        for (MinigameState state : MinigameState.values()) {
            this.registerScoreboard(state, scoreboard);
        }
    }

    public void registerGlobalScoreboard(BiConsumer<MinigameState, GameScoreboard> scoreboardModifier) {
        for (MinigameState state : MinigameState.values()) {
            this.registerScoreboard(state, scoreboardModifier);
        }
    }

    public void setMinigame(Minigame<?> minigame) {
        this.minigame = minigame;

        // Cleanup old minigames
//        this.stateSetupHandlers.values().forEach(List::clear);
//        this.stateCleanupHandlers.values().forEach(List::clear);
//        this.stateTasks.values().forEach(List::clear);
        this.statePlayerStates.clear();
        this.scoreboards.clear();
        this.scoreboardModifiers.clear();
        this.rulesSlides.clear();
        this.rounds.clear();

        // Setup rounds
        this.rounds.addAll(minigame.buildRounds());

        // Setup rules slides
        this.rulesSlides.addAll(minigame.buildRulesSlides());
    }

    @SuppressWarnings("unchecked")
    public <T extends Minigame<?>> T getMinigame() {
        return (T) this.minigame;
    }

    public int getCurrentRoundIndex() {
        return this.currentRound;
    }

    @SuppressWarnings("unchecked")
    public <R extends Round> R getCurrentRound() {
        if (this.currentRound < 0 || this.currentRound >= this.rounds.size()) {
            return null;
        }

        return (R) this.rounds.get(this.currentRound);
    }

    public void setCurrentRound(int newRound) {
        if (newRound < 0 || newRound >= this.rounds.size()) {
            throw new IllegalArgumentException("New round must correspond to an existing round");
        }
        this.getLogger().info("Round transition from " + this.currentRound + " to " + newRound);
        RoundChangeEvent event = new RoundChangeEvent(this.currentRound, newRound);
        BukkitUtils.dispatchEvent(event);
        if (!event.isCancelled()) {
            if (this.currentRound >= 0) {
                this.rounds.get(this.currentRound).onCleanup();
            }
            this.currentRound = newRound;
            this.rounds.get(newRound).onSetup();
        }
    }

    public boolean isLastRound() {
        return this.currentRound == this.rounds.size() - 1;
    }

    public void nextRound() {
        this.setCurrentRound(this.getCurrentRoundIndex() + 1);
        this.setState(MinigameState.PRE_ROUND);
    }

    private void transitionToSetup() {
        // This is here to make sure it runs last
        this.registerSetupHandler(MinigameState.SETUP, this::transitionToWaitingForPlayers);

        // Actually transition
        this.setState(MinigameState.SETUP);
    }

    private void transitionToInGame() {
        this.setState(MinigameState.IN_GAME);
    }

    private void transitionToWaitingForPlayers() {
        this.setCurrentRound(this.initialRound);
        this.setState(MinigameState.WAITING_FOR_PLAYERS);
    }

    @SuppressWarnings("unchecked")
    private void waitForPlayers() {
        Collection<UUID> players = this.minigame.getParticipants();
        ScoreboardService.getInstance().getAllScoreboards().forEach(scoreboard -> {
            ValueEntry<Integer> entry = ((ValueEntry<Integer>) scoreboard.getEntry("playerCount"));
            if (entry != null) {
                entry.setValue((int) players.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count());
            }
        });
        if (this.minigame.getAutomaticShowRules() && players.stream().allMatch(uuid -> Bukkit.getPlayer(uuid) != null)) {
            this.markDoneWaitingForPlayers();
        }
    }

    public void markDoneWaitingForPlayers() {
        this.setState(MinigameState.RULES);
    }

    public void goToNextRound() {
        if (this.isLastRound()) {
            BukkitUtils.runNextTick(() -> this.setState(MinigameState.POST_GAME));
        } else {
            this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.minigame.getPostRoundDelay() * 50L, TimeUnit.MILLISECONDS, this::nextRound), mm("Next round in:"));
        }
    }

    public Collection<GameTeam> getGameTeams() {
        return TeamService.getInstance().getAllTeams().stream().filter(team -> !team.spectator()).toList();
    }

    private void handlePlayersReady() {
        this.setState(MinigameState.PRE_ROUND);
    }

    @SuppressWarnings("unchecked")
    private void handlePlayerReady(Player player, ReadyUpSession session) {
        ScoreboardService.getInstance().getAllScoreboards().forEach(scoreboard -> {
            ValueEntry<Integer> entry = ((ValueEntry<Integer>) scoreboard.getEntry("playerCount"));
            if (entry != null) {
                entry.setValue((int) session.getReadyPlayerCount());
            }
        });
    }

    public void pauseGame() {
        if (!this.state.isInGame()) {
            throw new IllegalStateException("Game cannot be paused now.");
        }

        this.unpausedState = this.state;
        this.setState(MinigameState.PAUSED);
    }

    public void unpauseGame() {
        if (this.state != MinigameState.PAUSED) {
            throw new IllegalStateException("Game cannot be unpaused now.");
        }

        this.setState(this.unpausedState);
    }

    public void endRound() {
        this.setState(MinigameState.POST_ROUND);
    }

    public void endPhase1() {
        this.setState(MinigameState.MID_GAME);
    }

    public void startPhase2() {
        this.setState(MinigameState.IN_GAME_2);
    }

    public void endPhase2() {
        this.setState(MinigameState.MID_GAME_2);
    }

    public void startPhase3() {
        this.setState(MinigameState.IN_GAME_3);
    }

    private void endGame() {
        this.minigame.handleMinigameOver();
    }

    /**
     * Apply new scoreboards upon state change.
     */
    @EventHandler
    private void onStateChange(StateChangeEvent.Post event) {
        ScoreboardService.getInstance().resetAllScoreboards();
        this.drawScoreboard(event.getNewState());
    }

    public void refreshScoreboard() {
        this.drawScoreboard(this.getCurrentState());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (this.state != MinigameState.WAITING_FOR_PLAYERS) {
            // Ensure new players are spectators
            if (TeamService.getInstance().getTeamOfPlayer(event.getPlayer()) == null) {
                TeamService.getInstance().joinTeam(event.getPlayer(), DefaultTeams.SPECTATOR);
            }

            // Apply spectator player state
            // TODO: determine if necessary
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
            if (team == null || team.spectator()) {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                return;
            }
        }

        // Apply player state
        IPlayerState playerState = this.minigame.buildPlayerState(this.state);
        if (playerState != null) {
            playerState.apply(event.getPlayer());
        }
        playerState = this.statePlayerStates.get(this.state);
        if (playerState != null) {
            playerState.apply(event.getPlayer());
        }

        // Update scoreboard
        this.refreshScoreboard();
    }

    public int getRoundCount() {
        return this.rounds.size();
    }

    public @Nullable AbstractTimer getTimer() {
        return this.timer;
    }

    public void setPointMultiplier(double multiplier) {
        ScoreService.getInstance().setMultiplier(multiplier);
    }

    public double getPointMultiplier() {
        return ScoreService.getInstance().getMultiplier();
    }

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    public int getGameNumber() {
        return this.gameNumber;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    @NotNull
    public String getTournamentName() {
        return this.tournamentName;
    }

    public void setMaxGameNumber(int maxGameNumber) {
        this.maxGameNumber = maxGameNumber;
    }

    public int getMaxGameNumber() {
        return this.maxGameNumber;
    }

    // TODO: migrate to Tournament Connector
//    public void storeStateInDatabase() {
//        Map<String, String> values = Map.of(
//            "minigameId", MinigameConstantsService.getInstance().getMinigameID(),
//            "roundNumber", String.valueOf(MinigameService.getInstance().getCurrentRoundIndex())
//        );
//
//        this.db.addAction(conn -> {
//            try (PreparedStatement statement = conn.prepareStatement(
//                "INSERT INTO mm_minigame_state (id, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = ?;"
//            )) {
//                conn.setAutoCommit(false);
//
//                for (Map.Entry<String, String> entry : values.entrySet()) {
//                    String id = entry.getKey();
//                    String value = entry.getValue();
//                    statement.setString(1, id);
//                    statement.setString(2, value);
//                    statement.setString(3, id);
//                    statement.addBatch();
//                }
//
//                statement.executeBatch();
//                conn.commit();
//            } catch (SQLException e) {
//                GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
//            }
//        });
//    }

    // TODO: migrate to Tournament Connector
//    public void loadInitialState(Connection conn) {
//        Map<String, String> values = new HashMap<>();
//
//        try (PreparedStatement statement = conn.prepareStatement(
//            "SELECT * FROM mm_minigame_state;"
//        )) {
//            ResultSet resultSet = statement.executeQuery();
//            while (resultSet.next()) {
//                values.put(resultSet.getString("id"), resultSet.getString("value"));
//            }
//        } catch (SQLException e) {
//            GameToolsPlugin.logger().log(Level.SEVERE, "Could not store state information", e);
//        }
//
//        this.setPointMultiplier(Double.parseDouble(values.get("pointMultiplier")));
//        this.setGameNumber(Integer.parseInt(values.get("gameNumber")));
//        this.setMaxGameNumber(Integer.parseInt(values.get("maxGameNumber")));
//        this.setTournamentName(values.get("tournamentName"));
//
//        if (Objects.equals(values.get("minigameId"), MinigameConstantsService.getInstance().getMinigameID())) {
//            this.initialRound = Integer.parseInt(values.get("roundNumber"));
//        }
//    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            // TODO: migrate to Tournament Connector
            // this.db = new MySQLDataManager<>(this, conn -> {
            // })
        );
    }

    @EventHandler
    private void onStateChange(StateChangeEvent event) {
        if (event.getNewState() == MinigameState.PRE_ROUND) {
            ScoreService.getInstance().resetRoundScores(this.getGameTeams());
        } else if (event.getNewState() == MinigameState.SETUP) {
            ScoreService.getInstance().resetGameScores(this.getGameTeams());
        }
    }

    public void earnPoints(UUID playerId, String reason, double points) {
        ScoreService.getInstance().earnPoints(playerId, reason, points, this.getCurrentRoundIndex());
    }

    public void earnPoints(Player player, String reason, double points) {
        ScoreService.getInstance().earnPoints(player, reason, points, this.getCurrentRoundIndex());
    }

    public boolean canSpectatorsOnlySeeAliveTeammates() {
        return this.spectatorsCanOnlySeeAliveTeammates;
    }

    public void setSpectatorsCanOnlySeeAliveTeammates(boolean spectatorsCanOnlySeeAliveTeammates) {
        this.spectatorsCanOnlySeeAliveTeammates = spectatorsCanOnlySeeAliveTeammates;
    }

    public Map<MinigameState, List<BiConsumer<MinigameState, GameScoreboard>>> getScoreboardModifiers() {
        return this.scoreboardModifiers;
    }

    public Component getTimerLabel() {
        return this.timerLabel;
    }
}