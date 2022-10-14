package io.zkz.mc.minigameplugins.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum WeatherCycle implements EnumSetting.GameSettingEnum {
    CLEAR_ONLY(mm("No weather"), mm("No weather cycle."), ISB.stack(Material.LIGHT_GRAY_DYE)),
    NORMAL(mm("Normal weather cycle"), mm("Vanilla weather cycle."), ISB.stack(Material.BLUE_DYE)),
    RAIN_ONLY(mm("Always raining"), mm("Permanent rain."), ISB.stack(Material.RED_DYE)),
    STORM_ONLY(mm("Always storming"), mm("Permanent lightning storm."), ISB.stack(Material.ORANGE_DYE));

    private final Component label;
    private final Component description;
    private final ItemStack display;

    WeatherCycle(Component label, Component description, ItemStack display) {
        this.label = label;
        this.description = description;
        this.display = display;
    }

    @Override
    public Component label() {
        return this.label;
    }

    @Override
    public Component description() {
        return this.description;
    }

    @Override
    public ItemStack display() {
        return this.display;
    }
}
