package io.zkz.mc.minigameplugins.bingo;

import io.zkz.mc.minigameplugins.bingo.map.BingoCardMap;
import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONArray;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.event.PlayerInventoryChangeEvent;
import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourcePackService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BingoService extends PluginService<BingoPlugin> {
    private static final BingoService INSTANCE = new BingoService();

    public static BingoService getInstance() {
        return INSTANCE;
    }

    private final List<BingoRound> rounds = new ArrayList<>();

    @Override
    protected void setup() {
        // Setup scoreboard and state hooks
        MinigameService minigame = MinigameService.getInstance();

        ChatConstantsService.getInstance().setMinigameName("Bingo");

        // Rules slides
        char slide1 = ResourcePackService.getInstance().addCustomCharacterImage(this.getPlugin().getResourceAsStream("testinstructions.png"), 200, 200);
        char slide2 = ResourcePackService.getInstance().addCustomCharacterImage(this.getPlugin().getResourceAsStream("testinstructions2.png"), 200, 200);
        minigame.registerRulesSlides(slide1, slide2);
        minigame.setPreRoundDelay(400);
        minigame.setPostRoundDelay(400);
        minigame.setPostGameDelay(600);

        // Player states
        BasicPlayerState survivalMode = new BasicPlayerState(GameMode.SURVIVAL, new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true));
        BasicPlayerState adventureMode = new BasicPlayerState(GameMode.ADVENTURE, new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true));
        BasicPlayerState spectatorMode = new BasicPlayerState(GameMode.SPECTATOR, new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true));
        minigame.registerPlayerState(adventureMode,
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.PAUSED
        );
        minigame.registerPlayerState(survivalMode,
            MinigameState.IN_GAME
        );
        minigame.registerPlayerState(spectatorMode,
            MinigameState.POST_ROUND,
            MinigameState.POST_GAME
        );

        // State change handlers
        minigame.addSetupHandler(MinigameState.IN_GAME, () -> {
            minigame.changeTimer(new GameCountdownTimer(this.getPlugin(), 20, 12, TimeUnit.MINUTES, minigame::endRound));
            minigame.getTimer().addHook(new Runnable() {
                private int minAlert = 6;
                private int secAlert = 30;

                @SuppressWarnings("StatementWithEmptyBody")
                @Override
                public void run() {
                    long secondsRemaining = minigame.getTimer().getCurrentTime(TimeUnit.SECONDS);

                    if (secondsRemaining == 0) {
                        // intentionally empty
                    } else if (minAlert == 6 && secondsRemaining <= minAlert * 60L) {
                        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                        Chat.sendAlert(ChatType.WARNING, "6 minutes remaining..");
                        minAlert = 2;
                    } else if (secondsRemaining <= minAlert * 60L) {
                        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                        Chat.sendAlert(ChatType.WARNING, minAlert + " minute" + (minAlert == 1 ? "" : "s") + " remaining.");
                        --minAlert;
                    } else if (secAlert == 30 && secondsRemaining <= secAlert) {
                        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                        Chat.sendAlert(ChatType.WARNING, "30 seconds remaining.");
                        secAlert = 10;
                    } else if (secondsRemaining <= secAlert) {
                        SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                        Chat.sendAlert(ChatType.WARNING, secAlert + " second" + (secAlert == 1 ? "" : "s") + " remaining.");
                        --secAlert;
                    }
                }
            });
        });
        minigame.addSetupHandler(MinigameState.PAUSED, () -> {
            if (minigame.getTimer() != null) {
                minigame.getTimer().pause();
            }
        });
        minigame.addCleanupHandler(MinigameState.PAUSED, () -> {
            if (minigame.getTimer() != null) {
                minigame.getTimer().unpause();
            }
        });

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round starts in 20 seconds", ChatColor.GOLD + "Get ready to go!", 10, 70, 20);
        });
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
        // Register rounds
        MinigameService.getInstance().registerRounds(this.rounds.toArray(BingoRound[]::new));
        MinigameService.getInstance().randomizeRoundOrder();
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("arenas.json"), this::saveData, this::loadData)
        );
    }

    private JSONObject saveData() {
        return new JSONObject(Map.of(
            "arenas", new TypedJSONArray<>(this.rounds.stream().map(BingoRound::toJSON))
        ));
    }

    @SuppressWarnings("unchecked")
    private void loadData(TypedJSONObject<Object> object) {
        this.rounds.clear();
        this.rounds.addAll(object.getArray("arenas").stream().map(obj -> {
            TypedJSONObject<Object> round = new TypedJSONObject<Object>((JSONObject) obj);
            return new BingoRound(round);
        }).toList());
    }

    public void handleInventoryChange(Player player, PlayerInventory inventory) {
        ((BingoRound) MinigameService.getInstance().getCurrentRound()).checkForItemCollection(player, inventory);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (MinigameService.getInstance().getCurrentState().isInGame()) {
            event.getPlayer().getInventory().remove(Material.FILLED_MAP);
            if (event.getPlayer().getInventory().getItemInOffHand().getType() == Material.AIR) {
                event.getPlayer().getInventory().setItemInOffHand(BingoCardMap.makeMap());
            } else {
                event.getPlayer().getInventory().addItem(BingoCardMap.makeMap());
            }
        } else {
            event.getPlayer().getInventory().clear();
        }
    }

    @EventHandler
    private void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInventoryChange(PlayerInventoryChangeEvent event) {
        if (MinigameService.getInstance().getCurrentState() != MinigameState.IN_GAME) {
            return;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
        if (team == null || team.equals(DefaultTeams.SPECTATOR)) {
            return;
        }

        this.handleInventoryChange(event.getPlayer(), event.getInventory());
    }

    @EventHandler
    private void onPVP(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.IRON_ORE || event.getBlock().getType() == Material.DEEPSLATE_IRON_ORE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ISB.stack(Material.IRON_INGOT));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        } else if (event.getBlock().getType() == Material.GOLD_ORE || event.getBlock().getType() == Material.DEEPSLATE_GOLD_ORE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ISB.stack(Material.GOLD_INGOT));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        } else if (event.getBlock().getType() == Material.COPPER_ORE || event.getBlock().getType() == Material.DEEPSLATE_COPPER_ORE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ISB.stack(Material.GOLD_INGOT));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true));
        }, 1);
    }
}
