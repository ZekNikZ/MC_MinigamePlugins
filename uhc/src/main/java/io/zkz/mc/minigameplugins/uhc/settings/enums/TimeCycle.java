package io.zkz.mc.minigameplugins.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum TimeCycle implements EnumSetting.GameSettingEnum {
    NORMAL(mm("Normal day/night cycle"), mm("Vanilla day/night cycle."), ISB.stack(Material.CLOCK)),
    DAY_ONLY(mm("Day only"), mm("Permanent daytime."), ISB.stack(Material.SUNFLOWER)),
    NIGHT_ONLY(mm("Night only"), mm("Permanent nighttime."), ISB.stack(Material.COBWEB));

    private final Component label;
    private final Component description;
    private final ItemStack display;

    TimeCycle(Component label, Component description, ItemStack display) {
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
