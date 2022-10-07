package io.zkz.mc.minigameplugins.gametools.settings.impl;

import io.zkz.mc.minigameplugins.gametools.settings.GameSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class BooleanSetting extends GameSetting<Boolean> {
    private static final ItemStack TRUE_ICON = ISB
        .material(Material.LIME_DYE)
        .name(mm("<lime>Enabled"))
        .build();
    private static final ItemStack FALSE_ICON = ISB
        .material(Material.GRAY_DYE)
        .name(mm("<red>Disabled"))
        .build();

    public BooleanSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<Boolean> defaultValue) {
        super(title, description, display, defaultValue);
    }

    public BooleanSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<Boolean> defaultValue, boolean initialValue) {
        super(title, description, display, defaultValue, initialValue);
    }

    @Override
    public ItemStack optionIcon() {
        return this.value() ? TRUE_ICON : FALSE_ICON;
    }

    @Override
    public void handleLeftClick() {
        this.toggleValue();
    }

    @Override
    public void handleRightClick() {
        this.toggleValue();
    }

    private void toggleValue() {
        this.value(!this.value());
    }
}
