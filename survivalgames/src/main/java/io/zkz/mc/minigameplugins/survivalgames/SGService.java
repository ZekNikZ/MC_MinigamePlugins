package io.zkz.mc.minigameplugins.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.MinigameConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ObservableValueEntry;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.teams.event.TeamChangeEvent;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class SGService extends PluginService<SGPlugin> {
    private static final SGService INSTANCE = new SGService();

    public static SGService getInstance() {
        return INSTANCE;
    }

    private final List<SGRound> rounds = new ArrayList<>();
    private Location lobbySpawnLocation;

    private final ObservableValue<Integer> aliveTeamCount = new ObservableValue<>(-1);
    private final ObservableValue<Integer> alivePlayerCount = new ObservableValue<>(-1);

    private final Map<UUID, BukkitTask> disconnectedPlayers = new HashMap<>();
    private final Set<UUID> eatCooldown = new HashSet<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();
        minigame.setAutomaticPreRound(false);
        minigame.setAutomaticShowRules(false);
//        minigame.setAutomaticNextRound(false);
        minigame.setGlowingTeammates(true);
        minigame.setPostRoundDelay(1);

        MinigameConstantsService.getInstance().setMinigameID("survivalgames");
        MinigameConstantsService.getInstance().setMinigameName("Survival Games");

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(400);
        minigame.setPostRoundDelay(400);
        minigame.setPostGameDelay(600);

        // Player states
        BasicPlayerState adventureMode = new BasicPlayerState(GameMode.ADVENTURE);
        BasicPlayerState spectatorMode = new BasicPlayerState(GameMode.SPECTATOR);
        minigame.registerPlayerState(adventureMode,
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.PAUSED
        );
        minigame.addSetupHandler(MinigameState.IN_GAME, () -> {
            Bukkit.getOnlinePlayers().stream()
                .filter(this.getCurrentRound()::isAlive)
                .forEach(p -> p.setGameMode(GameMode.SURVIVAL));

            minigame.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, 15, TimeUnit.MINUTES));
        });
        minigame.registerPlayerState(spectatorMode,
            MinigameState.POST_ROUND,
            MinigameState.POST_GAME
        );

        // State tasks
        minigame.addTask(MinigameState.IN_GAME, SpectatorTask::new);

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            BukkitUtils.forEachPlayer(player -> {

                GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
                if (team.isSpectator()) {
                    this.activateSpectatorMode(player);
                }
            });
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
        minigame.registerGlobalScoreboard((state, scoreboard) -> {
        });
        BiConsumer<MinigameState, GameScoreboard> scoreboardModifier = (state, scoreboard) -> {
            scoreboard.addSpace();
            scoreboard.addEntry(new ObservableValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Teams Alive: " + ChatColor.RESET + "%s/" + MinigameService.getInstance().getPlayers().stream().map(TeamService.getInstance()::getTeamOfPlayer).map(GameTeam::getId).distinct().count(), this.aliveTeamCount));
            scoreboard.addEntry(new ObservableValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Players Alive: " + ChatColor.RESET + "%s/" + MinigameService.getInstance().getPlayers().size(), this.alivePlayerCount));
        };
        minigame.registerScoreboard(MinigameState.PRE_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.IN_GAME, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.PAUSED, scoreboardModifier);
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("arenas.json"), null, this::loadData)
        );
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> json) {
        this.rounds.clear();
        this.rounds.addAll(json.getArray("arenas").stream().map(obj -> new SGRound(new TypedJSONObject<Object>((JSONObject) obj))).toList());
        this.lobbySpawnLocation = adjustLocation(toLocation(JSONUtils.readBlockVector(json.getList("lobbySpawnLocation", Long.class)), "sg_lobby"));
    }

    @Override
    protected void onEnable() {
        // Register rounds
        MinigameService.getInstance().registerRounds(this.rounds.toArray(SGRound[]::new));
    }

    public SGRound getCurrentRound() {
        return (SGRound) MinigameService.getInstance().getCurrentRound();
    }

    private void setDead(Player player) {
        player.teleport(this.lobbySpawnLocation);
        BukkitUtils.forEachPlayer(p -> p.hidePlayer(this.getPlugin(), player));

        this.setDead(player.getUniqueId(), player.getDisplayName());
    }

    private void setDead(UUID playerId, String playerDisplayName) {
        SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);
        this.getCurrentRound().markDead(playerId);

        // Assign points to alive players
        this.getCurrentRound().getAlivePlayers().forEach(p -> MinigameService.getInstance().earnPoints(p, "survival", Points.SURVIVAL));

        // Elimination chat message
        Collection<? extends Player> noPointsPlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        Collection<? extends Player> pointsPlayers = this.getCurrentRound().getAliveOnlinePlayers();
        noPointsPlayers.removeAll(pointsPlayers);
        Chat.sendAlert(noPointsPlayers, ChatType.ELIMINATION, playerDisplayName + " was eliminated.");
        SoundUtils.playSound(pointsPlayers, StandardSounds.GOAL_MET_MINOR, 1, 1);
        Chat.sendAlert(pointsPlayers, ChatType.ELIMINATION, playerDisplayName + " was eliminated.", Points.SURVIVAL);

        // Player remaining message
        if (this.getCurrentRound().getAlivePlayers().size() <= 5) {
            Chat.sendAlert(ChatType.ALERT, this.getCurrentRound().getAlivePlayers().size() + " players remaining.");
        }

        // Team elimination message
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(playerId);
        if (!this.getCurrentRound().isTeamAlive(team)) {
            Chat.sendAlert(ChatType.TEAM_ELIMINATION, team.getDisplayName() + " was eliminated.");

            // Team remaining message
            if (this.getCurrentRound().getAliveTeams().size() <= 5) {
                Chat.sendAlert(ChatType.ALERT, this.getCurrentRound().getAlivePlayers().size() + " teams remaining.");
            }
        }

        this.updateGameState();
    }

    void setupPlayer(Player player) {
        // If in-game and alive, tp to map
        Location logoutLocation = this.getCurrentRound().getLogoutLocation(player);
        if ((MinigameService.getInstance().getCurrentState() == MinigameState.PRE_ROUND || (MinigameService.getInstance().getCurrentState().isInGame())) && this.getCurrentRound().isAlive(player)) {
            player.setGameMode(GameMode.SURVIVAL);
            // If have a logout location
            if (logoutLocation != null) {
                player.teleport(logoutLocation);
            } else {
                Location assignedSpawnLocation = this.getCurrentRound().getAssignedSpawnLocation(player);
                if (assignedSpawnLocation != null) {
                    player.teleport(assignedSpawnLocation);
                } else {
                    player.teleport(this.getCurrentRound().getCornLocation());
                }
            }
            return;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (team == null || team.isSpectator()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(GameMode.ADVENTURE);
        }

        // otherwise, tp to lobby
        player.teleport(this.lobbySpawnLocation);
    }

    public void updateGameState() {
        // Check teams
        Map<GameTeam, Long> aliveTeams = this.getCurrentRound().getAliveTeams();
        if (aliveTeams.size() <= 1) {
            this.roundIsOver();
        }

        // Update scoreboard
        this.alivePlayerCount.set(this.getCurrentRound().getAliveOnlinePlayers().size());
        this.aliveTeamCount.set(aliveTeams.size());
    }

    private void roundIsOver() {
        this.getCurrentRound().endRound();
    }

    public void activateSpectatorMode(Player player) {
        if (this.getCurrentRound().isAlive(player)) {
            Chat.sendMessage(player, ChatColor.RED + "You cannot spectate while you are still alive!");
            return;
        }

        if (!player.getWorld().getName().equals("sg_lobby")) {
            return;
        }

        player.teleport(Bukkit.getWorld(this.getCurrentRound().getTemplateWorldName()).getSpawnLocation());
        BukkitUtils.runNextTick(() -> {
            player.setGameMode(GameMode.SPECTATOR);
            Chat.sendAlert(player, ChatType.ACTIVE_INFO, ChatColor.GOLD + "You are now spectating. Use " + ChatColor.AQUA + "/lobby" + ChatColor.GOLD + " to return to the lobby.");
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
            if (team != null && this.getCurrentRound().isTeamAlive(team)) {
                player.setSpectatorTarget(this.getCurrentRound().getAliveOnlinePlayers().stream().filter(p -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(p), team)).findFirst().orElse(null));
            }

            PlayerUtils.hidePlayer(this.getPlugin(), player);
        });
    }

    public void deactivateSpectatorMode(Player player) {
        if (this.getCurrentRound().isAlive(player)) {
            Chat.sendMessage(player, ChatColor.RED + "You cannot leave the match!");
            return;
        }

        if (player.getWorld().getName().equals("sg_lobby")) {
            return;
        }

        this.setupPlayer(player);

        PlayerUtils.showPlayer(this.getPlugin(), player);
    }

    private boolean respawnTeammate(Player player) {
        if (!this.getCurrentRound().isAlive(player)) {
            return false;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (!this.getCurrentRound().isTeamAlive(team)) {
            return false;
        }

        AtomicBoolean status = new AtomicBoolean(false);
        TeamService.getInstance().getOnlineTeamMembers(team).stream()
            .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
            .findFirst().ifPresentOrElse(
                p -> {
                    this.getCurrentRound().respawnPlayer(p);
                    status.set(true);
                },
                () -> status.set(false)
            );
        return status.get();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.disconnectedPlayers.containsKey(player.getUniqueId())) {
            BukkitTask task = this.disconnectedPlayers.get(player.getUniqueId());
            if (task == null && this.getCurrentRound().isAlive(player)) {
                this.setDead(player);
            } else if (task != null) {
                task.cancel();
            }
            this.disconnectedPlayers.remove(player.getUniqueId());
        } else {
            this.setupPlayer(player);
        }

        MinigameService.getInstance().refreshGlowing();
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String playerDisplayName = player.getDisplayName();

        if (MinigameService.getInstance().getCurrentState().isInGame() && this.getCurrentRound().isAlive(playerId)) {
            this.getCurrentRound().recordLogoutLocation(player);
            this.disconnectedPlayers.put(playerId, new BukkitRunnable() {
                @Override
                public void run() {
                    disconnectedPlayers.put(playerId, null);
                    setDead(playerId, playerDisplayName);
                }
            }.runTaskLater(this.getPlugin(), 1800));
            Chat.sendAlert(ChatType.WARNING, player.getDisplayName() + ChatColor.RED + " has disconnected, waiting 90 seconds before eliminating them...");
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        BukkitUtils.runNextTick(() -> this.setupPlayer(event.getPlayer()));
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        if (MinigameService.getInstance().getCurrentState().isInGame() && this.getCurrentRound().isAlive(event.getEntity())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                this.getCurrentRound().recordKill(event.getEntity(), killer);
            }
            this.setDead(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        // Prevent players from moving in pre-game
        if (MinigameService.getInstance().getCurrentState() == MinigameState.PRE_ROUND && this.getCurrentRound().isAlive(event.getPlayer().getUniqueId())) {
            event.getTo().setX(event.getFrom().getX());
            event.getTo().setZ(event.getFrom().getZ());
        }
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!MinigameService.getInstance().getCurrentState().isInGame() || !this.getCurrentRound().isAlive(player)) {
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (this.getCurrentRound().isAlive(event.getPlayer())) {
            if (List.of(Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.FLOWERING_AZALEA_LEAVES, Material.DARK_OAK_LEAVES, Material.JUNGLE_LEAVES, Material.MANGROVE_LEAVES, Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.VINE).contains(event.getBlock().getType())) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    private void onSpectateTeleport(PlayerTeleportEvent event) {
        if (!MinigameService.getInstance().isSpectatorsCanOnlySeeAliveTeammates()) {
            return;
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            GameTeam team = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
            if (team != null && event.getPlayer().getSpectatorTarget() == null && this.getCurrentRound().isTeamAlive(team)) {
                event.getPlayer().setSpectatorTarget(this.getCurrentRound().getAliveOnlinePlayers().stream().filter(p -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(p), team)).findFirst().orElse(null));
            }
        }
    }

    @EventHandler
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!MinigameService.getInstance().isSpectatorsCanOnlySeeAliveTeammates()) {
            return;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR && this.getCurrentRound().isTeamAlive(team)) {
            event.setCancelled(true);
            event.getPlayer().setSpectatorTarget(this.getCurrentRound().getAliveOnlinePlayers().stream().filter(p -> Objects.equals(TeamService.getInstance().getTeamOfPlayer(p), team)).findFirst().orElse(null));
        }
    }

    @EventHandler
    private void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    private void onPlaceBlock(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.TNT) {
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().spawn(adjustLocation(event.getBlock().getLocation()), TNTPrimed.class);
        }
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        world.setDifficulty(Difficulty.HARD);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, Boolean.FALSE);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.FALSE);
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(0);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, Boolean.FALSE);
        world.setGameRule(GameRule.MOB_GRIEFING, Boolean.FALSE);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, Boolean.FALSE);
    }

    @EventHandler
    private void onEat(PlayerItemConsumeEvent event) {
//        if (event.getItem().getType() == Material.BEETROOT_SOUP) {
//            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 1));
//            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
//            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20, 4));
//        }
    }

    @EventHandler
    private void onUseItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (this.getCurrentRound().isAlive(player) && player.getInventory().getItemInMainHand().getType() == Material.NETHER_STAR) {
            if (this.respawnTeammate(player)) {
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                Chat.sendAlert(player, ChatType.SUCCESS, "Your teammate has been respawned!");
                SoundUtils.playSound(player, StandardSounds.GOAL_MET_MAJOR, 1, 1);
            } else {
                Chat.sendAlert(player, ChatType.WARNING, "Your teammate is either still alive or offline!");
                SoundUtils.playSound(player, StandardSounds.ALERT_ERROR, 1, 1);
            }
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.EMERALD) {
            if (this.eatCooldown.contains(player.getUniqueId())) {
                return;
            }
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 6, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
            SoundUtils.playSound(player, Sound.ENTITY_GENERIC_EAT, 1, 1);
            this.eatCooldown.add(player.getUniqueId());
            BukkitUtils.runLater(() -> {
                this.eatCooldown.remove(player.getUniqueId());
            }, 100);
        }
    }

    @EventHandler
    private void onTeamChange(TeamChangeEvent event) {
        event.getPlayers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(this::setupPlayer);
    }

    public static Location toLocation(BlockVector3 vec, String world) {
        return new Location(Bukkit.getWorld(world), vec.getX(), vec.getY(), vec.getZ());
    }

    public static Location adjustLocation(Location loc) {
        return loc.clone().add(0.5, 1, 0.5);
    }
}
