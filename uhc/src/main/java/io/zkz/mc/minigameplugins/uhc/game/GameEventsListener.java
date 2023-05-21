package io.zkz.mc.minigameplugins.uhc.game;

import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.EntityUtils;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.uhc.UHCPlugin;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;
import io.zkz.mc.minigameplugins.uhc.settings.enums.CompassBehavior;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TeamStatus;
import io.zkz.mc.minigameplugins.uhc.settings.enums.WeatherCycle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service
public class GameEventsListener extends PluginService<UHCPlugin> {
    private static final GameEventsListener INSTANCE = new GameEventsListener();

    public static GameEventsListener getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        WeatherCycle weatherState = SettingsManager.SETTING_WEATHER_CYCLE.value();
        boolean toRain = event.toWeatherState();

        if (weatherState == WeatherCycle.CLEAR_ONLY && toRain) {
            event.setCancelled(true);
        } else if ((weatherState == WeatherCycle.RAIN_ONLY || weatherState == WeatherCycle.STORM_ONLY) && !toRain) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        WeatherCycle weatherState = SettingsManager.SETTING_WEATHER_CYCLE.value();
        boolean toStorm = event.toThunderState();

        if ((weatherState == WeatherCycle.CLEAR_ONLY || weatherState == WeatherCycle.RAIN_ONLY) && toStorm) {
            event.setCancelled(true);
        } else if (weatherState == WeatherCycle.STORM_ONLY && !toStorm) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnMob(CreatureSpawnEvent event) {
        if (!SettingsManager.SETTING_HOSTILE_MOBS.value() && EntityUtils.isHostile(event.getEntity())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Material material = event.getMaterial();
        Player player = event.getPlayer();

        // Fireball
        if (material == Material.FIRE_CHARGE && SettingsManager.SETTING_THROWABLE_FIREBALLS.value() && action == Action.RIGHT_CLICK_AIR) {
            if (player.hasCooldown(Material.FIRE_CHARGE)) {
                return;
            }
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setVelocity(fireball.getVelocity().multiply(2));
            fireball.setYield(fireball.getYield() * 2.5f);
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            }
            player.setCooldown(Material.FIRE_CHARGE, 20);
        }

        // Compass
        else if (material == Material.COMPASS) {
            if (player.hasCooldown(Material.COMPASS)) {
                event.setCancelled(true);
                return;
            }

            if (SettingsManager.SETTING_COMPASS_BEHAVIOR.value() != CompassBehavior.NORMAL) {
                Location location = null;
                double minDistance = Double.MAX_VALUE;
                for (UUID onlinePlayerUUID : MinigameService.getInstance().getCurrentRound().getAlivePlayers()) {
                    Player onlinePlayer = Bukkit.getPlayer(onlinePlayerUUID);
                    if (onlinePlayer == null || onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }

                    if (SettingsManager.SETTING_COMPASS_BEHAVIOR.value() == CompassBehavior.TRACK_ENEMIES
                        && SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME
                        && TeamService.getInstance().getTeamOfPlayer(player).id().equals(TeamService.getInstance().getTeamOfPlayer(onlinePlayer).id())) {
                        continue;
                    }

                    if (onlinePlayer.getLocation().getWorld() != player.getLocation().getWorld()) {
                        continue;
                    }

                    double distance = player.getLocation().distance(onlinePlayer.getLocation());
                    if (location == null || distance < minDistance) {
                        location = onlinePlayer.getLocation();
                        minDistance = Math.min(distance, minDistance);
                    }
                }

                if (location == null) {
                    player.sendMessage("Could not track any player.");
                } else {
                    player.sendMessage("Updated tracking");
                    player.setCompassTarget(location);
                    player.setCooldown(Material.COMPASS, 100);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Prevent explosion damage from fireballs
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && event.getDamager() instanceof Fireball) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getFrom();
        switch (MinigameService.getInstance().getCurrentState()) {
            case PRE_ROUND:
                to.setY(event.getTo().getY());
            case PAUSED:
                to.setPitch(event.getTo().getPitch());
                to.setYaw(event.getTo().getYaw());
                event.setTo(to);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (MinigameService.getInstance().getCurrentState().isInGame()) {
            MinigameService.getInstance().getCurrentRound().setDead(player);
        }
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest) && !(event.getInventory().getHolder() instanceof DoubleChest)) {
            return;
        }

        for (ItemStack stack : event.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                event.getInventory().removeItem(stack);
            }
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            event.setCancelled(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
            player.setFoodLevel(Math.min(player.getFoodLevel() + 4, 20));

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (SettingsManager.SETTING_GOLDEN_HEADS.value()) {
            Player player = ((Player) event.getEntity());
            Player killer = player.getKiller();
            if (killer != null) {
                Location location = player.getLocation();
                location.getWorld().dropItemNaturally(
                    location,
                    ISB.material(Material.PLAYER_HEAD)
                        .skullOwner(player)
                        .name(mm("<0>'s Head", player.displayName()))
                        .build()
                );
            }
        }
    }

    @EventHandler
    public void onGhastDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Ghast)) {
            return;
        }

        if (!SettingsManager.SETTING_REGENERATION_POTIONS.value()) {
            event.getDrops().clear();
        }
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!MinigameService.getInstance().getCurrentState().isInGame() || MinigameService.getInstance().getCurrentState() == MinigameState.PAUSED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!MinigameService.getInstance().getCurrentState().isInGame() || MinigameService.getInstance().getCurrentState() == MinigameState.PAUSED) {
            event.setCancelled(true);
        }
    }
}
