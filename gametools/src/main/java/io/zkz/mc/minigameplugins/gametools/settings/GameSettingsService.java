package io.zkz.mc.minigameplugins.gametools.settings;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.settings.impl.BooleanSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service
public class GameSettingsService extends PluginService<GameToolsPlugin> {
    private static final GameSettingsService INSTANCE = new GameSettingsService();

    public static GameSettingsService getInstance() {
        return INSTANCE;
    }

    public final Map<GameSettingCategory, List<IGameSetting<?>>> settings = new HashMap<>();

    public void registerSetting(GameSettingCategory category, IGameSetting<?> setting) {
        if (!this.settings.containsKey(category)) {
            this.settings.put(category, new ArrayList<>());
        }

        this.settings.get(category).add(setting);
    }

    public void openMenu(Player player) {
        this.settings.forEach((category, categorySettings) -> {
            player.sendMessage(mm(" - <0>: <1>", category.name(), category.description()));
            categorySettings.forEach(setting -> {
                player.sendMessage(mm(" - <0>: <1>", setting.name(), setting.description()));
            });
        });
    }

    @Override
    protected void onEnable() {
        var cat = new GameSettingCategory(
            ISB.stack(Material.IRON_PICKAXE),
            mm("Test Category"),
            mm("Test Description")
        );

        this.registerSetting(cat, new BooleanSetting(
            mm("Test Setting 1"),
            mm("Test Description 1"),
            ISB.stack(Material.DIAMOND_AXE),
            () -> true
        ));

        this.registerSetting(cat, new BooleanSetting(
            mm("Test Setting 2"),
            mm("Test Description 2"),
            ISB.stack(Material.DIAMOND_AXE),
            () -> false
        ));
    }

    // TODO: settings types
    // TODO:  - int
    // TODO:  - long
    // TODO:  - double
    // TODO:  - float
    // TODO:  - boolean
    // TODO:  - string
    // TODO:  - enum (extends IGameSettingEnum or new GameSettingEnumProxy() to convert it)
    // TODO:  - one of several options (recommended to use enum instead)
    // TODO:  -
}
