package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ObservableValueEntry;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.battlebox.round.RoundType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class TGTTOSService extends PluginService<TGTTOSPlugin> {
    private static final TGTTOSService INSTANCE = new TGTTOSService();

    public static TGTTOSService getInstance() {
        return INSTANCE;
    }

    private final ObservableValue<Integer> finishedPlayerCount = new ObservableValue<>(0);

    private final List<TGTTOSRound> rounds = new ArrayList<>();

    private final Map<UUID, BukkitTask> boatRemoval = new HashMap<>();

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();
        minigame.registerRounds(this.rounds.toArray(TGTTOSRound[]::new));

        ChatConstantsService.getInstance().setMinigameName("TGTTOS");

        // Rules slides
        minigame.registerRulesSlides(ResourceAssets.SLIDES);
        minigame.setPreRoundDelay(400);
        minigame.setPostRoundDelay(200);
        minigame.setPostGameDelay(600);

        // Player states
        minigame.registerPlayerState(new BasicPlayerState(GameMode.ADVENTURE),
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.PAUSED
        );
        minigame.registerPlayerState(new BasicPlayerState(GameMode.SURVIVAL),
            MinigameState.IN_GAME
        );
        minigame.registerPlayerState(new BasicPlayerState(GameMode.SPECTATOR),
            MinigameState.POST_GAME,
            MinigameState.POST_ROUND
        );

        // Round setup handlers
        minigame.addSetupHandler(MinigameState.PRE_ROUND, this::updateFinishedPlayerCount);

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round starts in 20 seconds", ChatColor.GOLD + "Find a good starting position!", 10, 70, 20);
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
            scoreboard.addEntry(new ObservableValueEntry<>("" + ChatColor.GREEN + ChatColor.BOLD + "Players Finished: " + ChatColor.RESET + "%s/" + MinigameService.getInstance().getPlayers().size(), this.finishedPlayerCount));
        };
        minigame.registerScoreboard(MinigameState.PRE_ROUND, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.IN_GAME, scoreboardModifier);
        minigame.registerScoreboard(MinigameState.PAUSED, scoreboardModifier);
    }

    @Override
    protected void onEnable() {
        MinigameService minigame = MinigameService.getInstance();
        minigame.registerRounds(this.rounds.toArray(TGTTOSRound[]::new));
        minigame.randomizeRoundOrder();
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("arenas.json"), null, this::loadData)
        );
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> object) {
        this.rounds.clear();
        this.rounds.addAll(object.getArray("arenas").stream().map(obj -> {
            TypedJSONObject<Object> round = new TypedJSONObject<Object>((JSONObject) obj);
            String type = round.getString("type");
            return this.createRound(type, round);
        }).toList());
    }

    private TGTTOSRound createRound(String type, TypedJSONObject<Object> json) {
        return RoundType.valueOf(type.toUpperCase()).create(json);
    }

    public TGTTOSRound getCurrentRound() {
        return (TGTTOSRound) MinigameService.getInstance().getCurrentRound();
    }

    public void updateFinishedPlayerCount() {
        this.finishedPlayerCount.set(MinigameService.getInstance().getPlayers().size() - this.getCurrentRound().getAlivePlayers().size());
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setDamage(0);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (!this.getCurrentRound().isAlive(player)) {
            return;
        }

        if (this.getCurrentRound().isPlayerInEndRegion(player)) {
            this.getCurrentRound().onPlayerFinishCourse(player);
        } else if (loc.getY() <= this.getCurrentRound().getDeathYLevel()) {
            this.getCurrentRound().onPlayerFallOff(player);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.getCurrentRound().setupPlayerLocation(event.getPlayer());
    }

    @EventHandler
    private void onItemSlotMove(InventoryClickEvent event) {
        if (!(MinigameService.getInstance().getCurrentState().isInGame() || MinigameService.getInstance().getCurrentState() == MinigameState.PRE_ROUND || MinigameService.getInstance().getCurrentState() == MinigameState.POST_ROUND)) {
            return;
        }

        if (event.getView().getBottomInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() != Material.AIR) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        event.getPlayer().getInventory().getItemInOffHand().setAmount(64);
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (this.getCurrentRound().isAlive(event.getPlayer())) {
            if (BlockUtils.isWool(event.getBlock().getType())) {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    private void onBoatLeaveEvent(VehicleExitEvent event) {
        Vehicle vehicle = event.getVehicle();
        if ((event.getExited() instanceof Player player) && (vehicle.getType() == EntityType.BOAT)) {
            vehicle.remove();
            this.getCurrentRound().setupPlayerInventory(player);
        }
    }

    @EventHandler
    private void onBoatRideEvent(VehicleEnterEvent event) {
        this.boatRemoval.remove(event.getVehicle().getUniqueId()).cancel();
    }

    @EventHandler
    private void onBoatCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        this.boatRemoval.put(vehicle.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                vehicle.remove();
                BukkitUtils.forEachPlayer(player -> {
                    ItemStack stack = player.getInventory().getItem(0);
                    if (stack == null || stack.getType() != Material.OAK_BOAT) {
                        if (player.getVehicle() == null) {
                            getCurrentRound().setupPlayerInventory(player);
                        }
                    }
                });
            }
        }.runTaskLater(this.getPlugin(), 60));
    }
}
