package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.state.PlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.Round;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class PotionPanicRound extends Round {
    private int numThrows = 0;
    private GameTeam winner;
    private World world;
    private boolean isFirstPotion = true;
    private boolean roundCompleted = false;

    public PotionPanicRound() {
        super(null);
    }

    @Override
    public void onSetup() {
        this.world = Bukkit.getWorld("potionpanic");

        // Reset floor
        SchematicService.getInstance().placeSchematic(PotionPanicRound.class.getResourceAsStream("/floor.schem"), Locations.SCHEMATIC_ORIGIN);

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.EASY);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.NATURAL_REGENERATION, false);
    }

    @Override
    protected Collection<UUID> getInitialAlivePlayers() {
        return Stream.concat(
            TeamService.getInstance().getTeamMembers(PotionPanicService.getInstance().getTeam1()).stream(),
            TeamService.getInstance().getTeamMembers(PotionPanicService.getInstance().getTeam2()).stream()
        ).toList();
    }

    @Override
    public void onEnterPreRound() {
        super.onEnterPreRound();

        // Clear inventories
        this.world.getEntitiesByClass(Item.class).forEach(Entity::remove);
        BukkitUtils.forEachPlayer(player -> player.getInventory().clear());

        // Teleport
        this.getOnlineAlivePlayers().forEach(this::setupPlayer);
    }

    @Override
    protected void onPlayerSetup(Player player, PlayerState playerState) {
        switch (playerState) {
            case ALIVE -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
                if (Objects.equals(team, PotionPanicService.getInstance().getTeam1())) {
                    player.teleport(Locations.TEAM_SPAWNS[0]);
                } else if (Objects.equals(team, PotionPanicService.getInstance().getTeam2())) {
                    player.teleport(Locations.TEAM_SPAWNS[1]);
                }
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1.0);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
            case DEAD, SPEC -> player.teleport(Locations.SPEC_SPAWN);
        }
    }

    @Override
    public void onRoundStart() {
        // Spawn initial potion
        this.world.dropItem(
            Locations.SCHEMATIC_ORIGIN.clone().add(0.5, 2.5, 0.5),
            ISB.material(Material.SPLASH_POTION)
                .meta(itemMeta -> ((PotionMeta) itemMeta)
                    .setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))).build()
        ).setVelocity(new Vector(0, 0.3, 0));

        // Clear barriers
        WorldEditService we = WorldEditService.getInstance();
        com.sk89q.worldedit.world.World weWorld = we.wrapWorld(this.world);
        we.replaceRegion(
            weWorld,
            we.createCuboidRegion(Locations.ARENA_MIN, Locations.ARENA_MAX),
            we.createMask(weWorld, Material.BARRIER),
            we.createPattern(Material.AIR)
        );
    }

    public void handlePlayerThrow(Player player) {
        // Start timer to spawn next potion
        BukkitUtils.runLater(() -> {
            if (this.roundCompleted) {
                return;
            }

            GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
            if (PotionPanicService.getInstance().getTeam1().equals(team)) {
                // Spawn team 2 potion
                this.world.dropItem(
                    Locations.TEAM_SPAWNS[1].clone().add(0, 2, 0),
                    ISB.material(Material.SPLASH_POTION)
                        .meta(itemMeta -> ((PotionMeta) itemMeta)
                            .setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))).build()
                ).setVelocity(new Vector(0, 0.3, 0));
            } else {
                // Spawn team 1 potion
                this.world.dropItem(
                    Locations.TEAM_SPAWNS[0].clone().add(0, 2, 0),
                    ISB.material(Material.SPLASH_POTION)
                        .meta(itemMeta -> ((PotionMeta) itemMeta)
                            .setBasePotionData(new PotionData(PotionType.INSTANT_DAMAGE))).build()
                ).setVelocity(new Vector(0, 0.3, 0));
            }
        }, 40);

        // Check if arena needs to shrink (every 6 throws)
        if (++numThrows % 6 == 0) {
            this.shrinkArena();
        }
    }

    private void shrinkArena() {
        // TODO: shrink arena
    }

    public void handlePlayerDeath(Player player) {
        // Mark as dead
        this.setDead(player);
        SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);

        // Check if round is over
        if (!this.isTeamAlive(player)) {
            this.roundIsOver();
        }
    }

    private void roundIsOver() {
        this.roundCompleted = true;
        this.winner = this.isTeamAlive(PotionPanicService.getInstance().getTeam1()) ? PotionPanicService.getInstance().getTeam1() : PotionPanicService.getInstance().getTeam2();

        PotionPanicService.getInstance().getWins().compute(this.winner, (k, v) -> v + 1);
        PotionPanicService.getInstance().notifyObservers();

        if (PotionPanicService.getInstance().getWins().get(this.winner) >= 3) {
            MinigameService.getInstance().setState(MinigameState.POST_GAME);
        } else {
            this.triggerRoundEnd();
        }
    }

    public GameTeam getWinner() {
        return this.winner;
    }

    public void handlePotionPickUp(Player player) {
        if (this.isFirstPotion) {
            player.setCooldown(Material.SPLASH_POTION, 60);
            this.isFirstPotion = false;
        }
    }
}
