package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.TimerEntry;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ValueEntry;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.AbstractTimer;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.minigamemanager.event.RoundChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.StateBasedMinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.GameTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.RulesTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MinigameService extends MinigameManagerService {
    private static final MinigameService INSTANCE = new MinigameService();

    public static MinigameService getInstance() {
        return INSTANCE;
    }

    private static final StateBasedMinigameScoreboard DEFAULT_SCOREBOARD = (currentState) -> {
        switch (currentState) {
            case LOADING, SETUP, WAITING_FOR_PLAYERS, RULES, WAITING_TO_BEGIN, IN_GAME, PAUSED -> {
                GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(ChatConstantsService.getInstance().getScoreboardTitle());

                scoreboard.addSpace();
                scoreboard.addEntry(new ValueEntry<>(scoreboard, "State: ", currentState.toString()).setValueColor(ChatColor.YELLOW));
                scoreboard.addSpace();

                ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
            }
            case PRE_ROUND, POST_ROUND, POST_GAME -> {
                GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(ChatConstantsService.getInstance().getScoreboardTitle());

                scoreboard.addSpace();
                scoreboard.addEntry(new ValueEntry<>(scoreboard, "State: ", currentState.toString()).setValueColor(ChatColor.YELLOW));
                scoreboard.addEntry(new TimerEntry(scoreboard, "Time: ", getInstance().timer).setValueColor(ChatColor.YELLOW));
                scoreboard.addSpace();

                ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
            }
        }
    };

    private MinigameState state = MinigameState.SERVER_STARTING;
    private final Map<MinigameState, List<Runnable>> stateSetupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Runnable>> stateCleanupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<GameTask>> stateTasks = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, IPlayerState> statePlayerStates = new HashMap<>();
    private final Map<MinigameState, MinigameScoreboard> scoreboards = new HashMap<>();
    private int preRoundDelay = 200;
    private int postRoundDelay = 200;
    private int postGameDelay = 200;
    private final List<Round> rounds = new ArrayList<>();
    private final List<Character> rulesSlides = new ArrayList<>();
    private int currentRound = -1;
    private AbstractTimer timer;

    @Override
    public void onEnable() {
        // Setup
        this.setState(MinigameState.LOADING);

        // Schedule a transition to the setup phase
        BukkitUtils.runNextTick(this::transitionToSetup);

        // Add state handlers
        // ==================

        // WAITING_FOR_PLAYERS
        this.addTask(MinigameState.WAITING_FOR_PLAYERS, new GameTask(1, 20) {
            @Override
            public void run() {
                waitForPlayers();
            }
        });

        // RULES
        this.addTask(MinigameState.RULES, new RulesTask());

        // WAITING_TO_BEGIN
        this.addSetupHandler(MinigameState.WAITING_TO_BEGIN, () -> ReadyUpService.getInstance().waitForReady(this.getPlayers(), this::handlePlayersReady));

        // PRE_ROUND
        this.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            this.getCurrentRound().onPreRound();
            this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.preRoundDelay * 50L, TimeUnit.MILLISECONDS, this::transitionToInGame) {
                @Override
                protected void onUpdate() {
                    if (getCurrentTimeMillis() <= 5000) {
                        SoundUtils.broadcastSound(StandardSounds.TIMER_TICK, 1, 1);
                    }

                    super.onUpdate();
                }
            });
        });

        // POST_ROUND
        this.addSetupHandler(MinigameState.POST_ROUND, () -> {
            this.getCurrentRound().onPostRound();
            if (this.isLastRound()) {
                BukkitUtils.runNextTick(() -> this.setState(MinigameState.POST_GAME));
            } else {
                this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.postRoundDelay * 50L, TimeUnit.MILLISECONDS, this::nextRound));
            }
        });

        // IN_GAME (note: these are registered for different states to prevent paused from triggering them)
        this.addCleanupHandler(MinigameState.PRE_ROUND, () -> this.rounds.get(this.currentRound).onStart());
        this.addSetupHandler(MinigameState.POST_ROUND, () -> this.rounds.get(this.currentRound).onEnd());

        // PAUSE
        this.addSetupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onPause());
        this.addCleanupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onUnpause());

        // POST_GAME
        this.addSetupHandler(MinigameState.POST_GAME, () -> this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.postGameDelay * 50L, TimeUnit.MILLISECONDS, Bukkit::shutdown)));
        // TODO: send back to hub
    }

    private void changeTimer(AbstractTimer timer) {
        if (this.timer != null) {
            this.timer.stop();
        }

        this.timer = timer;
        timer.start();
    }

    public void setState(MinigameState newState) {
        this.getLogger().info("State transition from " + this.state.name() + " to " + newState.name());
        StateChangeEvent.Pre preEvent = new StateChangeEvent.Pre(this.state, newState);
        BukkitUtils.dispatchEvent(preEvent);
        if (!preEvent.isCancelled()) {
            if (this.state != null) {
                this.stateTasks.get(this.state).forEach(BukkitRunnable::cancel);
                this.stateCleanupHandlers.get(this.state).forEach(Runnable::run);
            }
            this.state = newState;
            this.stateSetupHandlers.get(newState).forEach(Runnable::run);
            this.stateTasks.get(this.state).forEach(t -> {
                if (!t.isScheduled()) {
                    t.start(this.getPlugin());
                    this.getLogger().info("Started task with ID " + t.getTaskId());
                }
            });
            IPlayerState playerState = this.statePlayerStates.get(this.state);
            if (playerState != null) {
                Bukkit.getOnlinePlayers().forEach(playerState::apply);
            }
        }
        BukkitUtils.dispatchEvent(new StateChangeEvent.Post(this.state, newState));
    }

    public MinigameState getCurrentState() {
        return this.state;
    }

    public void addSetupHandler(MinigameState state, Runnable handler) {
        this.stateSetupHandlers.get(state).add(handler);
    }

    public void addCleanupHandler(MinigameState state, Runnable handler) {
        this.stateCleanupHandlers.get(state).add(handler);
    }

    public void addTask(MinigameState state, GameTask task) {
        this.stateTasks.get(state).add(task);
    }

    public void randomizeRounds() {
        Collections.shuffle(this.rounds);
    }

    public void registerRounds(Round... rounds) {
        this.rounds.addAll(Arrays.asList(rounds));
    }

    public void registerRulesSlides(Character... c) {
        this.rulesSlides.addAll(Arrays.asList(c));
    }

    public List<Character> getRulesSlides() {
        return this.rulesSlides;
    }

    public void registerPlayerState(IPlayerState playerState, MinigameState... states) {
        Arrays.stream(states).forEach(state -> this.statePlayerStates.put(state, playerState));
    }

    public void setPreRoundDelay(int delayInTicks) {
        this.preRoundDelay = delayInTicks;
    }

    public void setPostRoundDelay(int delayInTicks) {
        this.postRoundDelay = delayInTicks;
    }

    public void setPostGameDelay(int delayInTicks) {
        this.postGameDelay = delayInTicks;
    }

    public void registerScoreboard(MinigameState state, MinigameScoreboard scoreboard) {
        this.scoreboards.put(state, scoreboard);
    }

    public int getCurrentRoundIndex() {
        return this.currentRound;
    }

    public Round getCurrentRound() {
        if (this.currentRound < 0 || this.currentRound >= this.rounds.size()) {
            return null;
        }

        return this.rounds.get(this.currentRound);
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
        this.addSetupHandler(MinigameState.SETUP, this::transitionToWaitingForPlayers);

        // Actually transition
        this.setState(MinigameState.SETUP);
    }

    private void transitionToInGame() {
        this.setState(MinigameState.IN_GAME);
    }

    private void transitionToWaitingForPlayers() {
        this.setCurrentRound(0);
        this.setState(MinigameState.WAITING_FOR_PLAYERS);
    }

    private void waitForPlayers() {
        Collection<UUID> players = this.getPlayers();
        if (players.stream().allMatch(uuid -> Bukkit.getPlayer(uuid) != null)) {
            this.setState(MinigameState.RULES);
        }
    }

    public Collection<UUID> getPlayers() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> TeamService.getInstance().getTeamOfPlayer(uuid) != DefaultTeams.SPECTATOR)
            .toList();
    }

    private void handlePlayersReady() {
        this.setState(MinigameState.PRE_ROUND);
    }

    public void pauseGame() {
        if (this.state != MinigameState.IN_GAME) {
            throw new IllegalStateException("Game cannot be paused now.");
        }

        this.setState(MinigameState.PAUSED);
    }

    public void unpauseGame() {
        if (this.state != MinigameState.PAUSED) {
            throw new IllegalStateException("Game cannot be unpaused now.");
        }

        this.setState(MinigameState.IN_GAME);
    }

    public void endRound() {
        this.setState(MinigameState.POST_ROUND);
    }

    /**
     * Apply new scoreboards upon state change.
     */
    @EventHandler
    private void onStateChange(StateChangeEvent.Post event) {
        ScoreboardService.getInstance().resetAllScoreboards();
        MinigameScoreboard scoreboard = this.scoreboards.get(event.getNewState());
        if (scoreboard == null) {
            scoreboard = DEFAULT_SCOREBOARD;
        }
        scoreboard.setup();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        // Ensure new players are spectators
        if (TeamService.getInstance().getTeamOfPlayer(event.getPlayer()) == null) {
            TeamService.getInstance().joinTeam(event.getPlayer(), DefaultTeams.SPECTATOR);
        }

        // Apply spectator player state
        if (TeamService.getInstance().getTeamOfPlayer(event.getPlayer()) == DefaultTeams.SPECTATOR) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            return;
        }

        // Apply player state
        IPlayerState playerState = this.statePlayerStates.get(this.state);
        if (playerState != null) {
            playerState.apply(event.getPlayer());
        }
    }
}
