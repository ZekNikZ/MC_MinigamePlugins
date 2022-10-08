package io.zkz.mc.minigameplugins.gametools.settings.menu;

import io.zkz.mc.minigameplugins.gametools.inventory.*;
import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.settings.GameSettingCategory;
import io.zkz.mc.minigameplugins.gametools.settings.GameSettingsService;
import io.zkz.mc.minigameplugins.gametools.settings.IGameSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.gametools.util.ItemStacks;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Comparator;
import java.util.List;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class GameSettingsMenu extends UIContents {
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
    private Pagination categoryPager;
    private Pagination settingPager;
    private PaginationIterator settingDisplayIterator;
    private PaginationIterator settingValueIterator;
    private int base;

    public GameSettingsMenu(CustomUI inv, Player player, boolean withCategories, boolean canEdit) {
        super(inv, player);
        this.withCategories = withCategories;
        this.canEdit = canEdit;
    }

    @Override
    protected void init() {
        this.base = this.withCategories ? 3 : 1;

        List<GameSettingCategory> categories = GameSettingsService.getInstance().getCategories().stream()
            .sorted(Comparator.comparing(x -> PlainTextComponentSerializer.plainText().serialize(x.name()).toLowerCase()))
            .toList();

        // Categories
        if (this.withCategories) {
            this.categoryPager = this.createPagination(this::updateCategoryButtons, (int) Math.ceil((double) categories.size() / 7));
            SlotIterator categoryIter = this.createIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
            this.categoryPager.createIterator(
                categoryIter,
                7,
                categories.stream()
                    .map(category -> ClickableItem.of(
                        ISB.fromItemStack(category.displayIcon())
                            .name(category.name())
                            .lore(category.description())
                            .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                            .build(),
                        clickType -> setCategory(category)
                    )).toList()
            );
        }

        // Settings
        this.settingPager = this.createPagination(this::updateSettings, 0);
        this.setCategory(categories.get(0));
    }

    private void updateCategoryButtons(int prevPage, int newPage) {
        // Left button
        if (newPage > 0) {
            this.set(1, 0, ClickableItem.of(ISB.stack(Material.ARROW), c -> this.categoryPager.prev()));
        } else {
            this.set(1, 0, ClickableItem.of(ItemStacks.AIR));
        }

        // Right button
        if (newPage < this.categoryPager.numPages() - 1) {
            this.set(1, 8, ClickableItem.of(ISB.stack(Material.ARROW), c -> this.categoryPager.next()));
        } else {
            this.set(1, 8, ClickableItem.of(ItemStacks.AIR));
        }
    }

    private void updateSettings(int prevPage, int newPage) {
        // Left button
        if (newPage > 0) {
            this.set(this.base + 2, 0, ClickableItem.of(ISB.stack(Material.ARROW), c -> this.settingPager.prev()));
        } else {
            this.set(this.base + 2, 0, ClickableItem.of(ItemStacks.AIR));
        }

        // Right button
        if (newPage < this.settingPager.numPages() - 1) {
            this.set(this.base + 2, 8, ClickableItem.of(ISB.stack(Material.ARROW), c -> this.settingPager.next()));
        } else {
            this.set(this.base + 2, 8, ClickableItem.of(ItemStacks.AIR));
        }
    }

    private void setCategory(GameSettingCategory category) {
        if (this.settingDisplayIterator != null) {
            this.settingPager.removeIterator(this.settingDisplayIterator);
            this.settingPager.removeIterator(this.settingValueIterator);
        }

        List<IGameSetting<?>> settings = GameSettingsService.getInstance().getSettings().get(category);

        this.settingPager.numPages((int) Math.ceil((double) settings.size() / 7));

        SlotIterator displayIter = this.createIterator(SlotIterator.Type.HORIZONTAL, this.base, 1);
        this.settingDisplayIterator = this.settingPager.createIterator(
            displayIter,
            7,
            settings.stream()
                .map(setting -> ClickableItem.of(
                    ISB.fromItemStack(setting.displayIcon())
                        .name(setting.name())
                        .lore(setting.description())
                        .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                        .build()
                ))
                .toList()
        );

        SlotIterator valueIter = this.createIterator(SlotIterator.Type.HORIZONTAL, this.base + 1, 1);
        this.settingValueIterator = this.settingPager.createIterator(
            valueIter,
            settings.stream()
                .map(setting -> (InventoryItemSupplier) () -> ClickableItem.of(
                    setting.optionIcon(),
                    clickType -> {
                        if (!this.canEdit) {
                            return;
                        }

                        setting.handleClick(clickType);
                        this.settingPager.page(this.settingPager.page()); // reset page
                    }
                ))
                .toList(),
            7
        );
    }
}
