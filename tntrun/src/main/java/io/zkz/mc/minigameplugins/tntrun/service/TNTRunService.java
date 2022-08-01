package io.zkz.mc.minigameplugins.tntrun.service;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.zkz.mc.minigameplugins.gametools.ChatConstantsService;
import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourcePackService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.scoreboard.entry.ObservableValueEntry;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.util.NMSUtils;
import io.zkz.mc.minigameplugins.gametools.util.ObservableValue;
import io.zkz.mc.minigameplugins.gametools.util.TitleUtils;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.RegionService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.BasicPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.tntrun.TNTRunRound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class TNTRunService extends TNTRunPluginService {
    private static final TNTRunService INSTANCE = new TNTRunService();

    public static TNTRunService getInstance() {
        return INSTANCE;
    }

    private final Set<UUID> alivePlayers = new HashSet<>();
    private final ObservableValue<Integer> alivePlayerCount = new ObservableValue<>(-1);

    @Override
    protected void setup() {
        MinigameService minigame = MinigameService.getInstance();

        ChatConstantsService.getInstance().setMinigameName("Spleef");

        // Rules slides
        char slide1 = ResourcePackService.getInstance().addCustomCharacterImage(this.getPlugin().getResourceAsStream("testinstructions.png"), 128, 128);
        char slide2 = ResourcePackService.getInstance().addCustomCharacterImage(this.getPlugin().getResourceAsStream("testinstructions2.png"), 128, 128);
        minigame.registerRulesSlides(slide1, slide2);
        minigame.setPreRoundDelay(400);
        minigame.setPostRoundDelay(400);
        minigame.setPostGameDelay(600);

        // Round
        minigame.registerRounds(
            new TNTRunRound(BlockVector3.at(-50, 90, -50), BlockVector3.at(50, 105, 50), BlockVector3.at(0, 101, 0), 95),
            new TNTRunRound(BlockVector3.at(-50, 90, -50), BlockVector3.at(50, 105, 50), BlockVector3.at(0, 101, 0), 95)
        );

        // Player states
        BasicPlayerState adventureMode = new BasicPlayerState(GameMode.ADVENTURE);
        BasicPlayerState spectatorMode = new BasicPlayerState(GameMode.SPECTATOR);
        minigame.registerPlayerState(adventureMode,
            MinigameState.SETUP,
            MinigameState.WAITING_FOR_PLAYERS,
            MinigameState.RULES,
            MinigameState.PRE_ROUND,
            MinigameState.WAITING_TO_BEGIN,
            MinigameState.IN_GAME,
            MinigameState.PAUSED
        );
        minigame.registerPlayerState(spectatorMode,
            MinigameState.POST_ROUND,
            MinigameState.POST_GAME
        );

        // Round setup handlers
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            // Setup alive players
            this.alivePlayers.clear();
            this.alivePlayers.addAll(minigame.getPlayers());
            this.alivePlayerCount.set(minigame.getPlayers().size());
        });

        // State change titles
        minigame.addSetupHandler(MinigameState.PRE_ROUND, () -> {
            SoundUtils.broadcastSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round starts in 20 seconds", ChatColor.GOLD + "Find a good starting position!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_ROUND, () -> {
            SoundUtils.broadcastSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Round over!", 10, 70, 20);
        });
        minigame.addSetupHandler(MinigameState.POST_GAME, () -> {
            SoundUtils.broadcastSound(StandardSounds.ALERT_INFO, 1, 1);
            TitleUtils.broadcastTitle(ChatColor.RED + "Game over!", ChatColor.GOLD + "Check the chat for score information.", 10, 70, 20);
        });
        
        // In game scoreboard
        minigame.registerScoreboard(MinigameState.IN_GAME, () -> {
            GameScoreboard scoreboard = ScoreboardService.getInstance().createNewScoreboard(ChatConstantsService.getInstance().getScoreboardTitle());

            scoreboard.addSpace();
            scoreboard.addEntry(new ObservableValueEntry<>(scoreboard, "Remaining players: ", this.alivePlayerCount));
            scoreboard.addSpace();

            ScoreboardService.getInstance().setGlobalScoreboard(scoreboard);
        });
    }

    public TNTRunRound getCurrentRound() {
        return (TNTRunRound) MinigameService.getInstance().getCurrentRound();
    }

    private void scheduleBlockForRemoval(Location location) {
        final double seconds = 0.5;
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            location.getWorld().setBlockData(location, Material.AIR.createBlockData());
            location.getWorld().setBlockData(location.clone().add(0, -1, 0), Material.AIR.createBlockData());
        }, (long) (seconds * 20));
    }

    public void setupArena(TNTRunRound round) {
        World world = WorldEditService.getInstance().wrapWorld(Bukkit.getWorlds().get(0));
        round.resetArena();

        // WorldGuard region
        if (RegionService.getInstance().getRegion(world, "arena") != null) {
            RegionService.getInstance().removeProtectedRegion(world, "arena");
        }
        ProtectedRegion protectedRegion = RegionService.getInstance().createProtectedRegion(world, "arena", round.getArenaMin(), round.getArenaMax());

        protectedRegion.setFlags(Map.of(
            Flags.BUILD, StateFlag.State.DENY,
            Flags.EXIT, StateFlag.State.DENY
        ));

        // TP players
        Bukkit.getOnlinePlayers().forEach(player -> player.teleport(new Location(Bukkit.getWorlds().get(0), round.getSpawnLocation().getX(), round.getSpawnLocation().getY(), round.getSpawnLocation().getZ())));

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.PEACEFUL);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRuleValue(GameRule.DO_WEATHER_CYCLE, false);
    }

    private void setDead(Player player) {
        player.teleport(new Location(Bukkit.getWorlds().get(0), this.getCurrentRound().getSpawnLocation().getX(), this.getCurrentRound().getSpawnLocation().getY(), this.getCurrentRound().getSpawnLocation().getZ()));
        player.setGameMode(GameMode.SPECTATOR);
        SoundUtils.broadcastSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);
        this.alivePlayers.remove(player.getUniqueId());

        // TODO: assign points

        this.updateGameState();
    }

    private void updateGameState() {
        // Update scoreboard
        this.alivePlayerCount.set(this.alivePlayers.size());

        // Check if round is over
        if (TeamService.getInstance().allSameTeam(this.alivePlayers)) {
            // TODO: assign round win points
            MinigameService.getInstance().endRound();
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        MinigameState currentState = MinigameService.getInstance().getCurrentState();

        event.getPlayer().teleport(new Location(Bukkit.getWorlds().get(0), 0, 101, 0));

        if (currentState == MinigameState.IN_GAME && this.alivePlayers.contains(event.getPlayer().getUniqueId())) {
            this.setDead(event.getPlayer());
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        this.setDead(event.getPlayer());
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (MinigameService.getInstance().getCurrentState() != MinigameState.IN_GAME || !this.alivePlayers.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getTo().getY() < this.getCurrentRound().getDeathYLevel()) {
            this.setDead(event.getPlayer());
            return;
        }

        // First, check if the player is just simply on a block
        Player player = event.getPlayer();
        Location blockOn = player.getLocation().add(new Vector(0, -1, 0));
        if (!blockOn.isWorldLoaded()) {
            return;
        }
        if (blockOn.getBlock().getType() == Material.SAND || blockOn.getBlock().getType() == Material.GRAVEL) {
            this.scheduleBlockForRemoval(blockOn);
            return;
        }

        // Otherwise, find the block the player is crouching on
        final AxisAlignedBB playerBB = NMSUtils.getEntityBoundingBox(player).d(0, -1, 0);
        Map<Block, AxisAlignedBB> blockBoxes = new HashMap<>();
        ArrayList<Block> supportingBlocks = new ArrayList<>();
        final Location cornerLoc = player.getLocation().clone().add(-1, -1, -1);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Block block = cornerLoc.clone().add(x, 0, z).getBlock();
                if (block.getType() != Material.AIR) {
                    AxisAlignedBB boundingBox = NMSUtils.getBlockBoundingBox(block);
                    blockBoxes.put(block, boundingBox);
                    player.sendMessage(block.getType() + " @ " + boundingBox.toString());
                }
            }
        }
        blockBoxes.forEach((block, blockBB) -> {
            if (playerBB.c(blockBB)) {
                supportingBlocks.add(block);
            }
        });

        supportingBlocks.forEach(block -> {
            if (block.getType() == Material.SAND || block.getType() == Material.GRAVEL) {
                this.scheduleBlockForRemoval(block.getLocation());
            }
        });

        // Naive approach
//        Location blockOn = event.getTo().clone().add(new Vector(0, -0.1, 0));
//        if (!blockOn.isWorldLoaded()) {
//            return;
//        }
//        Block block = blockOn.getBlock();
//        Location blockLocation = block.getLocation();
//
//        if (block.getType() == Material.AIR) {
//            List<Location> otherOptions = List.of(
//                blockLocation.add(1, 0, 0).getBlock().getLocation(),
//                blockLocation.add(-1, 0, 0).getBlock().getLocation(),
//                blockLocation.add(0, 0, -1).getBlock().getLocation(),
//                blockLocation.add(0, 0, 1).getBlock().getLocation()
//            );
//
//            Location finalBlockOn = blockOn;
//            blockOn = otherOptions.stream().map(b -> b.add(new Vector(0.5, 0.5, 0.5))).min(Comparator.comparing(b -> b.distance(finalBlockOn))).get();
//        }
//
//        if (blockOn.getBlock().getType() == Material.SAND || blockOn.getBlock().getType() == Material.GRAVEL) {
//            this.scheduleBlockForRemoval(blockOn);
//        }
    }
}
