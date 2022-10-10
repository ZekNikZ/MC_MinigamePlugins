package io.zkz.mc.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum TeamStatus implements EnumSetting.GameSettingEnum {
    TEAM_GAME(mm("Team mode"), mm("Players will work together as teams to win."), ISB.stack(Material.IRON_CHESTPLATE)),
    INDIVIDUAL_GAME(mm("Individual mode"), mm("Players will each be competing for themselves."), ISB.stack(Material.IRON_AXE));

    private final Component label;
    private final Component description;
    private final ItemStack display;

    TeamStatus(Component label, Component description, ItemStack display) {
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
