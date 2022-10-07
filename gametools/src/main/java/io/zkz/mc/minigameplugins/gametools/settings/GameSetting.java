package io.zkz.mc.minigameplugins.gametools.settings;

import io.zkz.mc.minigameplugins.gametools.util.AbstractObservable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class GameSetting<T> extends AbstractObservable implements IGameSetting<T>  {
    private final @NotNull Component title;
    private final @Nullable Component description;
    private final @NotNull Supplier<T> defaultValue;
    private final @NotNull ItemStack display;
    private T value;

    public GameSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<T> defaultValue) {
        this(title, description, display, defaultValue, defaultValue.get());
    }

    public GameSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Supplier<T> defaultValue, T initialValue) {
        this.title = title;
        this.description = description;
        this.defaultValue = defaultValue;
        this.display = display;
        this.value = initialValue;
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
        return this.value;
    }

    @Override
    public void value(T value) {
        this.value = value;
    }

    @Override
    public void resetToDefaultValue() {
        this.value(this.defaultValue.get());
    }

    @Override
    public ItemStack displayIcon() {
        return this.display;
    }
}
