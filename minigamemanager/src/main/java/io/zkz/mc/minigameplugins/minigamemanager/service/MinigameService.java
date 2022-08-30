package io.zkz.mc.minigameplugins.minigamemanager.service;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpService;
import io.zkz.mc.minigameplugins.gametools.readyup.ReadyUpSession;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.StringEntry;
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
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.MinigameManagerPlugin;
import io.zkz.mc.minigameplugins.minigamemanager.event.RoundChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.event.StateChangeEvent;
import io.zkz.mc.minigameplugins.minigamemanager.proxy.ProtocolLibProxy;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.TeamBasedMinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.TeamScoresScoreboardEntry;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.GameTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.RulesTask;
import io.zkz.mc.minigameplugins.minigamemanager.task.ScoreSummaryTask;
import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import net.md_5.bungee.api.ChatColor;
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

public class MinigameService extends PluginService<MinigameManagerPlugin> {
    private static final MinigameService INSTANCE = new MinigameService();

    public static MinigameService getInstance() {
        return INSTANCE;
    }

    private static final TeamBasedMinigameScoreboard DEFAULT_SCOREBOARD = (team) -> {
        MinigameState currentState = getInstance().getCurrentState();
        GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard("" + ChatColor.GOLD + ChatColor.BOLD + getInstance().getTournamentName());
        scoreboard.addEntry("gameName", new StringEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Game " + getInstance().getGameNumber() + "/" + getInstance().getMaxGameNumber() + ": " + ChatColor.RESET + ChatConstantsService.getInstance().getMinigameName()));
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
                if (getInstance().timer != null) {
                    scoreboard.addEntry(new TimerEntry("" + ChatColor.RED + ChatColor.BOLD + "Round begins in: " + ChatColor.RESET + "%s", getInstance().timer));
                } else {
                    scoreboard.addEntry(new StringEntry("" + ChatColor.RED + ChatColor.BOLD + "Round begins in: " + ChatColor.RESET + "waiting..."));
                }
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
                } else {
                    scoreboard.addEntry(new StringEntry("" + ChatColor.RED + ChatColor.BOLD + "Next round in: " + ChatColor.RESET + "waiting..."));
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

        ScoreboardService.getInstance().setTeamScoreboard(team.getId(), scoreboard);
    };

    private static void addRoundInformation(GameScoreboard scoreboard) {
        if (getInstance().getCurrentRound().getMapName() != null) {
            scoreboard.addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Map: " + ChatColor.RESET + getInstance().getCurrentRound().getMapName());
        }
        if (getInstance().getCurrentRound().getMapBy() != null) {
            scoreboard.addEntry("" + ChatColor.AQUA + ChatColor.BOLD + "Map by: " + ChatColor.RESET + getInstance().getCurrentRound().getMapBy());
        }
        if (getInstance().getRoundCount() > 1) {
            scoreboard.addEntry("" + ChatColor.GREEN + ChatColor.BOLD + "Round: " + ChatColor.RESET + (getInstance().getCurrentRoundIndex() + 1) + "/" + getInstance().getRoundCount());
        }
    }

    private static void addTeamInformation(GameScoreboard scoreboard, GameTeam team) {
        scoreboard.addSpace();
        scoreboard.addEntry(new TeamScoresScoreboardEntry(team));
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

    // ===============
    //  Minigame Info
    // ===============
    private String tournamentName = "MC Tournament 0";
    private int gameNumber = 0;
    private int maxGameNumber = 6;
    private double multiplier = 1.0;

    // ==========
    //  Settings
    // ==========
    private boolean automaticShowRules = true;
    private boolean automaticPreRound = true;
    private boolean automaticNextRound = true;
    private boolean glowingTeammates = true;
    private boolean spectatorsCanOnlySeeAliveTeammates = false;
    private boolean useSecondInGameState = false;

    public void setGameNumber(int gameNumber) {
        this.gameNumber = gameNumber;
    }

    @Override
    protected void onEnable() {
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
        this.addSetupHandler(MinigameState.WAITING_TO_BEGIN, () -> {
            if (this.automaticPreRound) {
                ReadyUpService.getInstance().waitForReady(this.getPlayersAndGameMasters(), this::handlePlayersReady, this::handlePlayerReady);
            } else {
                this.setState(MinigameState.PRE_ROUND);
            }
        });

        // PRE_ROUND
        this.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            this.getCurrentRound().onEnterPreRound();

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

            if (this.automaticPreRound) {
                this.startPreRoundTimer();
            } else {
                ReadyUpService.getInstance().waitForReady(this.getPlayersAndGameMasters(), () -> {
                    Chat.sendAlert(ChatType.GAME_INFO, "All players are now ready. Round starting in " + this.preRoundDelay / 20 + " seconds.");
                    this.startPreRoundTimer();
                });
            }
        });
        this.addCleanupHandler(MinigameState.PRE_ROUND, () -> {
            this.changeTimer(null);
        });

        // IN_GAME (note: these are registered for different states to prevent paused from triggering them)
        this.addCleanupHandler(MinigameState.PRE_ROUND, () -> this.getCurrentRound().onRoundStart());

        // MID_GAME
        this.addSetupHandler(MinigameState.MID_GAME, () -> this.getCurrentRound().onPhase1End());
        this.addCleanupHandler(MinigameState.MID_GAME, () -> this.getCurrentRound().onPhase2Start());

        // POST_ROUND
        this.addSetupHandler(MinigameState.POST_ROUND, () -> {
            this.getCurrentRound().onEnterPostRound();
            this.changeTimer(null);
            if (this.automaticNextRound) {
                this.goToNextRound();
            }
        });

        // PAUSE
        this.addSetupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onPause());
        this.addCleanupHandler(MinigameState.PAUSED, () -> this.rounds.get(this.currentRound).onUnpause());

