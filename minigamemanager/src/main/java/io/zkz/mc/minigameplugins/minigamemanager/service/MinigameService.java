package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.event.RoundChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.GameTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.RulesTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class MinigameService extends MinigameManagerService {
    private static final MinigameService INSTANCE = new MinigameService();
    private int preRoundDelay = 100;
    private int postRoundDelay = 100;

    public static MinigameService getInstance() {
        return INSTANCE;
    }

    private MinigameState state;
    private final Map<MinigameState, List<Runnable>> stateSetupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<Runnable>> stateCleanupHandlers = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, List<GameTask>> stateTasks = Arrays.stream(MinigameState.values()).collect(Collectors.toMap(s -> s, s -> new ArrayList<>()));
    private final Map<MinigameState, IPlayerState> statePlayerStates = new HashMap<>();

    private final List<Round> rounds = new ArrayList<>();
    private int currentRound = -1;

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
        this.addSetupHandler(MinigameState.PRE_ROUND, () -> Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> this.setState(MinigameState.IN_GAME), this.preRoundDelay));

        // POST_ROUND
        this.addSetupHandler(MinigameState.POST_ROUND, this::nextRound);

        // IN_GAME
        this.addSetupHandler(MinigameState.IN_GAME, () -> this.rounds.get(this.currentRound).onStart());
        this.addCleanupHandler(MinigameState.IN_GAME, () -> this.rounds.get(this.currentRound).onEnd());

        // PAUSE
        this.addSetupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onPause());
        this.addCleanupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onUnpause());

    }

    public void setState(MinigameState newState) {
        StateChangeEvent event = new StateChangeEvent(this.state, newState);
        BukkitUtils.dispatchEvent(event);
        if (!event.isCancelled()) {
            this.stateTasks.get(this.state).forEach(BukkitRunnable::cancel);
            this.stateCleanupHandlers.get(this.state).forEach(Runnable::run);
            this.state = newState;
            this.stateSetupHandlers.get(newState).forEach(Runnable::run);
            this.stateTasks.get(this.state).forEach(t -> t.start(this.getPlugin()));
            IPlayerState playerState = this.statePlayerStates.get(this.state);
            if (playerState != null) {
                Bukkit.getOnlinePlayers().forEach(playerState::apply);
            }
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        // Ensure new players are spectators
        if (TeamService.getInstance().getTeamOfPlayer(event.getPlayer()) == null) {
            TeamService.getInstance().joinTeam(event.getPlayer(), DefaultTeams.SPECTATOR);
        }

        // TODO: apply spectator player state

        // Apply player state
        IPlayerState playerState = this.statePlayerStates.get(this.state);
        if (playerState != null) {
            playerState.apply(event.getPlayer());
        }
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

    public int getCurrentRound() {
        return this.currentRound;
    }

    public void setCurrentRound(int newRound) {
        if (newRound < 0 || newRound >= this.rounds.size()) {
            throw new IllegalArgumentException("New round must correspond to an existing round");
        }
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
        if (this.isLastRound()) {
            this.setState(MinigameState.POST_GAME);
        } else {
            this.setCurrentRound(this.getCurrentRound() + 1);
            this.setState(MinigameState.PRE_ROUND);
        }
    }

    private void transitionToSetup() {
        this.addSetupHandler(MinigameState.SETUP, this::transitionToWaitingForPlayers);
        this.setState(MinigameState.SETUP);
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

    private Collection<UUID> getPlayers() {
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

    public void setRoundDelay(int preRoundInTicks, int postRoundInTicks) {
        this.preRoundDelay = preRoundInTicks;
        this.postRoundDelay = postRoundInTicks;
    }
}
