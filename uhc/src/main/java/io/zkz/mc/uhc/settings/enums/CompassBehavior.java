package io.zkz.mc.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum CompassBehavior implements EnumSetting.GameSettingEnum {
    NORMAL(mm("Default compass behavior"), mm("Compasses do not track players"), ISB.stack(Material.BARRIER)),
    TRACK_ENEMIES(mm("Compasses track nearest enemy"), mm("Compasses track nearest non-teammate"), ISB.stack(Material.CREEPER_HEAD)),
    TRACK_PLAYERS(mm("Compasses track nearest player"), mm("Compasses track nearest player"), ISB.stack(Material.PLAYER_HEAD));

    private final Component label;
    private final Component description;
    private final ItemStack display;

    CompassBehavior(Component label, Component description, ItemStack display) {
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
