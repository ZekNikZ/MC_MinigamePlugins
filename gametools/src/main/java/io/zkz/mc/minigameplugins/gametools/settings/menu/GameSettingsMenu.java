package io.zkz.mc.minigameplugins.gametools.settings.menu;

import io.zkz.mc.minigameplugins.gametools.inventory.CustomUI;
import io.zkz.mc.minigameplugins.gametools.inventory.pagination.PageableUIContents;
import io.zkz.mc.minigameplugins.gametools.settings.GameSettingsService;
import org.bukkit.entity.Player;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class GameSettingsMenu extends PageableUIContents {
    public static CustomUI create(boolean withCategories, boolean canEdit) {
        return CustomUI
            .builder((inv, player) -> new GameSettingsMenu(inv, player, withCategories, canEdit))
            .id("settings")
            .title(mm("Game Settings"))
            .size(withCategories ? 6 : 4, 9)
            .build();
    }

    private final boolean withCategories;
    private final boolean canEdit;

    public GameSettingsMenu(CustomUI inv, Player player, boolean withCategories, boolean canEdit) {
        super(inv, player, GameSettingsService);
        this.withCategories = withCategories;
        this.canEdit = canEdit;
    }

    @Override
    protected void init() {
        int base = this.withCategories ? 2 : 0;

        // Categories
        if (this.withCategories) {

        }

        // Settings
    }

    @Override
    public void onPageChange(int oldPage) {
        super.onPageChange(oldPage);
    }
}
