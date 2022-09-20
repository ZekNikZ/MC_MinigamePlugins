package io.zkz.mc.minigameplugins.bingo;

import com.sk89q.worldedit.regions.Region;
import io.zkz.mc.minigameplugins.bingo.card.BingoCard;
import io.zkz.mc.minigameplugins.bingo.map.BingoCardMap;
import io.zkz.mc.minigameplugins.bingo.map.BingoCardMapRenderer;
import io.zkz.mc.minigameplugins.bingo.util.PlayerUtils;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.sound.StandardSounds;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.*;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import io.zkz.mc.minigameplugins.minigamemanager.round.Round;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.map.MapView;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class BingoRound extends Round {
    private final Location spawnLocation;
    private final BingoCard card;
    private Map<Material, Integer> possiblePoints;
    private Map<Material, List<String>> teamCollectionOrder;
    private Map<String, Set<Material>> teamCollections;
    private Map<UUID, Set<Material>> playerItemCollections;

    public BingoCard getCard() {
        return this.card;
    }

    public BingoRound(TypedJSONObject<Object> json) {
        this.card = new BingoCard(json.getList("card", String.class));
        this.spawnLocation = JSONUtils.readLocation(json.getObject("spawn"));
        this.possiblePoints = new HashMap<>();
        this.teamCollectionOrder = new HashMap<>();
        this.teamCollections = new HashMap<>();
        this.playerItemCollections = new HashMap<>();
    }

    @Override
    public void onSetup() {
        // TODO: remove
//        this.card.randomizeItems();
        BingoCardMap.markDirty();

        this.setupCollections();

        // Setup lobby
        SchematicService.getInstance().placeSchematic(BingoRound.class.getResourceAsStream("/lobby.schem"), this.spawnLocation);

        // Gamerules
        WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WorldSyncUtils.setGameRule(GameRule.KEEP_INVENTORY, true);
        WorldSyncUtils.setGameRule(GameRule.FALL_DAMAGE, false);
        WorldSyncUtils.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        WorldSyncUtils.setWorldBorderCenter(this.spawnLocation.getBlockX(), this.spawnLocation.getBlockZ());
        WorldSyncUtils.setWorldBorderSize(1500);
        WorldSyncUtils.setDifficulty(Difficulty.NORMAL);
        WorldSyncUtils.setTime(6000);
        WorldSyncUtils.setWeatherClear();
    }

    @Override
    public void onEnterPreRound() {
        // Create the map
        MapView mapView = Bukkit.createMap(this.spawnLocation.getWorld());
        mapView.getRenderers().clear();
        mapView.setTrackingPosition(false);
        mapView.addRenderer(new BingoCardMapRenderer());

        // Player setup
        Bukkit.getOnlinePlayers().stream()
            .filter(player -> !TeamService.getInstance().getTeamOfPlayer(player).spectator())
            .forEach(player -> {
                // Tools
                player.getInventory().clear();

                player.getInventory().addItem(
                    ISB.material(Material.IRON_SWORD)
                        .addEnchantment(Enchantment.DAMAGE_ALL, 3)
                        .unbreakable()
                        .build(),
                    ISB.material(Material.IRON_PICKAXE)
                        .addEnchantment(Enchantment.DIG_SPEED, 3)
                        .unbreakable()
                        .build(),
                    ISB.material(Material.IRON_AXE)
                        .addEnchantment(Enchantment.DIG_SPEED, 3)
                        .unbreakable()
                        .build(),
                    ISB.material(Material.IRON_SHOVEL)
                        .addEnchantment(Enchantment.DIG_SPEED, 3)
                        .unbreakable()
                        .build(),
                    ISB.material(Material.NETHER_STAR)
                        .name("Bingo Crystal")
                        .lore("Right-click this to view the", "bingo card or teleport.")
                        .build()
                );

                // Map
                player.getInventory().setItemInOffHand(BingoCardMap.makeMap());

                // Armor
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
                if (team != null) {
                    Color color = ColorUtils.toBukkitColor(team.color());
                    LeatherArmorMeta meta = ((LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.LEATHER_HELMET));
                    meta.setColor(color);
                    player.getInventory().setItem(
                        EquipmentSlot.HEAD,
                        ISB.material(Material.LEATHER_HELMET)
                            .meta(meta)
                            .unbreakable()
                            .build()
                    );
                    player.getInventory().setItem(
                        EquipmentSlot.CHEST,
                        ISB.material(Material.LEATHER_CHESTPLATE)
                            .meta(meta)
                            .unbreakable()
                            .build()
                    );
                    player.getInventory().setItem(
                        EquipmentSlot.LEGS,
                        ISB.material(Material.LEATHER_LEGGINGS)
                            .meta(meta)
                            .unbreakable()
                            .build()
                    );
                    player.getInventory().setItem(
                        EquipmentSlot.FEET,
                        ISB.material(Material.LEATHER_BOOTS)
                            .meta(meta)
                            .unbreakable()
                            .build()
                    );
                }

                // Advancements and recipes
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() + " everything");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "recipe give " + player.getName() + " *");

                player.setBedSpawnLocation(this.spawnLocation, true);
                player.teleport(this.spawnLocation);
                player.setHealth(20);
                player.setFoodLevel(20);
            });
    }

    @Override
    public void onRoundStart() {
        WorldEditService we = WorldEditService.getInstance();

        // Remove barriers
        Region region = we.createCuboidRegion(we.wrapLocation(this.spawnLocation), 10);
        we.replaceRegion(
            we.wrapWorld(this.spawnLocation.getWorld()),
            region,
            we.createMask(we.wrapWorld(this.spawnLocation.getWorld()), Material.BARRIER),
            we.createPattern(Material.AIR)
        );
    }

    public JSONObject toJSON() {
        return new JSONObject(Map.of(
            "spawn", JSONUtils.toJSON(this.spawnLocation),
            "card", this.card.toJSON()
        ));
    }

    public void checkForItemCollection(Player player, PlayerInventory inventory) {
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        Set<Material> availableItems = getAvailableItems(team.id());
        availableItems.forEach(mat -> {
            if (inventory.contains(mat)) {
                PlayerUtils.consumeItem(player, 1, mat);
                this.handleItemCollected(player, team.id(), mat);
            }
        });
    }

    public int getPointsForItem(Material material) {
        return Optional.ofNullable(this.possiblePoints.get(material)).orElse(0);
    }

    public List<GameTeam> getCollectorsOfItem(Material material) {
        if (this.teamCollectionOrder.get(material) == null) {
            return List.of();
        }

        return this.teamCollectionOrder.get(material).stream().map(TeamService.getInstance()::getTeam).collect(Collectors.toList());
    }

    public Set<Material> getAvailableItems(String teamId) {
        return this.possiblePoints.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .filter(entry -> !this.teamCollections.get(teamId).contains(entry.getKey()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private void handleItemCollected(Player collector, String teamId, Material mat) {
        String itemName = WordUtils.capitalizeFully(mat.toString().replaceAll("_", " "));

        // Record player collection
        this.playerItemCollections.putIfAbsent(collector.getUniqueId(), new HashSet<>());
        this.playerItemCollections.get(collector.getUniqueId()).add(mat);

        // Update team points
        int numPoints = this.possiblePoints.get(mat);
        this.teamCollections.get(teamId).add(mat);
        this.teamCollectionOrder.get(mat).add(teamId);
        MinigameService.getInstance().earnPoints(collector.getUniqueId(), "collected " + itemName + " (" + Points.ORDINALS.get(numPoints) + ")", numPoints);

        // Alert variables
        GameTeam team = TeamService.getInstance().getTeam(teamId);
        String teamNameAndColor = team.getDisplayName();

        // Team alerts
        TeamService.getInstance().getOnlineTeamMembers(teamId).forEach(player -> {
            // Who got the item
            if (player != collector) {
                Chat.sendAlert(player, ChatType.SUCCESS, collector.getDisplayName() + ChatColor.GREEN + " obtained " + ChatColor.YELLOW + "[" + itemName + "]" + ChatColor.GREEN + " for your team!");
            }

            // Team points
            Chat.sendAlert(player, ChatType.ACTIVE_INFO, teamNameAndColor + ChatColor.GRAY + " obtained " + ChatColor.YELLOW + "[" + Points.ORDINALS.get(numPoints) + "] " + ChatColor.AQUA + "[" + itemName + "]", numPoints);

            // Sound
            SoundUtils.playSound(player, StandardSounds.GOAL_MET_MAJOR, 1, 1);
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 200, 1.5, 0.6, 1.5, 0);
        });

        // Non-team alerts
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (TeamService.getInstance().getTeamOfPlayer(player).id().equals(teamId)) {
                return;
            }

            // Other team points
            Chat.sendAlert(player, ChatType.PASSIVE_INFO, teamNameAndColor + ChatColor.GRAY + " obtained " + ChatColor.YELLOW + "[" + Points.ORDINALS.get(numPoints) + "] " + ChatColor.AQUA + "[" + itemName + "]");
        });

        // Compute new possible points
        this.possiblePoints.compute(mat, (key, val) -> {
            int index = Points.POINT_VALUES.indexOf(val);
            if (index == Points.POINT_VALUES.size() - 1) {
                SoundUtils.playSound(StandardSounds.PLAYER_ELIMINATION, 1, 1);
                Chat.sendAlert(ChatType.WARNING, "" + ChatColor.RESET + ChatColor.RED + "No more " + ChatType.Constants.POINT_CHAR + ChatColor.RED + " available for " + ChatColor.AQUA + "[" + itemName + "]");
                return 0;
            } else {
                return Points.POINT_VALUES.get(index + 1);
            }
        });

        BingoCardMap.markDirty();
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void randomizeCard() {
        this.card.randomizeItems();
        this.setupCollections();
    }

    private void setupCollections() {
        Set<Material> uniqueItems = new HashSet<>(this.card.getItems());
        this.possiblePoints = uniqueItems.stream().collect(Collectors.toMap(m -> m, m -> Points.INITIAL_POINTS));
        this.teamCollectionOrder = uniqueItems.stream().collect(Collectors.toMap(m -> m, m -> new ArrayList<>()));
        this.teamCollections = TeamService.getInstance().getAllTeams().stream().collect(Collectors.toMap(GameTeam::id, t -> new HashSet<>()));
    }
}
