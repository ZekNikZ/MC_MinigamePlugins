package io.zkz.mc.minigameplugins.gametools.settings;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.settings.impl.BooleanSetting;
import io.zkz.mc.minigameplugins.gametools.settings.menu.GameSettingsMenu;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Service
public class GameSettingsService extends PluginService<GameToolsPlugin> {
    private static final GameSettingsService INSTANCE = new GameSettingsService();

    public static GameSettingsService getInstance() {
        return INSTANCE;
    }

    private final Map<GameSettingCategory, List<IGameSetting<?>>> settings = new HashMap<>();

    public void registerSetting(GameSettingCategory category, IGameSetting<?> setting) {
        if (!this.settings.containsKey(category)) {
            this.settings.put(category, new ArrayList<>());
        }

        this.settings.get(category).add(setting);
    }

    public void openMenu(Player player) {
        // TODO: permissions & categories
        GameSettingsMenu.create(true, true)
            .open(player);
    }

    public Set<GameSettingCategory> getCategories() {
        return this.settings.keySet();
    }

    public Map<GameSettingCategory, List<IGameSetting<?>>> getSettings() {
        return this.settings;
    }

    @Override
    protected void onEnable() {
//        var cat = new GameSettingCategory(
//            mm("Test Category"),
//            mm("Test Description"),
//            ISB.stack(Material.IRON_PICKAXE)
//        );
//
//        this.registerSetting(cat, new BooleanSetting(
//            mm("Test Setting 1"),
//            mm("Test Description 1"),
//            ISB.stack(Material.DIAMOND_AXE),
//            () -> true
//        ));
//
//        this.registerSetting(cat, new BooleanSetting(
//            mm("Test Setting 2"),
//            mm("Test Description 2"),
//            ISB.stack(Material.DIAMOND_AXE),
//            () -> false
//        ));
//
//        for (int i = 0; i < 10; i++) {
//            this.registerSetting(cat, new BooleanSetting(
//                mm("Test Setting 3"),
//                mm("Test Description 3"),
//                ISB.stack(Material.GOLDEN_AXE),
//                () -> false
//            ));
//        }
//
//        var cat2 = new GameSettingCategory(
//            mm("Test Category 2"),
//            mm("Test Description 2"),
//            ISB.stack(Material.IRON_PICKAXE)
//        );
//
//        this.registerSetting(cat2, new BooleanSetting(
//            mm("Test Setting 4"),
//            mm("Test Description 4"),
//            ISB.stack(Material.IRON_AXE),
//            () -> false
//        ));
//
//
//        for (int i = 0; i < 10; i++) {
//            this.registerSetting(
//                new GameSettingCategory(
//                    mm("Test Category X" + i),
//                    mm("Test Description X" + i),
//                    ISB.stack(Material.RED_WOOL)
//                ),
//                new BooleanSetting(
//                    mm("Test Setting X" + i),
//                    mm("Test Description X" + i),
//                    ISB.stack(Material.BLUE_WOOL),
//                    () -> false
//                )
//            );
//        }
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
