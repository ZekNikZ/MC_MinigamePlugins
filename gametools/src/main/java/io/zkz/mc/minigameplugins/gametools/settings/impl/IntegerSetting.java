package io.zkz.mc.minigameplugins.gametools.settings.impl;

import io.zkz.mc.minigameplugins.gametools.settings.GameSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class IntegerSetting extends GameSetting<Integer> {
    private final Supplier<Integer> min, max, step;

    public IntegerSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Integer defaultValue, int min, int max, int step) {
        super(title, description, display, () -> defaultValue);
        this.min = () -> min;
        this.max = () -> max;
        this.step = () -> step;
    }

    public IntegerSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<Integer> defaultValue, Supplier<Integer> min, Supplier<Integer> max, Supplier<Integer> step) {
        super(title, description, display, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public IntegerSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<Integer> defaultValue, Integer initialValue, Supplier<Integer> min, Supplier<Integer> max, Supplier<Integer> step) {
        super(title, description, display, defaultValue, initialValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public ItemStack optionIcon() {
        return ISB.material(Material.LIGHT_BLUE_DYE)
            .name(Component.text(this.value()))
            .build();
    }

    @Override
    public void value(Integer value) {
        super.value(clamp(value, this.min.get(), this.max.get()));
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    @Override
    public void handleLeftClick() {
        this.value(this.value() - this.step.get());
    }

    @Override
    public void handleShiftLeftClick() {
        this.value(this.value() - this.step.get() * 5);
    }

    @Override
    public void handleRightClick() {
        this.value(this.value() + this.step.get());
    }

    @Override
    public void handleShiftRightClick() {
        this.value(this.value() + this.step.get() * 5);
    }
}
