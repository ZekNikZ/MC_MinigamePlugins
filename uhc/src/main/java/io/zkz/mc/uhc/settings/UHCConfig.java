package io.zkz.mc.uhc.settings;

import io.zkz.mc.uhc.settings.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(fluent = true)
public final class UHCConfig {
    // World behavior
    private TimeCycle timeCycle;
    private WeatherCycle weatherCycle;
    private boolean spawnPhantoms;
    private boolean hostileMobs;
    private GameDifficulty difficulty;

    // Items
    private CompassBehavior compassBehavior;
    private boolean throwableFireballs;
    private boolean regenerationPotions;
    private boolean goldenHeads;

    // Teams
    private TeamStatus teamGame;
    private boolean teamsSpawnTogether;
    private SpectatorMode spectatorInventories;

    // World border
    private int worldBorderDistance1;
    private int worldBorderTime1;
    private int worldBorderDistance2;
    private int worldBorderTime2;
    private int worldBorderDistance3;
    private boolean suddenDeathEnabled;
    private int parlayTime;
    private int suddenDeathTime;
}
