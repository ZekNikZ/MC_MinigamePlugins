package io.zkz.mc.minigameplugins.guessthebuild;

import com.sk89q.worldedit.math.BlockVector3;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.timer.GameCountdownTimer;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GuessTheBuildRound extends Round {
    private int playerPlacement = 0;
    private final UUID builderId;
    private String chosenWord;
    private String hint;
    private final Set<UUID> correctGuessers = new HashSet<>();
    private static int wordIndex = 0;
    private final List<String> wordOptions = new ArrayList<>();
    private SmartInventory inv;

    public GuessTheBuildRound(UUID builderId) {
        super(null);
        this.builderId = builderId;
    }

    public UUID getBuilderId() {
        return this.builderId;
    }

    public Player getBuilder() {
        return Bukkit.getPlayer(this.builderId);
    }

    @Override
    public void onSetup() {
        // Clear arena
        WorldEditService we = WorldEditService.getInstance();
        var weWorld = we.wrapWorld(Bukkit.getWorld("guessthebuild"));
        we.fillRegion(
            weWorld,
            we.createCuboidRegion(BlockVector3.at(24, -60, -8), BlockVector3.at(-8, -58, 24)),
            we.createPattern(Material.WHITE_TERRACOTTA)
        );
        we.fillRegion(
            weWorld,
            we.createCuboidRegion(BlockVector3.at(24, -57, -8), BlockVector3.at(-8, -41, 24)),
            we.createPattern(Material.AIR)
        );
        Bukkit.getWorld("guessthebuild").getEntities().forEach(entity -> {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        });

        // TP players
        BukkitUtils.forEachPlayer(this::setupPlayerLocation);

        // World setup
        WorldSyncUtils.setDifficulty(Difficulty.EASY);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setWeatherClear();
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        WorldSyncUtils.setGameRule(GameRule.FALL_DAMAGE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_TILE_DROPS, false);
        WorldSyncUtils.setGameRule(GameRule.DO_MOB_LOOT, false);
        WorldSyncUtils.setGameRule(GameRule.MOB_GRIEFING, false);
        WorldSyncUtils.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        WorldSyncUtils.setGameRule(GameRule.DO_FIRE_TICK, false);

        // Choose word options
        this.wordOptions.add(GuessTheBuildService.getInstance().getWords().get(wordIndex++).toUpperCase());
        this.wordOptions.add(GuessTheBuildService.getInstance().getWords().get(wordIndex++).toUpperCase());
        this.wordOptions.add(GuessTheBuildService.getInstance().getWords().get(wordIndex++).toUpperCase());
    }

    @Override
    public void onEnterPreRound() {
        // setup player gamemodes
        BukkitUtils.forEachPlayer(player -> {
            player.getInventory().clear();
        });
        Player builder = this.getBuilder();

        // Display word selector
        this.inv = SmartInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    this.drawContents(contents);
                }

                @Override
                public void update(Player player, InventoryContents contents) {
                    this.drawContents(contents);
                }

                private void drawContents(InventoryContents contents) {
                    contents.set(1, 1, ClickableItem.of(
                        ISB.material(Material.PAPER)
                            .name(wordOptions.get(0))
                            .lore(Objects.equals(chosenWord, wordOptions.get(0)) ? "" + ChatColor.GREEN + ChatColor.BOLD + "SELECTED" : "Click to select")
                            .build(),
                        (event) -> {
                            chosenWord = wordOptions.get(0);
                        }
                    ));
                    contents.set(1, 4, ClickableItem.of(
                        ISB.material(Material.PAPER)
                            .name(wordOptions.get(1))
                            .lore(Objects.equals(chosenWord, wordOptions.get(1)) ? "" + ChatColor.GREEN + ChatColor.BOLD + "SELECTED" : "Click to select")
                            .build(),
                        (event) -> {
                            chosenWord = wordOptions.get(1);
                        }
                    ));
                    contents.set(1, 7, ClickableItem.of(
                        ISB.material(Material.PAPER)
                            .name(wordOptions.get(2))
                            .lore(Objects.equals(chosenWord, wordOptions.get(2)) ? "" + ChatColor.GREEN + ChatColor.BOLD + "SELECTED" : "Click to select")
                            .build(),
                        (event) -> {
                            chosenWord = wordOptions.get(2);
                        }
                    ));
                }
            })
            .size(3, 9)
            .title("Choose a theme")
            .closeable(false)
            .build();
        this.inv.open(builder);
    }

    @Override
    public void onRoundStart() {
        // Pick word if player didn't already
        if (this.chosenWord == null) {
            this.chosenWord = this.wordOptions.get(0);
        }
        this.hint = this.chosenWord.replaceAll("[^ ]", "_");

        // Creative mode
        Player builder = this.getBuilder();
        BukkitUtils.runNextTick(() -> {
            this.inv.close(builder);
            builder.setGameMode(GameMode.CREATIVE);
        });

        // Setup timers
        MinigameService.getInstance().changeTimer(new GameCountdownTimer(GuessTheBuildService.getInstance().getPlugin(), 20, 120, TimeUnit.SECONDS, this::roundIsOver));
        MinigameService.getInstance().getTimer().addHook(new Runnable() {
            private boolean showingWord = false;
            private boolean hint1 = false;
            private boolean hint2 = false;

            @Override
            public void run() {
                long seconds = MinigameService.getInstance().getTimer().getCurrentTime(TimeUnit.SECONDS);
                if (seconds < 90 && !showingWord) {
                    showingWord = true;
                    SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                }
                if (seconds < 60 && !hint1) {
                    hint1 = true;
                    addHint();
                    SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                }
                if (seconds < 30 && !hint2) {
                    hint2 = true;
                    addHint();
                    SoundUtils.playSound(StandardSounds.ALERT_INFO, 1, 1);
                }

                if (seconds < 10) {
                    SoundUtils.playSound(StandardSounds.TIMER_TICK, 1, 1);
                }

                Player builder = getBuilder();
                if (showingWord) {
                    BukkitUtils.allPlayersExcept(builder).forEach(player -> TitleUtils.sendActionBarMessage(player, "" + ChatColor.AQUA + "Hint: " + ChatColor.RESET + hint));
                }
                TitleUtils.sendActionBarMessage(builder, "" + ChatColor.AQUA + "Your word: " + ChatColor.RESET + chosenWord);
            }
        });
    }

    @Override
    public void onEnterPostRound() {
        Chat.sendAlert(ChatType.GAME_INFO, "The prompt was " + ChatColor.AQUA + ChatColor.BOLD + chosenWord);
    }

    private void addHint() {
        Random rand = new Random();
        int hintIndex;
        do {
            hintIndex = rand.nextInt(this.chosenWord.length());
        } while (this.hint.charAt(hintIndex) != '_');
        this.hint = this.hint.substring(0, hintIndex) + this.chosenWord.charAt(hintIndex) + this.hint.substring(hintIndex + 1);
    }

    public void setupPlayerLocation(Player player) {
        player.teleport(new Location(Bukkit.getWorld("guessthebuild"), 8, -46, 8));
    }


    public boolean handlePlayerGuess(Player player, String guess) {
        if (player.getUniqueId().equals(this.builderId)) {
            BukkitUtils.runNextTick(() -> Chat.sendAlert(player, ChatType.WARNING, "You can't guess for your own build!"));
            return true;
        }

        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        if (team != null && team.isSpectator()) {
            BukkitUtils.runNextTick(() -> Chat.sendAlert(player, ChatType.WARNING, "You can't guess as a spectator!"));
            return true;
        }

//        GameTeam builderTeam = TeamService.getInstance().getTeamOfPlayer(this.builderId);
//        if (Objects.equals(team, builderTeam)) {
//            Chat.sendAlert(player, ChatType.WARNING, "You can't guess for your teammate's build!");
//            return true;
//        }

        if (this.correctGuessers.contains(player.getUniqueId())) {
            BukkitUtils.runNextTick(() -> Chat.sendAlert(player, ChatType.WARNING, "You already guessed correctly!"));
            return true;
        }

        if (!guess.strip().equalsIgnoreCase(this.chosenWord)) {
            return false;
        }

        this.correctGuessers.add(player.getUniqueId());

        BukkitUtils.runNextTick(() -> {
            // Compute score and placement
            int points = Points.getPlayerPlacementPointValue(this.playerPlacement);
            String placementOrdinal = NumberUtils.ordinal(this.playerPlacement + 1);
            MinigameService.getInstance().earnPoints(player, "correct guess (" + placementOrdinal + ")", points);

            // Builder score
            if (this.playerPlacement == 0) {
                MinigameService.getInstance().earnPoints(this.builderId, "successful build", Points.SUCCESSFUL_BUILD);
            }

            // Chat message
            Chat.sendAlert(player, ChatType.SUCCESS, "You correctly guessed the build! (" + ChatColor.AQUA + ChatColor.BOLD + placementOrdinal + ChatColor.GREEN + ChatColor.BOLD + " place)", points);
            Chat.sendAlert(BukkitUtils.allPlayersExcept(player), ChatType.ACTIVE_INFO, player.getDisplayName() + ChatColor.GRAY + " correctly guessed the build!");
            SoundUtils.playSound(player, StandardSounds.GOAL_MET_MAJOR, 1, 1);
            SoundUtils.playSound(BukkitUtils.allPlayersExcept(player), StandardSounds.ALERT_WARNING, 1, 1);
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);

            // Increment placement
            this.playerPlacement++;

            // Check if round is over
            if (this.correctGuessers.size() == MinigameService.getInstance().getPlayers().size() - 1) {
                this.roundIsOver();
            }
        });

        return true;
    }

    private void roundIsOver() {
        this.triggerRoundEnd();
    }

    @Override
    public @NotNull String getMapName() {
        return super.getMapName();
    }

    public void setFloor(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Hold an item to change the floor to it.");
            return;
        }


        Material mat = itemInHand.getType();

        if (mat == Material.WATER_BUCKET) {
            mat = Material.WATER;
        } else if (mat == Material.LAVA_BUCKET) {
            mat = Material.LAVA;
        }

        if (!mat.isBlock()) {
            player.sendMessage(ChatColor.RED + "You must be holding a block.");
            return;
        }

        WorldEditService we = WorldEditService.getInstance();
        var weWorld = we.wrapWorld(Bukkit.getWorld("guessthebuild"));
        we.fillRegion(
            weWorld,
            we.createCuboidRegion(BlockVector3.at(24, -60, -8), BlockVector3.at(-8, -58, 24)),
            we.createPattern(mat)
        );
    }
}