        // POST_GAME
        this.addSetupHandler(MinigameState.POST_GAME, () -> this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.postGameDelay * 50L + ScoreSummaryTask.SECONDS_PER_SLIDE * ScoreSummaryTask.NUM_SLIDES * 20, TimeUnit.MILLISECONDS, this::endGame)));
        this.addTask(MinigameState.POST_GAME, ScoreSummaryTask::new);

        // ProtocolLib stuff
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            ProtocolLibProxy.setupGlowing(this.getPlugin());
        }
    }

    private void drawScoreboard(MinigameState state) {
        MinigameScoreboard scoreboard = this.scoreboards.get(state);
        if (scoreboard == null) {
            scoreboard = DEFAULT_SCOREBOARD;
        }
        scoreboard.setup();
    }

    private void startPreRoundTimer() {
        this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.preRoundDelay * 50L, TimeUnit.MILLISECONDS, this::transitionToInGame) {
            @Override
            protected void onUpdate() {
                if (getCurrentTimeMillis() <= 5000) {
                    SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);
                }

                getCurrentRound().onPreRoundTimerTick(getCurrentTimeMillis());

                super.onUpdate();
            }
        });
        this.drawScoreboard(MinigameState.PRE_ROUND);
    }

    public void removeRunningTask(GameTask task) {
        this.runningTasks.remove(task);
    }

    public void changeTimer(AbstractTimer timer) {
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
                this.runningTasks.forEach(task -> task.cancel(false));
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
                BukkitUtils.forEachPlayer(playerState::apply);
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

    public void registerRulesSlides(Collection<Character> c) {
        this.rulesSlides.addAll(c);
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

    @SuppressWarnings("unchecked")
    private void waitForPlayers() {
        Collection<UUID> players = this.getPlayers();
        ScoreboardService.getInstance().getAllScoreboards().forEach(scoreboard -> {
            ValueEntry<Integer> entry = ((ValueEntry<Integer>) scoreboard.getEntry("playerCount"));
            if (entry != null) {
                entry.setValue((int) players.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count());
            }
        });
        if (this.automaticShowRules && players.stream().allMatch(uuid -> Bukkit.getPlayer(uuid) != null)) {
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
            this.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, this.postRoundDelay * 50L, TimeUnit.MILLISECONDS, this::nextRound));
        }
    }

    public Collection<GameTeam> getGameTeams() {
        return TeamService.getInstance().getAllTeams().stream().filter(team -> !team.isSpectator()).toList();
    }

    public Collection<UUID> getPlayers() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                return team != null && !team.isSpectator();
            })
            .toList();
    }

    public Collection<UUID> getPlayersAndGameMasters() {
        Collection<UUID> players = TeamService.getInstance().getTrackedPlayers();
        return players.stream()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                return team != null && (team.equals(DefaultTeams.GAME_MASTER) || !team.isSpectator());
            })
            .toList();
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

    public void endPhase1() {
        this.setState(MinigameState.MID_GAME);
    }

    public void startPhase2() {
        this.setState(MinigameState.IN_GAME_2);
    }

    private void endGame() {
        // Send players back to hub
        SubAPI.getInstance().getRemotePlayers(players -> {
            players.forEach((playerId, player) -> {
                player.transfer("lobby");
            });
        });
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
        // Ensure new players are spectators
        if (TeamService.getInstance().getTeamOfPlayer(event.getPlayer()) == null) {
            TeamService.getInstance().joinTeam(event.getPlayer(), DefaultTeams.SPECTATOR);
        }

        // Apply spectator player state
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
        if (team == null || team.isSpectator()) {
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

    public @Nullable AbstractTimer getTimer() {
        return this.timer;
    }

    public void setAutomaticShowRules(boolean automaticShowRules) {
        this.automaticShowRules = automaticShowRules;
    }

    public void setAutomaticPreRound(boolean automaticPreRound) {
        this.automaticPreRound = automaticPreRound;
    }

    public void setAutomaticNextRound(boolean automaticNextRound) {
        this.automaticNextRound = automaticNextRound;
    }

    public void setGlowingTeammates(boolean glowingTeammates) {
        this.glowingTeammates = glowingTeammates;
        this.refreshGlowing();
    }

    public void refreshGlowing() {
        BukkitUtils.forEachPlayer(player -> {
            BukkitUtils.forEachPlayer(otherPlayer -> {
                if (player.equals(otherPlayer)) {
                    return;
                }

                boolean canSee = player.canSee(otherPlayer);
                player.hidePlayer(this.getPlugin(), otherPlayer);
                if (canSee) {
                    player.showPlayer(this.getPlugin(), otherPlayer);
                }
            });
        });
    }

    public boolean isGlowingEnabled() {
        return this.glowingTeammates;
    }

    public void setPointMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getPointMultiplier() {
        // TODO: fetch from DB
        return this.multiplier;
    }

    public int getGameNumber() {
        // TODO: fetch from DB
        return this.gameNumber;
    }

    @NotNull
    private String getTournamentName() {
        // TODO: fetch from DB
        return this.tournamentName;
    }

    public int getMaxGameNumber() {
        // TODO: fetch from DB
        return this.maxGameNumber;
    }
}
