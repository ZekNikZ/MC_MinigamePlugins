package io.zkz.mc.minigameplugins.gametools.settings;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface IGameSetting<T> {
    Component name();

    Component description();

    T value();

    void value(T value);

    void resetToDefaultValue();

    ItemStack displayIcon();

    ItemStack optionIcon();

    default void handleClick(ClickType clickType) {
        switch (clickType) {
            case LEFT -> this.handleLeftClick();
            case RIGHT -> this.handleRightClick();
            case DOUBLE_CLICK -> this.handleDoubleClick();
            case SHIFT_LEFT -> this.handleShiftLeftClick();
            case SHIFT_RIGHT -> this.handleShiftRightClick();
        }
    }

    void handleLeftClick();

    void handleRightClick();

    default void handleDoubleClick() {
        this.resetToDefaultValue();
    }

    default void handleShiftLeftClick() {
        this.handleLeftClick();
    }

    default void handleShiftRightClick() {
        this.handleRightClick();
    }
}
