package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpSession;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.TimerEntry;
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
import io.zkz.mc.minigameplugins.minigamemanager.MinigameManagerPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.event.RoundChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.TeamBasedMinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.GameTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.RulesTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.ScoreSummaryTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MinigameService extends PluginService<MinigameManagerPlugin> {
    private static final MinigameService INSTANCE = new MinigameService();

    public static MinigameService getInstance() {
        return INSTANCE;
    }

    private static final TeamBasedMinigameScoreboard DEFAULT_SCOREBOARD = (team) -> {
        MinigameState currentState = getInstance().getCurrentState();
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.GOLD + ChatColor.BOLD + "MC Tournament 1");
        scoreboard.addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Game " + getInstance().getGameNumber() + "/" + getInstance().getMaxGameNumber() + ": " + ChatColor.RESET + ChatConstantsService.getInstance().getMinigameName());
        switch (currentState) {
            case SERVER_STARTING, LOADING -> {
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Server loading...");
            }
            case SETUP -> {
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Setting up minigame...");
            }
            case WAITING_FOR_PLAYERS -> {
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Waiting for players...");
                scoreboard.addSpace();
                scoreboard.addEntry("playerCount", new ValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Players: " + ChatColor.RESET + "%s/" + getInstance().getPlayers().size(), 0));
            }
            case RULES -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Showing rules...");
            }
            case WAITING_TO_BEGIN -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Waiting for ready...");
                scoreboard.addSpace();
                scoreboard.addEntry("playerCount", new ValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Ready players: " + ChatColor.RESET + "%s/" + getInstance().getPlayers().size(), 0));
            }
            case PRE_ROUND -> {
                addRoundInformation(scoreboard);
                scoreboard.addEntry(new TimerEntry("" + ChatColor.RED + ChatColor.BOLD + "Round begins in: " + ChatColor.RESET + "%s", getInstance().timer));
                addTeamInformation(scoreboard, team);
            }
            case IN_GAME -> {
                addRoundInformation(scoreboard);
                if (getInstance().timer != null) {
                    scoreboard.addEntry(new TimerEntry("" + ChatColor.RED + ChatColor.BOLD + "Time left: " + ChatColor.RESET + "%s", getInstance().timer));
                }
                addTeamInformation(scoreboard, team);
            }
            case PAUSED -> {
                addRoundInformation(scoreboard);
                scoreboard.addSpace();
                scoreboard.addEntry("" + ChatColor.RED + ChatColor.BOLD + "Game status:");
                scoreboard.addEntry("Paused");
                addTeamInformation(scoreboard, team);
            }
            case POST_ROUND -> {
                addRoundInformation(scoreboard);
                if (getInstance().timer != null) {
                    scoreboard.addEntry(new TimerEntry("" + ChatColor.RED + ChatColor.BOLD + "Next round in: " + ChatColor.RESET + "%s", getInstance().timer));
                }
                addTeamInformation(scoreboard, team);
            }
            case POST_GAME -> {
                addRoundInformation(scoreboard);
                scoreboard.addEntry(new TimerEntry("" + ChatColor.RED + ChatColor.BOLD + "Back to hub in: " + ChatColor.RESET + "%s", getInstance().timer));
                addTeamInformation(scoreboard, team);
            }
        }

        getInstance().scoreboardModifiers.get(currentState).forEach(consumer -> consumer.accept(currentState, scoreboard));

        getInstance().scoreboard = scoreboard;
        ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
    };

    private static void addRoundInformation(GameScoreboard scoreboard) {
        if (getInstance().getCurrentRound().getMapName() != null) {
            scoreboard.addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Map: " + ChatColor.RESET + getInstance().getCurrentRound().getMapName());
        }
        if (getInstance().getRoundCount() > 1) {
            scoreboard.addEntry("" + ChatColor.GREEN + ChatColor.BOLD + "Round: " + ChatColor.RESET + (getInstance().getCurrentRoundIndex() + 1) + "/" + getInstance().getRoundCount());
        }
    }

    private static void addTeamInformation(GameScoreboard scoreboard, GameTeam team) {
        // TODO: implement
        scoreboard.addSpace();
        scoreboard.addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Game Coins: " + ChatColor.RESET + "(" + ChatColor.YELLOW + getInstance().getPointMultiplier() + "x" + ChatColor.RESET + ")");
        scoreboard.addEntry(" 1. A");
        scoreboard.addEntry(" 2. B");
        scoreboard.addEntry(" 3. C");
        scoreboard.addEntry(" 4. D");
    }

    public float getPointMultiplier() {
        // TODO: use this
        return 1.0f;
    }

    public int getGameNumber() {
        // TODO: use this
        return this.gameNumber;
    }

    public int getMaxGameNumber() {
        return 6;
    }

    private MinigameState state = MinigameState.SERVER_STARTING;
    private final Map<MinigameState, List<Runnable>> stateSetupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Runnable>> stateCleanupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Supplier<GameTask>>> stateTasks = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, IPlayerState> statePlayerStates = new HashMap<>();
    private final Map<MinigameState, MinigameScoreboard> scoreboards = new HashMap<>();
    private final Map<MinigameState, List<BiConsumer<MinigameState, GameScoreboard>>> scoreboardModifiers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Set<GameTask> runningTasks = new HashSet<>();
    private int preRoundDelay = 200;
    private int postRoundDelay = 200;
    private int postGameDelay = 200;
    private final List<Round> rounds = new ArrayList<>();
    private final List<Character> rulesSlides = new ArrayList<>();
    private int currentRound = -1;
    private AbstractTimer timer;
    private GameScoreboard scoreboard;
    private int gameNumber = 0;

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    @Override
    public void onEnable() {
        // Setup
        this.setState(MinigameState.LOADING);

        // Schedule a transition to the setup phase
        BukkitUtils.runNextTick(this::transitionToSetup);

        // Add state handlers
        // ==================

        // WAITING_FOR_PLAYERS
        this.addTask(MinigameState.WAITING_FOR_PLAYERS, () -> new GameTask(1, 20) {
            @Override
            public void run() {
                waitForPlayers();
            }
        });

        // RULES
        this.addTask(MinigameState.RULES, RulesTask::new);

        // WAITING_TO_BEGIN
        this.addSetupHandler(MinigameState.WAITING_TO_BEGIN, () -> ReadyUpService.getInstance().waitForReady(this.getPlayers(), this::handlePlayersReady, this::handlePlayerReady));

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
        this.addCleanupHandler(MinigameState.PRE_ROUND, () -> {
            this.changeTimer(null);
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
        this.addSetupHandler(MinigameState.POST_GAME, () -> this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.postGameDelay * 50L + ScoreSummaryTask.SECONDS_PER_SLIDE * ScoreSummaryTask.NUM_SLIDES * 20, TimeUnit.MILLISECONDS, this::endGame)));
        this.addTask(MinigameState.POST_GAME, ScoreSummaryTask::new);
        // TODO: send back to hub
    }

    public void removeRunningTask(GameTask task) {
        this.runningTasks.remove(task);
    }

    private void changeTimer(AbstractTimer timer) {
        if (this.timer != null) {
            this.timer.stop();
        }

        this.timer = timer;
        if (this.timer != null) {
            this.timer.start();
        }
    }

    public void setState(MinigameState newState) {
        this.getLogger().info("State transition from " + this.state.name() + " to " + newState.name());
        StateChangeEvent.Pre preEvent = new StateChangeEvent.Pre(this.state, newState);
        BukkitUtils.dispatchEvent(preEvent);
        if (!preEvent.isCancelled()) {
            if (this.state != null) {
                this.runningTasks.forEach(BukkitRunnable::cancel);
                this.runningTasks.clear();
                this.stateCleanupHandlers.get(this.state).forEach(Runnable::run);
            }
            this.state = newState;
            this.stateSetupHandlers.get(newState).forEach(Runnable::run);
            this.stateTasks.get(this.state).forEach(t -> {
                GameTask task = t.get();
                task.start(this.getPlugin());
                this.getLogger().info("Started task with ID " + task.getTaskId() + " of type " + t.getClass().getName());
                this.runningTasks.add(task);
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

    public void addTask(MinigameState state, Supplier<GameTask> taskSupplier) {
        this.stateTasks.get(state).add(taskSupplier);
    }

    public void randomizeRoundOrder() {
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

    public void registerScoreboard(MinigameState state, BiConsumer<MinigameState, GameScoreboard> scoreboardModifier) {
        this.scoreboardModifiers.get(state).add(scoreboardModifier);
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
        ((ValueEntry<Integer>) this.scoreboard.getEntry("playerCount")).setValue((int) players.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count());
        if (players.stream().allMatch(uuid -> Bukkit.getPlayer(uuid) != null)) {
            this.setState(MinigameState.RULES);
        }
    }

    public Collection<GameTeam> getGameTeams() {
        return TeamService.getInstance().getAllTeams().stream().filter(team -> !Objects.equals(team, DefaultTeams.SPECTATOR)).toList();
    }

    public Collection<UUID> getPlayers() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> !Objects.equals(TeamService.getInstance().getTeamOfPlayer(uuid), DefaultTeams.SPECTATOR))
            .toList();
    }

    private void handlePlayersReady() {
        this.setState(MinigameState.PRE_ROUND);
    }

    private void handlePlayerReady(Player player, ReadyUpSession session) {
        ((ValueEntry<Integer>) this.scoreboard.getEntry("playerCount")).setValue((int) session.getReadyPlayerCount());
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

    private void endGame() {
        // TODO: score summary
//        Bukkit.shutdown();
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
        if (Objects.equals(TeamService.getInstance().getTeamOfPlayer(event.getPlayer()), DefaultTeams.SPECTATOR)) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            return;
        }

        // Apply player state
        IPlayerState playerState = this.statePlayerStates.get(this.state);
        if (playerState != null) {
            playerState.apply(event.getPlayer());
        }
    }

    public int getRoundCount() {
        return this.rounds.size();
    }
}
