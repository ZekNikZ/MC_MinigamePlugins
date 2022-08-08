package io.zkz.mc.minigameplugins.tntrun;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.score.ScoreEntry;
import io.zkz.mc.minigameplugins.minigamemanager.service.ScoreService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class TNTRunRound extends Round {
    private final BlockVector3 arenaMin;
    private final BlockVector3 arenaMax;
    private final BlockVector3 spawnLocation;
    private final int deathYLevel;

    public TNTRunRound(BlockVector3 arenaMin, BlockVector3 arenaMax, BlockVector3 spawnLocation, int deathYLevel, String mapName) {
        super(mapName);
        this.arenaMin = arenaMin;
        this.arenaMax = arenaMax;
        this.spawnLocation = spawnLocation;
        this.deathYLevel = deathYLevel;
    }

    @Override
    public void onSetup() {
        TNTRunService.getInstance().setupArena(this);
    }

    @Override
    public void onPreRound() {
        BukkitUtils.forEachPlayer(player -> {
            player.teleport(new Location(Bukkit.getWorlds().get(0), this.getSpawnLocation().getX(), this.getSpawnLocation().getY(), this.getSpawnLocation().getZ()));
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10, 10, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 1, false, false));
        });
    }

    @Override
    public void onEnd() {
        SoundUtils.playSound(StandardSounds.GAME_OVER, 10, 1);
    }

    @Override
    public void onPostRound() {
        BukkitUtils.forEachPlayer(player -> {
            double points = ScoreService.getInstance().getRoundEntries(player).stream().mapToDouble(ScoreEntry::points).sum();
            Chat.sendMessage(player, " ");
            Chat.sendAlertFormatted(player, ChatType.ACTIVE_INFO, "You earned " + ChatColor.GREEN + ChatColor.BOLD + "%.1f" + Chat.Constants.POINT_CHAR + " this round.", points);
        });
    }

    public BlockVector3 getArenaMax() {
        return this.arenaMax;
    }

    public BlockVector3 getArenaMin() {
        return this.arenaMin;
    }

    public BlockVector3 getSpawnLocation() {
        return this.spawnLocation;
    }

    public int getDeathYLevel() {
        return this.deathYLevel;
    }

    public void resetArena() {

    }

    @Override
    public @NotNull String getMapName() {
        return super.getMapName();
    }
}
