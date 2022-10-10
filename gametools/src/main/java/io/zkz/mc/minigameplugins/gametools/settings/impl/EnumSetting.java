package io.zkz.mc.minigameplugins.gametools.settings.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EnumSetting<E extends Enum<E> & EnumSetting.GameSettingEnum> extends OptionSetting<E> {
    public interface GameSettingEnum {
        Component label();

        Component description();

        ItemStack display();
    }

    private record ConstructorInfo<E>(@NotNull Component title, @Nullable Component description,
                                      @NotNull ItemStack display, @NotNull List<Option<E>> options,
                                      @NotNull Supplier<Option<E>> defaultValue, Option<E> initialValue) {
    }

    public EnumSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Class<E> enumClass, @NotNull E defaultValue) {
        this(title, description, display, enumClass, defaultValue, defaultValue);
    }

    public EnumSetting(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Class<E> enumClass, @NotNull E defaultValue, E initialValue) {
        this(makeData(title, description, display, enumClass, defaultValue, initialValue));
    }

    private EnumSetting(ConstructorInfo<E> data) {
        super(data.title(), data.description(), data.display(), data.options(), data.defaultValue(), data.initialValue());
    }

    private static <E extends Enum<E> & GameSettingEnum> ConstructorInfo<E> makeData(@NotNull Component title, @Nullable Component description, @NotNull ItemStack display, @NotNull Class<E> enumClass, @NotNull E defaultValue, E initialValue) {
        E[] enumConstants = enumClass.getEnumConstants();
        List<Option<E>> options = Stream.of(enumConstants)
            .map(v -> new Option<>(
                v,
                v.label(),
                v.description(),
                v.display()
            ))
            .toList();
        var defVal = options.stream().filter(o -> o.value() == defaultValue).findFirst().orElseThrow(IllegalStateException::new);
        var initVal = options.stream().filter(o -> o.value() == initialValue).findFirst().orElseThrow(IllegalStateException::new);

        return new ConstructorInfo<E>(title, description, display, options, () -> defVal, initVal);
    }
}
