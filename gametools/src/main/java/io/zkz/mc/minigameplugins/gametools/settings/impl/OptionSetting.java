package io.zkz.mc.minigameplugins.gametools.settings.impl;

import io.zkz.mc.minigameplugins.gametools.settings.IGameSetting;
import io.zkz.mc.minigameplugins.gametools.util.AbstractObservable;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class OptionSetting<T> extends AbstractObservable implements IGameSetting<T> {
    public record Option<T>(T value, Component name, Component description, ItemStack display) {
    }

    private final @NotNull Component title;
    private final @Nullable Component description;
    private final @NotNull Supplier<Option<T>> defaultValue;
    private final @NotNull ItemStack display;
    private final List<Option<T>> options;
    private int index;

    public OptionSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull List<Option<T>> options, @NotNull Supplier<Option<T>> defaultValue) {
        this(title, description, display, options, defaultValue, defaultValue.get());
    }

    public OptionSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull List<Option<T>> options, @NotNull Supplier<Option<T>> defaultValue, Option<T> initialValue) {
        this.title = title;
        this.description = description;
        this.display = display;
        this.options = options;
        this.defaultValue = defaultValue;
        this.index = this.options.indexOf(initialValue);
    }

    @Override
    public final Component name() {
        return this.title;
    }

    @Override
    public final Component description() {
        return this.description;
    }

    @Override
    public T value() {
        return this.options.get(this.index).value();
    }

    @Override
    public void value(T value) {
        this.value(
            this.options.stream()
                .filter(o -> Objects.equals(o.value(), value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Value is not an option"))
        );
    }

    public void value(Option<T> value) {
        this.index = this.options.indexOf(value);
    }

    public void value(int index) {
        this.index = index;
    }

    @Override
    public void resetToDefaultValue() {
        this.value(this.defaultValue.get());
    }

    @Override
    public ItemStack displayIcon() {
        return this.display;
    }

    @Override
    public ItemStack optionIcon() {
        return ISB.fromItemStack(this.options.get(this.index).display())
            .name(this.options.get(this.index).name())
            .lore(this.options.get(this.index).description())
            .build();
    }

    @Override
    public void handleLeftClick() {
        --this.index;
        if (this.index < 0) {
            this.index = this.options.size() - 1;
        }
    }

    @Override
    public void handleRightClick() {
        ++this.index;
        if (this.index >= this.options.size()) {
            this.index = 0;
        }
    }
}
