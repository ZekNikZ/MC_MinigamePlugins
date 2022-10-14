package io.zkz.mc.minigameplugins.uhc.settings;

import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.ConfigHolder;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.scoreboard.ScoreboardService;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.settings.GameSettingCategory;
import io.zkz.mc.minigameplugins.gametools.settings.GameSettingsService;
import io.zkz.mc.minigameplugins.gametools.settings.IGameSetting;
import io.zkz.mc.minigameplugins.gametools.settings.impl.BooleanSetting;
import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.settings.impl.IntegerSetting;
import io.zkz.mc.minigameplugins.gametools.util.IObserver;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.gametools.util.WorldSyncUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.uhc.UHCPlugin;
import io.zkz.mc.minigameplugins.uhc.settings.enums.*;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service
public class SettingsManager extends PluginService<UHCPlugin> implements ConfigHolder<UHCConfig> {
    private static final SettingsManager INSTANCE = new SettingsManager();

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    // ==============
    // WORLD BEHAVIOR
    // ==============
    private static final GameSettingCategory WORLD_BEHAVIOR_CATEGORY = new GameSettingCategory(mm("World Behavior"), mm("Time and weather settings."), ISB.stack(Material.CLOCK));
    public static final EnumSetting<TimeCycle> SETTING_TIME_CYCLE = setting(WORLD_BEHAVIOR_CATEGORY,
        new EnumSetting<>(
            mm("Time Cycle"),
            mm("Determine how time works in the game."),
            ISB.stack(Material.CLOCK),
            TimeCycle.class,
            TimeCycle.NORMAL
        ),
        setting -> {
            if (MinigameService.getInstance().getCurrentState().isInGame()) {
                WorldSyncUtils.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, setting.value() == TimeCycle.NORMAL);

                if (setting.value() == TimeCycle.DAY_ONLY) {
                    WorldSyncUtils.setTime(6000);
                } else if (setting.value() == TimeCycle.NIGHT_ONLY) {
                    WorldSyncUtils.setTime(18000);
                }
            }
        }
    );

    public static final EnumSetting<WeatherCycle> SETTING_WEATHER_CYCLE = setting(WORLD_BEHAVIOR_CATEGORY,
        new EnumSetting<>(
            mm("Weather Cycle"),
            mm("Determines how weather works in the game."),
            ISB.stack(Material.DEAD_BUSH),
            WeatherCycle.class,
            WeatherCycle.CLEAR_ONLY
        ),
        setting -> {
            if (MinigameService.getInstance().getCurrentState().isInGame()) {
                if (setting.value() == WeatherCycle.CLEAR_ONLY) {
                    WorldSyncUtils.setWeatherClear();
                } else if (setting.value() == WeatherCycle.RAIN_ONLY) {
                    WorldSyncUtils.setWeatherRain();
                } else if (setting.value() == WeatherCycle.STORM_ONLY) {
                    WorldSyncUtils.setWeatherStorm();
                }
            }
        }
    );

    public static final BooleanSetting SETTING_SPAWN_PHANTOMS = setting(WORLD_BEHAVIOR_CATEGORY,
        new BooleanSetting(
            mm("Allow Phantom Spawning"),
            mm("Determines if phantoms will be allowed to spawn."),
            ISB.stack(Material.PHANTOM_MEMBRANE),
            false
        )
    );

    public static final BooleanSetting SETTING_HOSTILE_MOBS = setting(WORLD_BEHAVIOR_CATEGORY,
        new BooleanSetting(
            mm("Allow Hostile Mob Spawning"),
            mm("Determines if hostile mobs will be allowed to spawn."),
            ISB.stack(Material.ZOMBIE_HEAD),
            true
        )
    );

    public static final EnumSetting<GameDifficulty> SETTING_DIFFICULTY = setting(WORLD_BEHAVIOR_CATEGORY,
        new EnumSetting<>(
            mm("Game Difficulty"),
            mm("Determines the game difficulty"),
            ISB.stack(Material.NETHERITE_SWORD),
            GameDifficulty.class,
            GameDifficulty.HARD
        )
    );

    // =====
    // ITEMS
    // =====
    private static final GameSettingCategory ITEMS_CATEGORY = new GameSettingCategory(mm("Items"), mm("Item settings."), ISB.stack(Material.COMPASS));

    public static final EnumSetting<CompassBehavior> SETTING_COMPASS_BEHAVIOR = setting(ITEMS_CATEGORY,
        new EnumSetting<>(
            mm("Compass Player Tracking"),
            mm("Determines which player(s) compasses track, if any."),
            ISB.stack(Material.COMPASS),
            CompassBehavior.class,
            CompassBehavior.TRACK_ENEMIES
        )
    );

    public static final BooleanSetting SETTING_THROWABLE_FIREBALLS = setting(ITEMS_CATEGORY,
        new BooleanSetting(
            mm("Throwable Fireballs"),
            mm("Determines if fire charges can be thrown via right-click."),
            ISB.stack(Material.FIRE_CHARGE),
            true
        )
    );

    public static final BooleanSetting SETTING_REGENERATION_POTIONS = setting(ITEMS_CATEGORY,
        new BooleanSetting(
            mm("Regeneration Potions"),
            mm("Determines if regeneration potions will be allowed."),
            ISB.material(Material.POTION).potion(new PotionData(PotionType.REGEN)).build(),
            false
        )
    );

    public static final BooleanSetting SETTING_GOLDEN_HEADS = setting(ITEMS_CATEGORY,
        new BooleanSetting(
            mm("Golden Heads"),
            mm("Determines if golden heads will be craftable."),
            ISB.stack(Material.PLAYER_HEAD),
            true
        )
    );

    // =====
    // TEAMS
    // =====
    private static final GameSettingCategory TEAMS_CATEGORY = new GameSettingCategory(mm("Teams"), mm("Team gameplay settings."), ISB.stack(Material.IRON_CHESTPLATE));

    public static final EnumSetting<TeamStatus> SETTING_TEAM_GAME = setting(TEAMS_CATEGORY,
        new EnumSetting<>(
            mm("Team Mode"),
            mm("Determines if players will compete in teams or solo."),
            ISB.stack(Material.LEATHER_CHESTPLATE),
            TeamStatus.class,
            TeamStatus.INDIVIDUAL_GAME
        ),
        setting -> {
            if (MinigameService.getInstance().getCurrentState() == MinigameState.WAITING_FOR_PLAYERS) {
                ScoreboardService.getInstance().getAllScoreboards().forEach(GameScoreboard::redraw);
            }
        }
    );

    public static final BooleanSetting SETTING_TEAMS_SPAWN_TOGETHER = setting(TEAMS_CATEGORY,
        new BooleanSetting(
            mm("Teams Spawn Together"),
            mm("Determines if teams will spawn together at the beginning of the game."),
            ISB.stack(Material.GRASS_BLOCK),
            true
        )
    );

    public static final EnumSetting<SpectatorMode> SETTING_SPECTATOR_INVENTORIES = setting(TEAMS_CATEGORY,
        new EnumSetting<>(
            mm("Spectator Inventories"),
            mm("Determines if specators can see the inventories of players."),
            ISB.stack(Material.PHANTOM_MEMBRANE),
            SpectatorMode.class,
            SpectatorMode.SPECTATORS_SEE_INVENTORIES
        )
    );

    // ============
    // WORLD BORDER
    // ============
    private static final GameSettingCategory WORLD_BORDER_CATEGORY = new GameSettingCategory(mm("Worldborder and Timings"), mm("Worldborder and game timing settings."), ISB.stack(Material.CLOCK));

    public static final IntegerSetting SETTING_WORLD_BORDER_DISTANCE_1 = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("World Border Diameter (Start)"),
            mm("Determines the initial diameter of the world border."),
            ISB.stack(Material.IRON_BARS, 1),
            () -> 3000,
            () -> 100,
            () -> 5000,
            () -> 100
        )
    );

    public static final IntegerSetting SETTING_WORLD_BORDER_TIME_1 = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("Phase 1 Time (Minutes)"),
            mm("Determines the time it takes for the world border\nto shrink from setting 1 to 2."),
            ISB.stack(Material.CLOCK, 1),
            60,
            30,
            120,
            5
        )
    );

    public static final IntegerSetting SETTING_WORLD_BORDER_DISTANCE_2 = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("World Border Diameter (Phase 2 Start)"),
            mm("Determines the diameter of the world border when the phase changes."),
            ISB.stack(Material.IRON_BARS, 2),
            () -> 1000,
            () -> 100,
            SETTING_WORLD_BORDER_DISTANCE_1::value,
            () -> 100
        )
    );

    public static final IntegerSetting SETTING_WORLD_BORDER_TIME_2 = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("Phase 2 Time (Minutes)"),
            mm("Determines the time it takes for the world border\nto shrink from setting 2 to 3.\nSet to 0 to disable phase 2."),
            ISB.stack(Material.CLOCK, 2),
            30,
            0,
            120,
            5
        )
    );

    public static final IntegerSetting SETTING_WORLD_BORDER_DISTANCE_3 = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("World Border Diameter (End)"),
            mm("Determines the final diameter of the world border."),
            ISB.stack(Material.IRON_BARS, 3),
            () -> 100,
            () -> 0,
            SETTING_WORLD_BORDER_DISTANCE_2::value,
            () -> 100
        )
    );

    public static final BooleanSetting SETTING_SUDDEN_DEATH_ENABLED = setting(WORLD_BORDER_CATEGORY,
        new BooleanSetting(
            mm("Sudden Death"),
            mm("Determines if sudden death should be enabled."),
            ISB.stack(Material.CAMPFIRE),
            true
        )
    );

    public static final IntegerSetting SETTING_PARLAY_TIME = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("Parlay Time (Minutes)"),
            mm("Determines the time it takes for sudden death to start once the world border stops moving."),
            ISB.stack(Material.CLOCK, 3),
            10,
            1,
            30,
            1
        )
    );

    public static final IntegerSetting SETTING_SUDDEN_DEATH_TIME = setting(WORLD_BORDER_CATEGORY,
        new IntegerSetting(
            mm("Parlay Time (Minutes)"),
            mm("Determines the time it takes for sudden death to start once the world border stops moving."),
            ISB.stack(Material.CLOCK, 4),
            5,
            1,
            20,
            1
        )
    );

    @SafeVarargs
    private static <T extends IGameSetting<?>> T setting(GameSettingCategory category, T setting, IObserver<T>... observers) {
        GameSettingsService.getInstance().registerSetting(category, setting);
        for (var observer : observers) {
            setting.addListener(observer);
        }
        return setting;
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("settings.json"), UHCConfig.class, this)
        );
    }

    @Override
    public void setConfig(UHCConfig config) {
        // World behavior
        SETTING_TIME_CYCLE.value(config.timeCycle());
        SETTING_WEATHER_CYCLE.value(config.weatherCycle());
        SETTING_SPAWN_PHANTOMS.value(config.spawnPhantoms());
        SETTING_HOSTILE_MOBS.value(config.hostileMobs());
        SETTING_DIFFICULTY.value(config.difficulty());

        // Items
        SETTING_COMPASS_BEHAVIOR.value(config.compassBehavior());
        SETTING_THROWABLE_FIREBALLS.value(config.throwableFireballs());
        SETTING_REGENERATION_POTIONS.value(config.regenerationPotions());
        SETTING_GOLDEN_HEADS.value(config.goldenHeads());

        // Teams
        SETTING_TEAM_GAME.value(config.teamGame());
        SETTING_TEAMS_SPAWN_TOGETHER.value(config.teamsSpawnTogether());
        SETTING_SPECTATOR_INVENTORIES.value(config.spectatorInventories());

        // World border
        SETTING_WORLD_BORDER_DISTANCE_1.value(config.worldBorderDistance1());
        SETTING_WORLD_BORDER_TIME_1.value(config.worldBorderTime1());
        SETTING_WORLD_BORDER_DISTANCE_2.value(config.worldBorderDistance2());
        SETTING_WORLD_BORDER_TIME_2.value(config.worldBorderTime2());
        SETTING_WORLD_BORDER_DISTANCE_3.value(config.worldBorderDistance3());
        SETTING_SUDDEN_DEATH_ENABLED.value(config.suddenDeathEnabled());
        SETTING_PARLAY_TIME.value(config.parlayTime());
        SETTING_SUDDEN_DEATH_TIME.value(config.suddenDeathTime());
    }

    @Override
    public UHCConfig getConfig() {
        return new UHCConfig(
            // World behavior
            SETTING_TIME_CYCLE.value(),
            SETTING_WEATHER_CYCLE.value(),
            SETTING_SPAWN_PHANTOMS.value(),
            SETTING_HOSTILE_MOBS.value(),
            SETTING_DIFFICULTY.value(),
            // Items
            SETTING_COMPASS_BEHAVIOR.value(),
            SETTING_THROWABLE_FIREBALLS.value(),
            SETTING_REGENERATION_POTIONS.value(),
            SETTING_GOLDEN_HEADS.value(),
            // Teams
            SETTING_TEAM_GAME.value(),
            SETTING_TEAMS_SPAWN_TOGETHER.value(),
            SETTING_SPECTATOR_INVENTORIES.value(),
            // World border
            SETTING_WORLD_BORDER_DISTANCE_1.value(),
            SETTING_WORLD_BORDER_TIME_1.value(),
            SETTING_WORLD_BORDER_DISTANCE_2.value(),
            SETTING_WORLD_BORDER_TIME_2.value(),
            SETTING_WORLD_BORDER_DISTANCE_3.value(),
            SETTING_SUDDEN_DEATH_ENABLED.value(),
            SETTING_PARLAY_TIME.value(),
            SETTING_SUDDEN_DEATH_TIME.value()
        );
    }
}
